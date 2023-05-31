package example.gateway;

import java.util.Map;
import java.util.HashMap;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;

public class GatewaySecurityContextRepository implements ServerSecurityContextRepository {

    private final ServerSecurityContextRepository decorated;
    private final ApiUserSessionRegistry sessionRegistry;

    private final Map<String, OAuth2AuthenticationToken> principals = new HashMap<>();

    public GatewaySecurityContextRepository(
            ServerSecurityContextRepository decorated,
            ApiUserSessionRegistry sessionRegistry
            ) {
        this.decorated = decorated;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return decorated.load(exchange);
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return decorated.save(exchange, context)
                .then(Mono.defer(() -> exchange.getSession()))
                .flatMap(session -> saveSession(session, context));
    }

    public OAuth2AuthenticationToken findPrincipal(String userId) {
        return principals.get(userId);
    }

    private Mono<Void> saveSession(WebSession session, SecurityContext context) {
        var sessionId = session.getId();
        if (context != null) {
            var authentication = context.getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken token) {
                var principal = authentication.getPrincipal();
                if (principal instanceof OidcUser oidcUser) {
                    sessionRegistry.registerSession(session.getId(), oidcUser.getName());
                }
            }
        } else {
            sessionRegistry.unregisterSession(sessionId);
        }
        return Mono.empty();
    }
}
