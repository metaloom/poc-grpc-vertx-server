package io.metaloom.loom.poc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.grpc.common.GrpcException;
import io.vertx.grpc.common.GrpcStatus;

public class ClientServerTest {

	public static final Logger log = LoggerFactory.getLogger(ClientServerTest.class);
	public static final Path KEYSTORE_PATH = Path.of("target/keystore.jceks");
	public static final String KEYSTORE_PASSWORD = "finger";
	public Vertx vertx = Vertx.vertx();

	@BeforeAll
	public static void setupKeystore() throws Exception {
		// Initialize Keystore
		if (Files.exists(KEYSTORE_PATH)) {
			Files.delete(KEYSTORE_PATH);
		}
		KeyStoreHelper.gen(KEYSTORE_PATH.toString(), KEYSTORE_PASSWORD);
	}

	@Test
	public void testInvalidAuth() throws Throwable {
		// 1. Setup server
		PocGrpcServer server = new PocGrpcServer(vertx, KEYSTORE_PATH, KEYSTORE_PASSWORD);
		server.start();

		// 2. Prepare client with an invalid token auth
		PocGrpcClient client = new PocGrpcClient(vertx, "localhost", server.port(), "invalid");
		CompletableFuture<Void> fut = new CompletableFuture<>();
		client.sayHello("Johannes")
			.onFailure(error -> {
				switch (error) {
				case GrpcException e -> {
					log.error("Request failed with status {} - http code: {}", e.status().name(), e.response().statusCode());
					assertEquals(GrpcStatus.UNAUTHENTICATED, e.status(), "The request should failed with code UNAUTHENTICATED");
					fut.complete(null);
					return;
				}
				default -> {
					log.error("Unknown error.", error);
				}
				}
				fut.completeExceptionally(error);
			})
			.onSuccess(reply -> {
				log.info("Reply: " + reply.getMessage());
				fut.completeExceptionally(new Exception("Test should have failed"));
			});

		fut.join();

	}

	@Test
	public void testPublicServerMethod() throws Throwable {
		// 1. Setup server
		PocGrpcServer server = new PocGrpcServer(vertx, KEYSTORE_PATH, KEYSTORE_PASSWORD);
		server.start();

		PocGrpcClient client = new PocGrpcClient(vertx, "localhost", server.port(), null);
		CompletableFuture<Void> fut = new CompletableFuture<>();
		client.sayPublicHello("Johannes")
			.onFailure(error -> {
				switch (error) {
				case GrpcException e -> {
					log.error("Request failed with status {}", e.status().name());
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

	@Test
	public void testClientServer() throws Throwable {
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
				case GrpcException e -> {
					log.error("Request failed with status {}", e.status().name());
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
