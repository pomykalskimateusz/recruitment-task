package pl.pomykalskimateusz.recruitmenttask.conf;

import liquibase.integration.spring.MultiTenantSpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class CouponLiquibaseConf {
    @Value("${spring.liquibase.liquibase-schema:db_changelog}")
    private String schema;

    @Bean
    public MultiTenantSpringLiquibase couponLiquibase(DataSource dataSource) {
        var moduleConfig = new MultiTenantSpringLiquibase();
        moduleConfig.setDataSource(dataSource);
        moduleConfig.setDefaultSchema("public");
        moduleConfig.setLiquibaseSchema(schema);
        moduleConfig.setChangeLog("db/coupons/changelog/postgres/db-changelog.xml");
        return moduleConfig;
    }
}
