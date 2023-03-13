package io.metaloom.loom.poc;

import io.metaloom.loom.poc.proto.HelloReply;
import io.metaloom.loom.poc.proto.HelloRequest;
import io.metaloom.loom.poc.proto.VertxGreeterGrpc;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import io.vertx.rxjava3.SingleHelper;
import io.vertx.rxjava3.core.Vertx;

public class RxPocGrpcServer {

	private Vertx vertx;

	public RxPocGrpcServer(Vertx vertx) {
		this.vertx = vertx;
	}

	public void start() throws Exception {
		VertxGreeterGrpc.GreeterVertxImplBase service = new VertxGreeterGrpc.GreeterVertxImplBase() {
			@Override
			public Future<HelloReply> sayHello(HelloRequest request) {
				return SingleHelper.toFuture(rxSayHello(request));
			}

			@Override
			public Future<HelloReply> sayHello2(HelloRequest request) {
				return SingleHelper.toFuture(rxSayHello(request));
			}
		};

		VertxServer rpcServer = VertxServerBuilder
			.forPort(vertx.getDelegate(), 4444)
			.addService(service)
			.build();

		rpcServer.start();
	}

	private Single<HelloReply> rxSayHello(HelloRequest request) {
		return Single.create(sub -> {
			HelloReply reply = HelloReply.newBuilder()
				.setMessage("Reply with " + request.getName())
				.build();
			sub.onSuccess(reply);
		});
	}

	public static void main(String[] args) throws Exception {
		new RxPocGrpcServer(Vertx.vertx()).start();
	}

}
