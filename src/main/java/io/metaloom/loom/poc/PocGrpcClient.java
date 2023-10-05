package io.metaloom.loom.poc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.loom.poc.proto.GreeterGrpc;
import io.metaloom.loom.poc.proto.HelloReply;
import io.metaloom.loom.poc.proto.HelloRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.grpc.client.GrpcClientResponse;
import io.vertx.grpc.server.auth.JWTGrpcClient;

public class PocGrpcClient {

	public static final Logger log = LoggerFactory.getLogger(PocGrpcClient.class);

	private JWTGrpcClient client;
	private SocketAddress socket;

	public PocGrpcClient(Vertx vertx, String host, int port, String token) {
		this.client = JWTGrpcClient.create(vertx);
		if (token != null) {
			client = client.withCredentials(new TokenCredentials(token));
		}
		this.socket = SocketAddress.inetSocketAddress(port, host);
	}

	public Future<HelloReply> sayHello(String name) throws Throwable {
		return client
			.request(socket, GreeterGrpc.getSayHelloMethod()).compose(request -> {
				request.end(HelloRequest
					.newBuilder()
					.setName(name)
					.build());

				return request.response()
					.compose(GrpcClientResponse::last);
			});
	}

	public Future<HelloReply> sayPublicHello(String name) throws Throwable {
		return client
			.request(socket, GreeterGrpc.getSayHello2Method()).compose(request -> {
				request.end(HelloRequest
					.newBuilder()
					.setName(name)
					.build());

				return request.response()
					.compose(GrpcClientResponse::last);
			});
	}

}
