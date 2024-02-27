package nginxTraficTest.com.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class ClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
	private static final String HEALTH_ENDPOINT = "http://localhost:8080/health";
	private static final String TIMEOUT_ENDPOINT = "http://localhost:8080/timeout";

//	@Value("${numberOFCalls}")
	private static long numberOfCalls = 100;

	private static final AtomicInteger successCounter = new AtomicInteger(0);
	private static final AtomicInteger failureCounter = new AtomicInteger(0);

	public static void main(String[] args) throws InterruptedException {

		WebClient webClient = WebClient.create();
		while(true) {
			CountDownLatch latch = new CountDownLatch(1);
			Flux.interval(Duration.ZERO, Duration.ofMillis(1))
					.take(numberOfCalls * 2)
					.flatMap(i -> Flux.merge(makeAsyncCall(webClient, HEALTH_ENDPOINT), makeAsyncCall(webClient, TIMEOUT_ENDPOINT)))
					.doOnTerminate(() -> {
						// Sleep for 900ms after all requests
						try {
							Thread.sleep(900);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						checkResults();
					})
					.subscribe(null, null, latch::countDown);
			latch.await();
		}

//		SpringApplication.run(ClientApplication.class, args);
	}


	private static Flux<Void> makeAsyncCall(WebClient webClient, String endpoint) {
		return webClient.get()
				.uri(endpoint)
				.retrieve()
				.bodyToMono(String.class)
				.doOnNext(response -> {
					if (response.startsWith("ok")) {
						successCounter.incrementAndGet();
						logger.info("requesting "+endpoint);
					} else {
						failureCounter.incrementAndGet();
					}
				})
				.doOnError(error -> logger.error("Error in async call", error))
				.thenMany(Flux.empty());
	}

	private static void checkResults() {
		// Use logging instead of System.out.println
		logger.info("Success Count: {}", successCounter.get());
		logger.info("Failure Count: {}", failureCounter.get());
	}

}
