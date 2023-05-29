package example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ApiKeyAuthFilter implements GlobalFilter, Ordered {

    private static final String API_KEY_HEADER = "X-Api-Key";

    private final ApiUserRepository userRepos;
    private final GatewaySecurityContextRepository securityContextRepos;

    public ApiKeyAuthFilter(
            ApiUserRepository userRepos,
            GatewaySecurityContextRepository securityContextRepos
            ) {
        this.userRepos = userRepos;
        this.securityContextRepos = securityContextRepos;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        ApiUser apiUser = userRepos.findUserByKey(apiKey);
        if (apiUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        var principal = securityContextRepos.findPrincipal(apiUser.id());

        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        requestBuilder.header("X-User-Id", apiUser.id());

        var exchangeBuilder = exchange.mutate();
        exchangeBuilder.request(requestBuilder.build());

        if (principal != null) {
            // Replaces the principal if exists
            exchangeBuilder.principal(Mono.just(principal));
        }

        return chain.filter(exchangeBuilder.build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
