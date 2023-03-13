package io.metaloom.loom.poc;

import io.vertx.grpc.client.GrpcClientResponse;
import io.vertx.grpc.common.GrpcStatus;

public final class GrpcClientUtils {

	private GrpcClientUtils() {
	}

	public static <T, U> GrpcClientResponse<T, U> errorMapper(GrpcClientResponse<T, U> response) {
		GrpcStatus status = response.status();
		if (status != null && status != GrpcStatus.OK) {
			throw new GrpcClientResponseException(status);
		} else {
			return response;
		}
	}
}
