package io.metaloom.loom.poc;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.metaloom.loom.poc.proto.HelloReply;
import io.metaloom.loom.poc.proto.HelloRequest;
import io.metaloom.loom.poc.proto.VertxGreeterGrpc;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.grpc.BlockingServerInterceptor;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

public class PocGrpcServer {

	public static final Logger log = LoggerFactory.getLogger(PocGrpcServer.class);

	private Vertx vertx;
	private VertxServer rpcServer;

	public PocGrpcServer(Vertx vertx) {
		this.vertx = vertx;
		setup();
	}

	private void setup() {
		VertxGreeterGrpc.GreeterVertxImplBase service = new VertxGreeterGrpc.GreeterVertxImplBase() {
			@Override
			public Future<HelloReply> sayHello(HelloRequest request) {
				log.info("Server got hello request with name {}", request.getName());
				return Future.succeededFuture(
					HelloReply.newBuilder()
						.setMessage("Reply with " + request.getName())
						.build());
			}
		};

		ServerInterceptor wrappedAuthInterceptor = BlockingServerInterceptor.wrap(vertx, new AuthInterceptor());
		ServerServiceDefinition authedService = ServerInterceptors.intercept(service, wrappedAuthInterceptor);
		rpcServer = VertxServerBuilder
			.forPort(vertx, 0)
			.addService(authedService)
			.build();
	}

	public void start() throws Exception {
		log.info("Starting server");
		CompletableFuture<Void> fut = new CompletableFuture<>();
		rpcServer.start(srv -> {
			log.info("Server started and listening on port " + rpcServer.getPort());
			fut.complete(null);
		});
		fut.join();
	}

	public int port() {
		return rpcServer.getPort();
	}

}
