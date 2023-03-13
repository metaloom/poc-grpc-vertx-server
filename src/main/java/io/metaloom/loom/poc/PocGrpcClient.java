package io.metaloom.loom.poc;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.loom.poc.proto.GreeterGrpc;
import io.metaloom.loom.poc.proto.HelloReply;
import io.metaloom.loom.poc.proto.HelloRequest;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;

public class PocGrpcClient {

	public static final Logger log = LoggerFactory.getLogger(PocGrpcClient.class);

	private GrpcClient client;
	private SocketAddress server;

	public PocGrpcClient(Vertx vertx, String host, int port) {
		this.client = GrpcClient.client(vertx);
		this.server = SocketAddress.inetSocketAddress(port, host);
	}

	public HelloReply sayHello(String name) {
		CompletableFuture<HelloReply> fut = new CompletableFuture<>();
		client
			.request(server, GreeterGrpc.getSayHelloMethod()).compose(request -> {
				request.end(HelloRequest
					.newBuilder()
					.setName(name)
					.build());
				return request.response().compose(response -> response.last());
			}).onFailure(error -> {
				log.error("Request failed", error);
				fut.completeExceptionally(error);
			})
			.onSuccess(reply -> {
				fut.complete(reply);
			});
		return fut.join();
	}

}
