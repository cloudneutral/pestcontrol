package io.cockroachdb.pestcontrol.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import io.cockroachdb.pestcontrol.ApplicationProfiles;

@Configuration
@Profile(value = {ApplicationProfiles.DEV})
public class ThymeleafConfiguration {
    @Autowired
    private ThymeleafProperties properties;

    @Bean
    public ITemplateResolver defaultTemplateResolver() {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(properties.getSuffix());
        resolver.setPrefix("src/main/resources/templates/");
        resolver.setTemplateMode(properties.getMode());
        resolver.setCharacterEncoding(properties.getEncoding().name());
        resolver.setCacheable(false);
        resolver.setOrder(0);
        return resolver;
    }
}
