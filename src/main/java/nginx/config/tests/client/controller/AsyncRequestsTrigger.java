package nginx.config.tests.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.resources.ConnectionProvider;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class AsyncRequestsTrigger {

	private final Logger logger = LoggerFactory.getLogger(AsyncRequestsTrigger.class);
	private final String HEALTH_ENDPOINT = "http://localhost:8080/health";
	private final String TIMEOUT_ENDPOINT = "http://localhost:8080/timeout";

	@Value("${numberOFCalls}")
	private long numberOfCalls;


	@Autowired
	WebClient webClient;
	private final AtomicInteger successCounterHealth = new AtomicInteger(0);
	private final AtomicInteger failureCounterHealth = new AtomicInteger(0);
	private final AtomicInteger successCounterTimeout = new AtomicInteger(0);
	private final AtomicInteger failureCounterTimeout = new AtomicInteger(0);

	@GetMapping("/trigger")
	public void trigger() {
		var endpoints = List.of(HEALTH_ENDPOINT);
		while (true) {
			for (String endpoint : endpoints) {
				try {
					handle(endpoint);
					logResults();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void handle(String endpoint) throws InterruptedException {
		CountDownLatch latch = new CountDownLatch((int) numberOfCalls);
		Flux.range(0, (int) numberOfCalls)
				.flatMap(i -> makeAsyncCall(webClient, endpoint)
						.doOnNext(response -> processResponse(response, endpoint))
						.onErrorResume(e -> {
							logger.error("Error in async call", e);
							return Mono.empty(); // or another fallback
						})
				)
				.doOnTerminate(() -> latch.countDown())
				.subscribe();


		latch.await(900, TimeUnit.MILLISECONDS);
	}

	private Flux<ClientResponse> makeAsyncCall(WebClient webClient, String endpoint) {
		return webClient.get()
				.uri(endpoint)
				.exchange()
				.flux();
	}

	private void processResponse(ClientResponse response, String endpoint) {
		HttpStatusCode httpStatus = response.statusCode();
		if (httpStatus.is2xxSuccessful()) {
			switch (endpoint) {
				case HEALTH_ENDPOINT:
					successCounterHealth.incrementAndGet();
					break;
				case TIMEOUT_ENDPOINT:
					successCounterTimeout.incrementAndGet();
			}
		} else {
			switch (endpoint) {
				case HEALTH_ENDPOINT:
					failureCounterHealth.incrementAndGet();
					break;
				case TIMEOUT_ENDPOINT:
					failureCounterTimeout.incrementAndGet();
					break;
			}
		}
	}

	private void logResults() {
		logger.info("success health count: {} ", successCounterHealth.get());
		logger.info("failure health count: {} ", failureCounterHealth.get());
		logger.info("success timeout count: {} ", successCounterTimeout.get());
		logger.info("failure timeout count: {} ", failureCounterTimeout.get());
	}

}
