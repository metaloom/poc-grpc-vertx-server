package io.metaloom.loom.poc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.loom.poc.proto.GreeterGrpc;
import io.metaloom.loom.poc.proto.HelloReply;
import io.metaloom.loom.poc.proto.HelloRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientResponse;

public class PocGrpcClient {

	public static final Logger log = LoggerFactory.getLogger(PocGrpcClient.class);

	private GrpcClient client;
	private SocketAddress server;

	public PocGrpcClient(Vertx vertx, String host, int port) {
		this.client = GrpcClient.client(vertx);
		this.server = SocketAddress.inetSocketAddress(port, host);
	}

	public Future<HelloReply> sayHello(String name) throws Throwable {
		return client
			.request(server, GreeterGrpc.getSayHelloMethod()).compose(request -> {
				request.headers().add("Test", "testvalue");
				request.end(HelloRequest
					.newBuilder()
					.setName(name)
					.build());

				return request.response()
					.map(GrpcClientUtils::errorMapper)
					.compose(GrpcClientResponse::last);
			});
	}

}
