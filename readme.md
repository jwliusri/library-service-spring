# Library Service Spring Boot

use docker compose up to build the app and run the necessary services.
- app: http://localhost:8080
- mailhog: http://localhost:8025
- swagger: http://localhost:8080/swagger-ui/index.html

db is seeded with users of all roles and private and public articles from every users

### Authentication
- perform login to `/api/auth/login` to get `requestId` for MFA OTP
    - test users can be used with username of `admin`, `editor`, `contributor`, or `viewer` for their respective role with password for all test user `secret`
- check OTP code in mailhog, and perform MFA OTP validation to `/api/auth/validate` with `requestId` from login. successfull validation will return JWT token for authentication

### Code Structure

The foldering structure of this project is Feature-based, while the file is following Layered architecture design pattern of Entity, Repository, Service, and Controller.

```
.
├── LibraryServiceApplication.java
├── article
│   ├── Article.java
│   ├── ArticleController.java
│   ├── ArticleRepository.java
│   ├── ArticleRequestDto.java
│   ├── ArticleResponseDto.java
│   └── ArticleService.java
├── audit
│   ├── AuditAspect.java
│   ├── AuditEntity.java
│   ├── AuditLog.java
│   ├── AuditLogController.java
│   ├── AuditLogRepository.java
│   ├── AuditLogService.java
│   └── Auditable.java
├── config
│   ├── RedisConfig.java
│   └── WebSecurityConfig.java
├── email
│   ├── EmailDetail.java
│   └── EmailService.java
├── security
│   ├── AuthTokenFilter.java
│   ├── CustomAuthenticationProvider.java
│   ├── CustomUserDetailsService.java
│   ├── JwtUtil.java
│   ├── LoginAttemptService.java
│   ├── auth
│   │   ├── AuthController.java
│   │   ├── LoginRequestDto.java
│   │   ├── LoginResponseDto.java
│   │   ├── RegisterRequestDto.java
│   │   ├── ValidateRequestDto.java
│   │   └── ValidateResponseDto.java
│   └── mfa
│       ├── MfaOtp.java
│       ├── MfaOtpRepository.java
│       └── MfaOtpService.java
└── user
    ├── RoleEnum.java
    ├── User.java
    ├── UserController.java
    ├── UserRepository.java
    ├── UserRequestDto.java
    ├── UserResponseDto.java
    └── UserService.java
```


### Unit Test Code Coverage
after running maven test, jacoco report is generated in `target/site/jacoco/index.html`

Instruction Coverage: 92%
Branches Coverage: 82%