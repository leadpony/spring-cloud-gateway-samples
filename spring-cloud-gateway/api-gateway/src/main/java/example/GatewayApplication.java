package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public TabularResponseBodyGatewayFilterFactory convertResponseBodyGatewayFilterFactory(
            ServerCodecConfigurer codecConfigurer) {
        return new TabularResponseBodyGatewayFilterFactory(
                codecConfigurer.getReaders(),
                codecConfigurer.getWriters());
    }
}
