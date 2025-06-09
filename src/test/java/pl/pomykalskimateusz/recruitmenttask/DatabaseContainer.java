package pl.pomykalskimateusz.recruitmenttask;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ComponentScan
@Configuration
public class DatabaseContainer {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");

    @Autowired
    private DSLContext dslContext;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (!postgres.isRunning()) {
            postgres.start();
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("database_url", postgres::getJdbcUrl);
        registry.add("database_username", postgres::getUsername);
        registry.add("database_password", postgres::getPassword);

        registry.add("spring.liquibase.liquibase-schema", () -> "public");
    }

    public void cleanDatabase(String schema) {
        DatabaseCleaner.cleanAllTables(dslContext, schema);
    }

    public void cleanDatabase(String schema, boolean cache) {
        DatabaseCleaner.cleanAllTables(dslContext, schema, cache);
    }
}
