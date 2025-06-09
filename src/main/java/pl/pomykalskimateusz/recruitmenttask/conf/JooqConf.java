package pl.pomykalskimateusz.recruitmenttask.conf;

import org.jooq.conf.RenderNameCase;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConf {

    @Bean
    public DefaultConfigurationCustomizer configurationCustomizerTest() {
        return configuration -> configuration.settings()
                .withRenderNameCase(RenderNameCase.LOWER);
    }
}
