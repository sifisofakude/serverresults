package com.slambyte.util.serverresults;

import okhttp3.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * Internal utility class to provide an OkHttpClient instance that disables SSL certificate
 * verification. Not intended for public use.
 */
public final class SSLUtil	{
	private SSLUtil() {}

	public static OkHttpClient createUnsafeClient()	{
		try	{
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[]	{
				new X509TrustManager()	{
					public X509Certificate[] getAcceptedIssuers()	{
						return new X509Certificate[]{}	;
					}

					public void checkClientTrusted(X509Certificate[] certs,String authType)	{}

					public void checkServerTrusted(X509Certificate[] certs,String authType)	{}
				}
			};

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("TSL");
			sslContext.init(null,trustAllCerts, new java.security.SecureRandom());

			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory,(X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);

			return builder.build();
		}catch(Exception e)	{
			return null;
		}
	}
}