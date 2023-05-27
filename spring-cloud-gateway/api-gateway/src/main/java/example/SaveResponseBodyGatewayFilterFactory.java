package example;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SaveResponseBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<SaveResponseBodyGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(SaveResponseBodyGatewayFilterFactory.class);

    public SaveResponseBodyGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new SaveResponseBodyGatewayFilter(config);
    }

    public static class Config {

        private String paramName = "output";

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }
    }

    static class SaveResponseBodyGatewayFilter implements GatewayFilter, Ordered {

        private final Config config;

        SaveResponseBodyGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            var modified = exchange.mutate()
                    .response(new SavedServerHttpResponse(exchange, this.config))
                    .build();
            return chain.filter(modified);
        }

        @Override
        public int getOrder() {
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
        }
    }

    static class SavedServerHttpResponse extends ServerHttpResponseDecorator {

        private final ServerWebExchange exchange;
        private final Config config;

        SavedServerHttpResponse(ServerWebExchange exchange, Config config) {
            super(exchange.getResponse());
            this.exchange = exchange;
            this.config = config;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            var newBody = saveToFile(body).then(emptyBody());
            return super.writeWith(newBody);
        }

        private Mono<DataBuffer> emptyBody() {
            var bufferFactory = exchange.getResponse().bufferFactory();
            var dataBuffer = bufferFactory.allocateBuffer(0);
            return Mono.just(dataBuffer);
        }

        private Mono<Void> saveToFile(Publisher<? extends DataBuffer> body) {
            String output = exchange.getRequest().getQueryParams().getFirst(config.getParamName());
            // TODO: Prevent directory traversal
            Path path = Path.of("responses", output);
            logger.info("Saved at " + path.toString());
            return DataBufferUtils.write(Flux.from(body), path, StandardOpenOption.CREATE);
        }
    }
}
