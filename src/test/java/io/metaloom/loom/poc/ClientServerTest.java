package io.metaloom.loom.poc;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class ClientServerTest {

	public static final Logger log = LoggerFactory.getLogger(ClientServerTest.class);

	@Test
	public void testClientServer() throws Throwable {
		Vertx vertx = Vertx.vertx();
		PocGrpcServer server = new PocGrpcServer(vertx);
		server.start();

		PocGrpcClient client = new PocGrpcClient(vertx, "localhost", server.port());
		CompletableFuture<Void> fut = new CompletableFuture<>();
		client.sayHello("Johannes")
			.onFailure(error -> {
				switch (error) {
				case GrpcClientResponseException e -> {
					log.error("Request failed with status " + e.status());
				}
				default -> {
					log.error("Unknown error.", error);
				}
				}
				fut.completeExceptionally(error);
			})
			.onSuccess(reply -> {
				log.info("Reply: " + reply.getMessage());
				fut.complete(null);
			});
		fut.join();
	}
}
