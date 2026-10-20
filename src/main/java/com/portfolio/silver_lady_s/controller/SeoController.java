package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.SlugProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

/**
 * SEO uchun ochiq endpointlar: sitemap.xml va robots.txt.
 * Frontend (React SPA) tomonida render qilinmagan sahifalarni Google'ga ko'rsatish uchun.
 */
@RestController
@RequiredArgsConstructor
public class SeoController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.seo.site-url}")
    private String siteUrl;

    @Value("${app.seo.product-path}")
    private String productPath;

    @Value("${app.seo.category-path}")
    private String categoryPath;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Cacheable(CacheConfig.CACHE_SITEMAP)
    public ResponseEntity<String> sitemap() {
        String base = stripTrailingSlash(siteUrl);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        appendUrl(sb, base + "/", null);

        for (SlugProjection c : categoryRepository.findAllSlugsForSitemap()) {
            appendUrl(sb, base + categoryPath + c.getId(), c.getUpdatedAt());
        }

        for (SlugProjection p : productRepository.findActiveSlugsForSitemap()) {
            appendUrl(sb, base + productPath + p.getSlug(), p.getUpdatedAt());
        }

        sb.append("</urlset>\n");

        return ResponseEntity.ok().body(sb.toString());
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain")
    public ResponseEntity<String> robots() {
        String base = stripTrailingSlash(siteUrl);
        // /api/ ochiq qolishi shart: React sahifalar ma'lumotni API'dan oladi, Googlebot render qilganda
        // robots.txt'ga amal qiladi — /api/ yopilsa sahifa bo'sh ko'rinadi va indekslanmaydi.
        // Shaxsiy sahifalar frontend'da <meta robots="noindex"> bilan yopilgan.
        String body = """
                User-agent: *
                Disallow: /admin/
                Allow: /

                Sitemap: %s/sitemap.xml
                """.formatted(base);
        return ResponseEntity.ok().body(body);
    }

    private void appendUrl(StringBuilder sb, String loc, java.time.Instant updatedAt) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        if (updatedAt != null) {
            sb.append("    <lastmod>")
                    .append(DATE_FMT.format(updatedAt.atZone(ZoneOffset.UTC)))
                    .append("</lastmod>\n");
        }
        sb.append("  </url>\n");
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
