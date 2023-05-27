package example;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class ApiKeyGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiKeyGatewayFilterFactory.Config> {

    private final UserRepository userRepository;

    public ApiKeyGatewayFilterFactory(UserRepository userRepository) {
        super(Config.class);
        this.userRepository = userRepository;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new ApiKeyGatewayFilter(config);
    }

    public static class Config {
    }

    public class ApiKeyGatewayFilter implements GatewayFilter {

        @SuppressWarnings("unused")
        private final Config config;

        public ApiKeyGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            var request = exchange.getRequest();
            var user = userRepository.findUserAt(request.getRemoteAddress());

            ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
            // Add request headers
            builder.header("X-User-Id", user.id());
            builder.header("X-Api-Key", user.apiKey());

            var modified = exchange.mutate().request(builder.build()).build();
            return chain.filter(modified);
        }
    }
}
