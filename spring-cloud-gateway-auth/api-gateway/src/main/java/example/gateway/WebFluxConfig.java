package example.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.session.DefaultWebSessionManager;
import org.springframework.web.server.session.WebSessionManager;

@Configuration
public class WebFluxConfig {

    @Bean
    public ApiUserSessionRegistry apiUserSessionRegistry(
            WebSessionManager sessionManager,
            ApiUserRepository userRepos
            ) {
        var sessionRegistry = new ApiUserSessionRegistry();

        var defaultSessionManager = (DefaultWebSessionManager) sessionManager;
        var oldResolver = defaultSessionManager.getSessionIdResolver();
        var newResolver = new ApiUserSessionIdResolver(oldResolver, userRepos, sessionRegistry);

        defaultSessionManager.setSessionIdResolver(newResolver);

        return sessionRegistry;
    }
}
