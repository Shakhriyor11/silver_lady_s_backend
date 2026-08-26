package com.portfolio.silver_lady_s.bootstrap;

import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductSizeEntry;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Faqat test/lokal muhitda ishlaydi — SEED_PRODUCTS_ENABLED=true bo'lsagina
 * ishga tushadi va faqat products jadvali bo'sh bo'lsa (o'chirilgan holatda hech narsa qilmaydi).
 */
@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.seed.products.enabled:false}")
    private boolean enabled;

    @Value("${app.seed.products.count:1000}")
    private int count;

    private static final String[] JEWELRY_TYPES = {
            "Uzuk", "Sirg'a", "Bilaguzuk", "Zanjir", "Marjon", "Kulon", "Broshka", "Uzuklar to'plami"
    };

    private static final String[] MATERIALS = {
            "925 kumush", "kumush, oltin suvi yugurtirilgan", "kumush, zircon toshli",
            "kumush, marvarid bilan", "kumush, minakori", "kumush, gravirovkali"
    };

    private static final String[] ADJECTIVES = {
            "Klassik", "Zamonaviy", "Nafis", "Hashamatli", "Minimalist", "Vintage",
            "Романтик", "Elegant", "Original", "Nozik"
    };

    private static final String[] RING_SIZES = {"15", "16", "17", "18", "19", "20", "21"};
    private static final String[] BRACELET_SIZES = {"S", "M", "L"};

    private static final String[] CATEGORY_NAMES = {
            "Uzuklar", "Sirg'alar", "Bilaguzuklar", "Zanjirlar", "Marjonlar", "Kulonlar"
    };

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
        if (productRepository.existsByBarcode("TEST-000001")) {
            log.info("ProductSeeder: test mahsulotlar allaqachon qo'shilgan, qayta seed qilinmadi");
            return;
        }

        List<Category> categories = resolveCategories();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        int batchSize = 200;
        List<Product> batch = new ArrayList<>(batchSize);

        for (int i = 1; i <= count; i++) {
            batch.add(buildProduct(i, categories, rnd));

            if (batch.size() == batchSize || i == count) {
                productRepository.saveAll(batch);
                batch.clear();
            }
        }

        log.info("ProductSeeder: {} ta test mahsulot qo'shildi", count);
    }

    private List<Category> resolveCategories() {
        List<Category> existing = categoryRepository.findAllByParentIsNullOrderBySortOrderAscIdAsc();
        if (!existing.isEmpty()) return existing;

        List<Category> created = new ArrayList<>();
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            Category c = new Category();
            c.setName(CATEGORY_NAMES[i]);
            c.setNameUz(CATEGORY_NAMES[i]);
            c.setSortOrder(i);
            created.add(categoryRepository.save(c));
        }
        return created;
    }

    private Product buildProduct(int index, List<Category> categories, ThreadLocalRandom rnd) {
        String type = JEWELRY_TYPES[rnd.nextInt(JEWELRY_TYPES.length)];
        String adjective = ADJECTIVES[rnd.nextInt(ADJECTIVES.length)];
        String material = MATERIALS[rnd.nextInt(MATERIALS.length)];

        Product p = new Product();
        p.setName("%s %s №%d".formatted(adjective, type, index));
        p.setNameUz(p.getName());
        p.setDescription("%s. Material: %s.".formatted(p.getName(), material));
        p.setBarcode("TEST-%06d".formatted(index));

        BigDecimal price = BigDecimal.valueOf(50_000 + rnd.nextInt(1_950_000))
                .setScale(0, java.math.RoundingMode.HALF_UP);
        p.setPrice(price);

        if (rnd.nextInt(100) < 25) {
            p.setDiscountPercent(5 + rnd.nextInt(46));
        }

        p.setActive(rnd.nextInt(100) < 90);

        int categoryCount = 1 + rnd.nextInt(Math.min(2, categories.size()));
        for (int c = 0; c < categoryCount; c++) {
            p.getCategories().add(categories.get(rnd.nextInt(categories.size())));
        }

        boolean sized = "Uzuk".equals(type) || "Bilaguzuk".equals(type);
        if (sized) {
            String[] sizes = "Uzuk".equals(type) ? RING_SIZES : BRACELET_SIZES;
            int total = 0;
            for (int s = 0; s < sizes.length; s++) {
                int qty = rnd.nextInt(15);
                total += qty;
                ProductSizeEntry entry = new ProductSizeEntry();
                entry.setProduct(p);
                entry.setSize(sizes[s]);
                entry.setQuantity(qty);
                entry.setSortOrder(s);
                p.getSizeEntries().add(entry);
            }
            p.setStockQuantity(total);
        } else {
            p.setStockQuantity(rnd.nextInt(50));
        }

        return p;
    }
}
