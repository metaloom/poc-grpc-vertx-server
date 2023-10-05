package io.metaloom.loom.poc;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.loom.poc.proto.GreeterGrpc;
import io.metaloom.loom.poc.proto.HelloReply;
import io.metaloom.loom.poc.proto.HelloRequest;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.grpc.server.GrpcServerResponse;
import io.vertx.grpc.server.auth.JWTGrpcServer;

public class PocGrpcServer {

	public static final Logger log = LoggerFactory.getLogger(PocGrpcServer.class);

	private HttpServer server;
	private Vertx vertx;
	private JWTGrpcServer jwtServer;

	public PocGrpcServer(Vertx vertx, Path keystorePath, String keystorePassword) {
		this.vertx = vertx;
		setup(keystorePath, keystorePassword);
	}

	private void setup(Path keystorePath, String keystorePassword) {

		JWTAuthOptions jwtConfig = new JWTAuthOptions().setKeyStore(new KeyStoreOptions().setPath(keystorePath.toString())
			.setPassword(keystorePassword)
			.setType("jceks"));

		JWTAuth jwtAuth = JWTAuth.create(vertx, jwtConfig);

		jwtServer = JWTGrpcServer.create(vertx, jwtAuth);

		// Create the method handler which does require authentication
		jwtServer.callHandler(GreeterGrpc.getSayHelloMethod(), true, request -> {
			User user = request.user();
			request.handler(hello -> {
				log.info("Server got hello request with name {} from {}", hello.getName(), user.subject());
				GrpcServerResponse<HelloRequest, HelloReply> response = request.response();
				HelloReply reply = HelloReply.newBuilder()
					.setMessage("Reply with " + hello.getName())
					.build();
				response.end(reply);
			});
		});

		// Create the public method handler which does not require authentication
		jwtServer.callHandler(GreeterGrpc.getSayHello2Method(), false, request -> {
			User user = request.user();
			request.handler(hello -> {
				log.info("Server got public hello2 request with name {} from {}", hello.getName(), user);
				GrpcServerResponse<HelloRequest, HelloReply> response = request.response();
				HelloReply reply = HelloReply.newBuilder()
					.setMessage("Reply with " + hello.getName())
					.build();
				response.end(reply);
			});
		});

		server = vertx.createHttpServer(new HttpServerOptions().setPort(0)
			.setHost("localhost")).requestHandler(jwtServer);

	}

	public void start() throws Exception {
		log.info("Starting server");
		CompletableFuture<Void> fut = new CompletableFuture<>();
		server.listen(srv -> {
			log.info("Server started and listening on port " + srv.result()
				.actualPort());
			fut.complete(null);
		});
		fut.join();
	}

	public int port() {
		return server.actualPort();
	}

}
