package example;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

import java.util.List;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A {@link GatewayFilterFactory} for converting the response body into tabular form.
 */
public class TabularResponseBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<TabularResponseBodyGatewayFilterFactory.Config> {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(TabularResponseBodyGatewayFilterFactory.class);

    private final List<HttpMessageReader<?>> messageReaders;
    private final List<HttpMessageWriter<?>> messageWriters;

    public TabularResponseBodyGatewayFilterFactory(
            List<HttpMessageReader<?>> messageReaders,
            List<HttpMessageWriter<?>> messageWriters) {
        super(Config.class);
        this.messageReaders = messageReaders;
        this.messageWriters = messageWriters;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new TabularResponseGatewayFilter(config);
    }

    /**
     * Configuration for this filter.
     */
    public static class Config {

        private FlatMapper<?, ?> mapper;
        private String newContentType;

        public FlatMapper<?, ?> getMapper() {
            return this.mapper;
        }

        public void setMapper(FlatMapper<?, ?> mapper) {
            this.mapper = mapper;
        }

        public String getNewContentType() {
            return newContentType;
        }

        public void setNewContentType(String newContentType) {
            this.newContentType = newContentType;
        }
    }

    public class TabularResponseGatewayFilter implements GatewayFilter, Ordered {

        private final Config config;

        public TabularResponseGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            if (this.config.mapper == null) {
                return chain.filter(exchange);
            }
            var modified = exchange.mutate()
                    .response(new TabularServerHttpResponse(exchange, config))
                    .build();
            return chain.filter(modified);
        }

        @Override
        public int getOrder() {
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
        }
    }

    protected class TabularServerHttpResponse extends ServerHttpResponseDecorator {

        private final ServerWebExchange exchange;
        private final Config config;

        public TabularServerHttpResponse(ServerWebExchange exchange, Config config) {
            super(exchange.getResponse());
            this.exchange = exchange;
            this.config = config;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

            @SuppressWarnings("unchecked")
            FlatMapper<Object, Object> mapper = (FlatMapper<Object, Object>) config.getMapper();
            Class<Object> inClass = mapper.getInClass();
            Class<Object> outClass = mapper.getOutClass();

            Flux<Object> modifiedBody = extractBody(body, inClass)
                    .flatMapMany(originalBody -> Flux.fromStream(mapper.map(originalBody)));

            return writeWithModifiedBody(modifiedBody, outClass);
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return writeWith(Flux.from(body).flatMapSequential(p -> p));
        }

        // Note: Content-Encoding is not supported yet.
        private <T> Mono<T> extractBody(Publisher<? extends DataBuffer> body, Class<T> inClass) {

            var clientResponse = buildClientResponse(body);

            var encodingHeaders = exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
            if (!encodingHeaders.isEmpty()) {
                throw new UnsupportedOperationException();
            }

            return clientResponse.bodyToMono(inClass);
        }

        private ClientResponse buildClientResponse(Publisher<? extends DataBuffer> body) {
            ClientResponse.Builder builder = ClientResponse.create(
                    exchange.getResponse().getStatusCode(),
                    messageReaders);

            String contentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);

            return builder
                    .headers(headers -> headers.putAll(httpHeaders))
                    .body(Flux.from(body))
                    .build();
        }

        private <T> Mono<Void> writeWithModifiedBody(Publisher<T> body, Class<T> outClass) {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            var newContentType = config.getNewContentType();
            if (newContentType != null) {
                headers.set(HttpHeaders.CONTENT_TYPE, newContentType);
            }
            var outputMessage = new CachedBodyOutputMessage(exchange, headers);

            var inserter = BodyInserters.fromPublisher(body, outClass);
            var inserterContext = createInserterContext();
            return inserter.insert(outputMessage, inserterContext)
                        .then(Mono.defer(() -> writeWholeResponseBody(outputMessage)));
        }

        private Mono<Void> writeWholeResponseBody(CachedBodyOutputMessage message) {
            Mono<DataBuffer> responseBody = DataBufferUtils.join(message.getBody());

            HttpHeaders headers = getDelegate().getHeaders();
            if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)
              || headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                responseBody = responseBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
            }

            return getDelegate().writeWith(responseBody);
        }

        protected BodyInserterContext createInserterContext() {
            return new BodyInserterContext() {
                @Override
                public List<HttpMessageWriter<?>> messageWriters() {
                    return messageWriters;
                }
            };
        }
    }
}
