package io.metaloom.loom.poc;

import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class AuthInterceptor implements ServerInterceptor {

	public static final Key<String> AUTH_HEADER_KEY = Key.of("test", Metadata.ASCII_STRING_MARSHALLER);

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
		try {
			System.out.println("Test:" + metadata.get(AUTH_HEADER_KEY));
			//call.close(Status.UNAUTHENTICATED.withDescription("Auth Token Required"), metadata);
			return next.startCall(call, metadata);
		} finally {
			// call.close(Status.UNAUTHENTICATED.withDescription("Auth Token Required"), metadata);
		}
	}

}
