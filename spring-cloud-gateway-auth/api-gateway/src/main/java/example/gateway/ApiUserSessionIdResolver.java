package example.gateway;

import java.util.Collections;
import java.util.List;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.WebSessionIdResolver;

public class ApiUserSessionIdResolver implements WebSessionIdResolver {

    private static final String API_KEY_HEADER = "X-Api-Key";

    private final WebSessionIdResolver decorated;

    private final ApiUserRepository userRepos;
    private final ApiUserSessionRegistry sessionRegistry;

    public ApiUserSessionIdResolver(
            WebSessionIdResolver decorated,
            ApiUserRepository userRepos,
            ApiUserSessionRegistry sessionRegistry
            ) {
        this.decorated = decorated;
        this.userRepos = userRepos;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public List<String> resolveSessionIds(ServerWebExchange exchange) {
        var found = decorated.resolveSessionIds(exchange);
        if (found.isEmpty()) {
            found = resolveSessionIdsByApiKey(exchange);
        }
        return found;
    }

    @Override
    public void setSessionId(ServerWebExchange exchange, String sessionId) {
        decorated.setSessionId(exchange, sessionId);
    }

    @Override
    public void expireSession(ServerWebExchange exchange) {
        decorated.expireSession(exchange);
    }

    private List<String> resolveSessionIdsByApiKey(ServerWebExchange exchange) {
        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey == null) {
            return Collections.emptyList();
        }

        ApiUser apiUser = userRepos.findUserByApiKey(apiKey);
        if (apiUser == null) {
            return Collections.emptyList();
        }

        String sessionId = sessionRegistry.getSessionId(apiUser.id());
        if (sessionId == null) {
            return Collections.emptyList();
        }
        return List.of(sessionId);
    }
}
