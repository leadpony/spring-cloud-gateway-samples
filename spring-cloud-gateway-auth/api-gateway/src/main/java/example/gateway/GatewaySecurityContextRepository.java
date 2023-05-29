package example.gateway;

import java.util.Map;
import java.util.HashMap;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class GatewaySecurityContextRepository extends ServerSecurityContextRepositoryDecorator {

    private final Map<String, OAuth2AuthenticationToken> principals = new HashMap<>();

    public GatewaySecurityContextRepository(ServerSecurityContextRepository decorated) {
        super(decorated);
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // TODO: handles if null
        if (context != null) {
            addPrincipal(context);
        }
        return super.save(exchange, context);
    }

    public OAuth2AuthenticationToken findPrincipal(String userId) {
        return principals.get(userId);
    }

    private void addPrincipal(SecurityContext context) {
        var authentication = context.getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken token) {
            var principal = authentication.getPrincipal();
            if (principal instanceof OidcUser oidcUser) {
                principals.put(oidcUser.getName(), token);
            }
        }
    }
}
