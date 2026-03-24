package com.skishop.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
@ComponentScan(basePackages = "com.skishop.dao")
public abstract class DaoTestBase {

    @Autowired
    protected DataSource dataSource;

    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement()) {
            st.execute("DROP ALL OBJECTS");
            runScript(con, "/db/schema.sql");
            runScript(con, "/db/data.sql");
        }
    }

    private void runScript(Connection con, String path) throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("SQL resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                 Statement statement = con.createStatement()) {
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append('\n');
                }
                for (String sql : buffer.toString().split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        statement.execute(trimmed);
                    }
                }
            }
        }
    }
}
