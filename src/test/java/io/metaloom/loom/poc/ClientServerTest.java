package io.metaloom.loom.poc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;

public class ClientServerTest {

	public static final Logger log = LoggerFactory.getLogger(ClientServerTest.class);
	public static final Path KEYSTORE_PATH = Path.of("target/keystore.jceks");
	public static final String KEYSTORE_PASSWORD = "finger";

	@BeforeAll
	public static void setupKeystore() throws Exception {
		// Initialize Keystore
		if (Files.exists(KEYSTORE_PATH)) {
			Files.delete(KEYSTORE_PATH);
		}
		KeyStoreHelper.gen(KEYSTORE_PATH.toString(), KEYSTORE_PASSWORD);
	}

	@Test
	public void testClientServer() throws Throwable {
		Vertx vertx = Vertx.vertx();
		String token = IssueToken.issueToken(vertx, KEYSTORE_PATH, KEYSTORE_PASSWORD);


		// 1. Setup server
		PocGrpcServer server = new PocGrpcServer(vertx, KEYSTORE_PATH, KEYSTORE_PASSWORD);
		server.start();

		// 2. Prepare client with token auth
		System.out.println("Using token: " + token);

		PocGrpcClient client = new PocGrpcClient(vertx, "localhost", server.port(), token);
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
