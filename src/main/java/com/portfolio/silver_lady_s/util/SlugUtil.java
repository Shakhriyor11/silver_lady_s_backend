package com.portfolio.silver_lady_s.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Nomdan SEO-friendly slug (URL bo'lagi) yasash uchun yordamchi klass.
 * Masalan: "Kumush uzuk №5" -> "kumush-uzuk-5"
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN   = Pattern.compile("[^a-zA-Z0-9\\s-]");
    private static final Pattern WHITESPACE  = Pattern.compile("[\\s-]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("^-+|-+$");

    private SlugUtil() {}

    public static String slugify(String input) {
        if (input == null) return "";

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", ""); // diakritik belgilarni olib tashlash

        String withoutSpecials = NON_LATIN.matcher(normalized).replaceAll("");
        String collapsed = WHITESPACE.matcher(withoutSpecials.trim()).replaceAll("-");
        String slug = EDGE_DASHES.matcher(collapsed).replaceAll("").toLowerCase();

        return slug.isBlank() ? "" : slug;
    }

    /** Nom + id dan noyob slug yasaydi (id tufayli to'qnashuv imkoni yo'q). */
    public static String slugifyWithId(String input, Long id) {
        String base = slugify(input);
        return (base.isBlank() ? "mahsulot" : base) + "-" + id;
    }
}
