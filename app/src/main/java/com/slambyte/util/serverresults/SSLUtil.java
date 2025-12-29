package com.slambyte.util.serverresults;

import okhttp3.*;
import javax.net.ssl.*;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;

import java.io.InputStream;

/**
 * Internal utility class to provide an OkHttpClient instance that disables SSL certificate
 * verification. Not intended for public use.
 */
public final class SSLUtil	{
	private SSLUtil() {}

	public static OkHttpClient createPinnedClient(InputStream certInputStream, String trustedHostname) throws Exception	{
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate cert = cf.generateCertificate(certInputStream);

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null,null);
		keyStore.setCertificateEntry("ca", cert);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(
			TrustManagerFactory.getDefaultAlgorithm()
		);

		tmf.init(keyStore);

		// Install the trust manager
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null,tmf.getTrustManagers(), new java.security.SecureRandom());

		X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];

		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.sslSocketFactory(sslSocketFactory,trustManager);
		builder.hostnameVerifier((hostname, session) -> trustedHostname.equals(hostname));

		return builder.build();
	}
}