package example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(authorize ->
                authorize
                    .pathMatchers("/profile").authenticated()
                    .anyExchange().permitAll()
                )
            .securityContextRepository(securityContextRepository())
            .oauth2Login(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public GatewaySecurityContextRepository securityContextRepository() {
        return new GatewaySecurityContextRepository(
                new WebSessionServerSecurityContextRepository()
                );
    }
}
