# t5 — Created Files

## Build & Entry
| File | Purpose |
|------|---------|
| `monolith-new/pom.xml` | Maven build — Spring Boot 3.2.5 parent, Java 21, all dependencies per architect spec |
| `monolith-new/src/main/java/com/skishop/Application.java` | `@SpringBootApplication` entry point |

## Configuration
| File | Purpose |
|------|---------|
| `monolith-new/src/main/resources/application.properties` | Server port 8080, JSP view resolver, SQL init, message source, multipart limits |
| `monolith-new/src/main/resources/application-h2.properties` | H2 in-memory DB with `MODE=PostgreSQL` for `api-test.sh` |
| `monolith-new/src/main/resources/application-postgresql.properties` | PostgreSQL production profile with env-var credentials |
| `monolith-new/src/main/java/com/skishop/config/DataSourceConfig.java` | `JdbcTemplate` bean, `@EnableTransactionManagement`, `DataSourceTransactionManager` |
| `monolith-new/src/main/java/com/skishop/config/WebMvcConfig.java` | Static resource handler `/assets/**`, CSRF interceptor registration |
| `monolith-new/src/main/java/com/skishop/config/SecurityConfig.java` | Spring Security filter chain — CSRF with `_csrfToken` param name, URL authorization matching constitution, form-login disabled (manual auth), custom entry point redirect to `/login` |
| `monolith-new/src/main/java/com/skishop/config/CsrfTokenConfig.java` | Interceptor that exposes `_csrf` and `_csrfToken` to JSP model for hidden fields |

## Utilities
| File | Purpose |
|------|---------|
| `monolith-new/src/main/java/com/skishop/common/util/PasswordHasher.java` | SHA-256 + salt, 1000 iterations — verbatim port from source, ensures existing password hashes remain valid |

## Error Handling
| File | Purpose |
|------|---------|
| `monolith-new/src/main/java/com/skishop/web/GlobalExceptionHandler.java` | `@ControllerAdvice` — catches unhandled exceptions, renders `error` view |

## Data
| File | Purpose |
|------|---------|
| `monolith-new/src/main/resources/schema.sql` | Copied verbatim from `src/main/resources/db/schema.sql` — 22 tables |
| `monolith-new/src/main/resources/data.sql` | Copied verbatim from `src/main/resources/db/data.sql` — seed data (141 products, users, orders, etc.) |
| `monolith-new/src/main/resources/messages.properties` | Copied verbatim from source — Chinese/Japanese UI labels and error messages |

## Verification
- `mvn -f monolith-new/pom.xml clean package -DskipTests -q` → SUCCESS
- `java -jar monolith-new/target/skishop-app-2.0.0.jar --spring.profiles.active=h2` → Starts in ~1.5s, port 8080
- H2 schema + data init → SUCCESS (no SQL errors in startup log)
- Spring Security filter chain wired with correct CSRF and authorization rules
