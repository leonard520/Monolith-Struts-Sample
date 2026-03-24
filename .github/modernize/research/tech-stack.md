# Tech Stack Research

## Language and Runtime

| Component | Current Version | Notes |
|-----------|----------------|-------|
| Java | 1.5 (source/target) | `maven.compiler.source=1.5`, `maven.compiler.target=1.5` |
| Servlet API | 2.5 | `javax.servlet:servlet-api:2.5` (provided) |
| JSP API | 2.1 | `javax.servlet.jsp:jsp-api:2.1` (provided) |

## Build System

- **Maven** (pom.xml at project root)
- `groupId`: com.skishop
- `artifactId`: skishop-monolith
- `packaging`: WAR
- `finalName`: skishop-monolith
- Maven plugins: maven-compiler-plugin 2.0.2, maven-war-plugin 2.1, maven-resources-plugin 2.4

## Framework Dependencies

| Dependency | Version | Purpose | EOL Status |
|------------|---------|---------|------------|
| `org.apache.struts:struts-core` | 1.3.10 | MVC framework | **EOL** — last release 2008, known CVEs |
| `org.apache.struts:struts-taglib` | 1.3.10 | JSP tag libraries | **EOL** |
| `org.apache.struts:struts-tiles` | 1.3.10 | Page layout/composition | **EOL** |
| `org.apache.struts:struts-extras` | 1.3.10 | Additional utilities | **EOL** |
| `log4j:log4j` | 1.2.17 | Logging | **EOL** — known CVEs (CVE-2019-17571) |

## Database Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `org.postgresql:postgresql` | 9.2-1004-jdbc3 | PostgreSQL JDBC driver (JDBC3 level) |
| `commons-dbcp:commons-dbcp` | 1.2.2 | Connection pooling |
| `commons-pool:commons-pool` | 1.2 | Object pooling (for DBCP) |
| `commons-dbutils:commons-dbutils` | 1.1 | JDBC utility (QueryRunner, ResultSetHandler) |

## Utility Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `commons-fileupload:commons-fileupload` | 1.3.3 | File upload handling |
| `javax.mail:mail` | 1.4.7 | SMTP email sending |

## Test Dependencies

| Dependency | Version | Scope | Purpose |
|------------|---------|-------|---------|
| `junit:junit` | 4.12 | test | Unit test framework |
| `strutstestcase:strutstestcase` | 2.1.4-1.2-2.4 | test | StrutsTestCase for Struts Action testing |
| `com.h2database:h2` | 1.3.176 | test | In-memory database for DAO tests |

## Deprecated/End-of-Life APIs

1. **Struts 1.3.10** — Last release 2008; no security patches since; known vulnerabilities
2. **log4j 1.2.17** — EOL; CVE-2019-17571 (deserialization RCE); replaced by Log4j 2.x or SLF4J+Logback
3. **Java 1.5 source/target** — Java 5 is EOL since 2009; no security updates
4. **Servlet 2.5 / JSP 2.1** — Superseded by Jakarta Servlet 6.0
5. **commons-dbcp 1.2.2** — Superseded by commons-dbcp2 (Apache DBCP 2.x) or HikariCP
6. **PostgreSQL JDBC3 driver 9.2** — Ancient; current driver is 42.x with JDBC 4.2+
7. **javax.mail 1.4.7** — Superseded by Jakarta Mail
8. **StrutsTestCase** — Tightly coupled to Struts 1.x; no migration path

## Configuration Files

- **struts-config.xml**: 28 action mappings, 12 form-beans, global exceptions/forwards, Tiles + Validator plugins, custom `AuthRequestProcessor`
- **tiles-defs.xml**: 24 tile definitions (1 base layout + 23 pages)
- **validation.xml**: Form validation rules (field-level validators)
- **web.xml**: Servlet 2.5 descriptor — ActionServlet, RequestIdFilter, CharacterEncodingFilter, JNDI DataSource ref
- **app.properties**: Application config with `${TOKEN}` placeholders (DB, mail settings)
- **log4j.properties**: Log4j 1.x configuration
- **messages.properties**: i18n message bundle

## Runtime Environment

- **Application server**: Tomcat 6.0.53 (as documented in Dockerfile)
- **Database**: PostgreSQL 9.2
- **Deployment**: WAR deployed to Tomcat 6 webapps directory
- **Docker**: Multi-stage Dockerfile with JDK 5 + Maven 2.2.1 build stage, Tomcat 6 runtime stage
