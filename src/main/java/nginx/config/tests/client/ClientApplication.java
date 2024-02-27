package nginx.config.tests.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.function.Function;


@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    public ReactorResourceFactory resourceFactory() {
        ConnectionProvider provider =
                ConnectionProvider.builder("test")
                        .maxConnections(500)
                        // Set custom max pending requests
                        .pendingAcquireMaxCount(50000)
                        .pendingAcquireTimeout(Duration.ofMillis(6000000))
                              .build();
        ReactorResourceFactory factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        factory.setConnectionProvider(provider);
        return factory;
    }

    @Bean
    public WebClient webClient() {

        Function<HttpClient, HttpClient> mapper = Function.identity();

        ClientHttpConnector connector =
                new ReactorClientHttpConnector(resourceFactory(), mapper);

        return WebClient.builder().clientConnector(connector).build();
    }
}
