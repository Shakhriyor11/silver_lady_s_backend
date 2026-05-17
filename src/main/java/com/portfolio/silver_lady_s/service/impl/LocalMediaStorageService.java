package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.service.MediaStorageService;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Rasmlarni lokal diskka saqlaydi.
 * Kelajakda S3/MinIO ga o'tish uchun faqat shu class o'zgartiriladi —
 * qolgan kod MediaStorageService interface orqali ishlaydi.
 */
@Service
public class LocalMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalMediaStorageService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final int MAX_DIMENSION = 1920;

    private final Path uploadRoot;
    private final long maxFileSizeBytes;

    public LocalMediaStorageService(
            @Value("${app.media.upload-dir:uploads}") String uploadDir,
            @Value("${app.media.max-file-size-mb:10}") long maxFileSizeMb
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeMb * 1024 * 1024;
        createDirectoryIfNotExists(uploadRoot);
    }

    @Override
    public String store(MultipartFile file, Long productId) {
        validateFile(file);

        String ext = resolveExtension(file.getContentType());
        String filename = UUID.randomUUID() + "." + ext;
        Path productDir = uploadRoot.resolve("products").resolve(String.valueOf(productId));

        try {
            Files.createDirectories(productDir);
            Path target = productDir.resolve(filename);
            compressAndSave(file, target);
            return "/uploads/products/" + productId + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }
        // /uploads/products/1/abc.jpg → {uploadRoot}/products/1/abc.jpg
        String relative = url.substring("/uploads/".length());
        Path target = uploadRoot.resolve(relative).normalize();

        // Path traversal himoyasi
        if (!target.startsWith(uploadRoot)) {
            log.warn("Suspicious delete path rejected: {}", url);
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", target, e);
        }
    }

    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException(
                    "File too large: max " + (maxFileSizeBytes / 1024 / 1024) + " MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Unsupported file type: " + contentType + ". Allowed: JPEG, PNG, WebP");
        }
    }

    private void compressAndSave(MultipartFile file, Path target) throws IOException {
        try (InputStream in = file.getInputStream()) {
            Thumbnails.of(in)
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .keepAspectRatio(true)
                    // 1920px dan kichik rasmlarni kattalashtirmaydi
                    .allowOverwrite(false)
                    .outputQuality(0.85)
                    .toFile(target.toFile());
        }
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default           -> "jpg";
        };
    }

    private void createDirectoryIfNotExists(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + path, e);
        }
    }
}
