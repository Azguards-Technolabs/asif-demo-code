package io.azguards.services.enterprise.data.config;

import io.marketplace.commons.jwt.JWTFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Configuration
@ComponentScan({"io.marketplace.commons.gson", "io.marketplace.commons.exception.utils"})
@Import({
    io.marketplace.commons.config.BaseSerializationConfig.class,
    io.marketplace.commons.config.GsonConfiguration.class,
    io.marketplace.commons.config.RestTemplateConfig.class,
    io.marketplace.commons.config.HttpClientConfig.class,
    io.marketplace.commons.crypto.jws.JWSUtils.class,
    io.marketplace.commons.config.JWTFactoryConfig.class,
    io.marketplace.commons.config.JWTConfigurations.class,
    io.marketplace.commons.filter.CustomWebSecurity.class,
    io.marketplace.commons.security.CustomSecurityAspect.class,
    io.marketplace.commons.exception.handler.HttpExceptionHandler.class,
    io.marketplace.commons.config.RequestLogConfig.class,
    io.marketplace.commons.config.ExtraConfigLoader.ConfigLoader.class,
    io.marketplace.commons.utils.EventMessageUtils.class
})
public class AppConfig {

    @Primary
    @Bean(name = "gsonRestTemplate")
    public RestTemplate restTemplate(RestTemplate restTemplate) {
        return restTemplate;
    }

    @Value("${jwt.header-name:X-JWT-Assertion}")
    private String jwtHeaderName;

    @Value("${jwt.default-membership-id:}")
    private String defaultMembershipId;

    @Bean("internal-rest-template")
    public RestTemplate internalRestTemplate(@Autowired
                                             @Qualifier("gson-rest-template-customizer")
                                             RestTemplateCustomizer restTemplateCustomizer,
                                             @Qualifier("internal-jwt-factory")
                                             JWTFactory jwtFactory) {
        RestTemplate restTemplate =
            new RestTemplateBuilder(restTemplateCustomizer)
                .build();
        restTemplate.getInterceptors().add(new InternalJwtInterceptor(jwtHeaderName, jwtFactory, defaultMembershipId));
        return restTemplate;
    }

    public static class InternalJwtInterceptor implements ClientHttpRequestInterceptor {

        private final String jwtHeaderName;
        private final JWTFactory jwtFactory;

        private final String defaultMembershipId;

        public InternalJwtInterceptor(String jwtHeaderName, JWTFactory jwtFactory, String defaultMembershipId) {
            this.jwtHeaderName = jwtHeaderName;
            this.jwtFactory = jwtFactory;
            this.defaultMembershipId = defaultMembershipId;
        }

        @Override
        @SneakyThrows
        public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) {

            String jwt = jwtFactory.generateUserToken(Optional.ofNullable(defaultMembershipId).orElse(UUID.randomUUID().toString()),
                Arrays.asList("SuperAdmin"));
            request.getHeaders().add(jwtHeaderName, jwt);
            ClientHttpResponse response = execution.execute(request, body);
            return response;
        }
    }
}
