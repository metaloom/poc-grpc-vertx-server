package io.metaloom.loom.poc;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

public class IssueToken {

	public static String issueToken(Vertx vertx, Path keystorePath, String password) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		JWTAuthOptions config = new JWTAuthOptions()
			.setKeyStore(new KeyStoreOptions()
				.setPath(keystorePath.toString())
				.setPassword(password)
				.setType("jceks"));

		JWTAuth provider = JWTAuth.create(vertx, config);
		String token = provider.generateToken(new JsonObject().put("sub", "johannes"), new JWTOptions().setIgnoreExpiration(true));
		return token;
	}

}
