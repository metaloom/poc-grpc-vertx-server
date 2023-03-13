package io.metaloom.loom.poc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.loom.poc.proto.HelloReply;
import io.vertx.core.Vertx;

public class ClientServerTest {

	public static final Logger log = LoggerFactory.getLogger(ClientServerTest.class);

	@Test
	public void testClientServer() throws Exception {
		Vertx vertx = Vertx.vertx();
		PocGrpcServer server = new PocGrpcServer(vertx);
		server.start();

		PocGrpcClient client = new PocGrpcClient(vertx, "localhost", server.port());
		HelloReply reply = client.sayHello("Johannes");
		log.info("Got message from server: {}", reply.getMessage());

	}
}
