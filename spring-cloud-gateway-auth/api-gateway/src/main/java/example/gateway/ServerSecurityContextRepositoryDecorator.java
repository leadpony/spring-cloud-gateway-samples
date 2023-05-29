package example.gateway;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class ServerSecurityContextRepositoryDecorator implements ServerSecurityContextRepository {

    private final ServerSecurityContextRepository decorated;

    public ServerSecurityContextRepositoryDecorator(ServerSecurityContextRepository decorated) {
        this.decorated = decorated;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return decorated.save(exchange, context);
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return decorated.load(exchange);
    }
}
