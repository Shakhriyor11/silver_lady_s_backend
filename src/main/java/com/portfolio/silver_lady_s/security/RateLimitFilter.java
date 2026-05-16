package com.portfolio.silver_lady_s.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * IP manzili bo'yicha login/register endpointlarini himoyalaydi.
 * Har IP uchun 1 daqiqada maksimal 10 ta urinish.
 * Limitdan oshilsa 429 Too Many Requests qaytaradi.
 * app.rate-limit.enabled=false bilan o'chiriladi (masalan, test muhitida).
 *
 * Xavfsizlik:
 *  - X-Real-IP ishlatiladi (Nginx tomonidan o'rnatiladi) — X-Forwarded-For
 *    client tomonidan soxtalashtirishi mumkin bo'lgani uchun ISHLATILMAYDI.
 *  - Bucket map Caffeine cache bilan boshqariladi (TTL + max size) —
 *    cheksiz o'sib DoS'ga olib kelmasligi uchun.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    private static final int    MAX_REQUESTS_PER_MINUTE     = 10;
    private static final int    OTP_MAX_REQUESTS_PER_MINUTE = 5;
    private static final String LOGIN_PATH    = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";
    private static final String OTP_SEND_PATH = "/api/auth/otp/send";

    // Caffeine: 10 daqiqa ishlatilmagan IP avtomatik o'chiriladi; max 50 000 ta yozuv
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(50_000)
            .build();

    // OTP uchun alohida, qattiqroq bucket (SMS xarajati oldini olish)
    private final Cache<String, Bucket> otpBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(50_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        boolean isOtpPath  = path.equals(OTP_SEND_PATH);
        boolean isAuthPath = path.equals(LOGIN_PATH) || path.equals(REGISTER_PATH);

        if (!isAuthPath && !isOtpPath) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        Bucket bucket = isOtpPath
                ? otpBuckets.get(ip, this::newOtpBucket)
                : buckets.get(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"status":429,"title":"Too Many Requests","detail":"Juda ko'p urinish. 1 daqiqadan so'ng qayta urining."}
                    """);
        }
    }

    private Bucket newBucket(String ip) {
        Refill refill = Refill.intervally(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS_PER_MINUTE, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newOtpBucket(String ip) {
        Refill refill = Refill.intervally(OTP_MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(OTP_MAX_REQUESTS_PER_MINUTE, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Nginx {@code proxy_set_header X-Real-IP $remote_addr;} orqali o'rnatilgan
     * headerdan haqiqiy client IP olinadi.
     * X-Forwarded-For ishlatilmaydi — client tomonidan soxtalashtirish mumkin.
     */
    private String getClientIp(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }
}
