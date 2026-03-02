# Silver Lady's Backend (Spring Boot 4.0.1, Java 17)

## Run
1) PostgreSQL: `silver_lady_s_db` yaratib qo'ying
2) `JWT_SECRET` ni albatta kuchli qilib env orqali bering (prod)
3) Start:
```bash
mvn spring-boot:run
```

## Bootstrap admin (ixtiyoriy)
Env:
- BOOTSTRAP_ADMIN_ENABLED=true
- BOOTSTRAP_ADMIN_EMAIL=admin@...
- BOOTSTRAP_ADMIN_PASSWORD=...
- BOOTSTRAP_ADMIN_FULLNAME=...

## Auth
- POST /api/auth/register
- POST /api/auth/login  -> JWT qaytaradi

Authorization header:
`Authorization: Bearer <token>`
