package com.slambyte.util.serverresults;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import java.io.InputStream;
import java.io.FileInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

/**
 * ServerResults is a lightweight OkHttp wrapper class that executes blocking HTTP request
 * and captures the result (success or failure) into a single immutable object.
 * 
 * <p>This API is designed to simplify HTTP usage by avoiding checked exceptions and 
 * exposing response data through getter methods.</p>
 * 
 * <h3>Threading</h3>
 * <p><b>This class performs blocking network calls.</b> 
 * Do Not invoke these methods on the Android main/UI thread.</p>
 * 
 * <h3>Error Handling</h3>
 * <ul>
 *    <li>If the request succeeds, {@link #getResponseCode()} returns the HTTP status code 
 *  and {@link #getResponseText()} returns the server response body.</li>
 *    <li>If request fails due to a network, SSL, or IO error, {@link #getResponseCode()} 
 *  returns {@code -1} and {@link #getResponseText()} returns an error message.</li>
 * </ul>
 * 
 * <h3>Security</h3>
 * <p>Use {@link #getServerResultsUnsafe(String, String, String, Map)} with extreme caution.
 * It disables SSL certificate verification and should only be used for testing or trusted 
 * internal servers.</p>
 */
public final class ServerResults {
  private static OkHttpClient safeClient;
  private static OkHttpClient pinnedClient;

  private final int responseCode;
  private final String responseText;
  private final String exceptionMsg;
  private final Map<String, List<String>> responseHeaders;

  private ServerResults(int code, String text, String exceptionMsg,Map<String, List<String>> responseHeaders) {
    this.responseCode = code;
    this.responseText = text;
    this.exceptionMsg = exceptionMsg;
    this.responseHeaders = responseHeaders;
  }

  /**
   * Returns the shared OkHttpClient instance used for standard (secure) requests.
   *
   * <p>The returned client is immutable and thread-safe. Advanced users may call
   * {@link OkHttpClient#newBuilder()} to create a customized client without affecting
   * the global configuration.</p>
   *
   * @return shared OkHttpClient with SSL certificate verification enabled
   */
  public static synchronized OkHttpClient getSafeClient()  {
    if(safeClient == null)  {
      safeClient = new OkHttpClient();
    }
    return safeClient;
  }

  
  /**
   * Initializes a pinned SSL OkHttpClient that trusts only the specified certificate and hostname.
   *
   * <p>This method must be called before any attempt to retrieve the pinned client via
   * {@link #getPinnedClient()}. The provided hostname should not include the protocol
   * (e.g., "example.com" instead of "https://example.com").</p>
   *
   * <p>The returned OkHttpClient is safe for production and Play Store distribution since
   * it performs certificate pinning and hostname verification.</p>
   *
   * <p>Subsequent calls to this method will have no effect if the pinned client is already
   * initialized.</p>
   *
   * @param certificate path to the PEM/X.509 certificate file to pin
   * @param hostname trusted server hostname (without protocol)
   * @throws IllegalStateException if the pinned client cannot be initialized
   */
  public static synchronized void createPinnedClient(String certificate, String hostname)  {
    if(pinnedClient == null)  {
      try(InputStream certStream = new FileInputStream(certificate))   {
        hostname = hostname.replaceAll("^[a-zA-Z]+://","");
        System.out.println(hostname);
        pinnedClient = SSLUtil.createPinnedClient(certStream,hostname);
      }catch(Exception e) {
        throw new IllegalStateException("Failed to initialize pinned SSL client",e);
      }
    }
  }

  /**
   * Returns the previously initialized pinned SSL OkHttpClient.
   *
   * <p>Throws an IllegalStateException if the pinned client has not been initialized
   * via {@link #createPinnedClient(String, String)}.</p>
   *
   * @return the pinned OkHttpClient instance
   * @throws IllegalStateException if the pinned client has not been initialized
   */
  public static OkHttpClient getPinnedClient()  {
    if(pinnedClient == null)  {
      throw new IllegalStateException(
        "Pinned client is not initialized, call getPinnedClient(certPath, hostname) first."
      );
    }
    return pinnedClient;
  }

  /**
   * Executes an HTTP request using the shared secure client without custom headers.
   *
   * <p>This is a convenience overload of
   * {@link #getServerResults(String, String, String, Map)}.</p>
   *
   * @param url    target URL
   * @param data   request body data (may be {@code null})
   * @param method HTTP method (GET, POST, PUT, PATCH, DELETE)
   *
   * @return a ServerResults instance containing either the response or an error
   */
  public static ServerResults getServerResults(String url, String data, String method) {
    return getServerResults(url, data, method, null, null);
  }

  /**
   * Executes an HTTP request with SSL certificate enabled.
   * 
   * <p>This method never throws exceptions. All errors are captured inside the returned {@link ServerResults} instance.<p>
   * 
   * @param url      Target URL
   * @param data     Request body data (maybe {@code null} for GET requests)
   * @param method   HTTP method (e.g "GET", "POST", "PUT", "DELETE")
   * @param headers  Request headers (may be {@code null})
   * 
   * @return a {@link ServerResults} instance containing either a response or an error message.
   * 
   * @implNote This method performs blocking network operation.
   * 
   */
  public static ServerResults getServerResults(String url, String data, String method,Map<String,String> headers) {
    return executeRequest(getSafeClient(),url,data,method,headers, null);
  }

  /**
   * Executes an HTTP request using the shared secure client with optional headers and content type.
   *
   * <p>This method never throws exceptions. All errors are captured inside the returned {@link ServerResults} instance.</p>
   *
   * @param url      Target URL
   * @param data     Request body data (may be {@code null})
   * @param method   HTTP method (GET, POST, PUT, PATCH, DELETE). Defaults to GET if {@code null}.
   * @param headers  Optional request headers (may be {@code null})
   * @param type     Optional content type for the request body (e.g., MediaType.parse("application/json; charset=utf-8")). If {@code null}, defaults to plain text.
   * @return a {@link ServerResults} instance containing either a response or an error message.
   *
   * @implNote This is a blocking network call. Do not call on Android main/UI thread.
   */
  public static ServerResults getServerResults(String url, String data, String method,Map<String,String> headers, MediaType type) {
    return executeRequest(getSafeClient(),url,data,method,headers, type);
  }

  /**
   * Executes an HTTP request using a caller-provided OkHttpClient.
   *
   * <p>This method allows advanced users to fully customize networking behavior
   * (timeouts, proxies, interceptors, TLS, caching) while still benefiting from
   * the ServerResults unified response model.</p>
   *
   * <p>The provided client must be fully configured before invocation. This method
   * does not modify the client.</p>
   *
   * @param client  OkHttpClient to use for the request
   * @param url     target URL
   * @param data    request body data (may be {@code null})
   * @param method  HTTP method (GET, POST, PUT, PATCH, DELETE)
   * @param headers optional request headers (may be {@code null})
   *
   * @return a ServerResults instance containing either the response or an error
   *
   */
  public static ServerResults getServerResults(OkHttpClient client,String url, String data, String method,Map<String,String> headers) {
    return executeRequest(client,url,data,method,headers, null);
  }

  /**
   * Executes an HTTP request using a user-provided OkHttpClient with optional headers and content type.
   *
   * <p>This method allows advanced users to fully customize networking behavior (timeouts, interceptors, TLS, caching).</p>
   *
   * @param client  OkHttpClient to use for the request
   * @param url     Target URL
   * @param data    Request body data (may be {@code null})
   * @param method  HTTP method (GET, POST, PUT, PATCH, DELETE). Defaults to GET if {@code null}.
   * @param headers Optional request headers (may be {@code null})
   * @param type    Optional content type for the request body. If {@code null}, defaults to plain text.
   * @return a {@link ServerResults} instance containing either a response or an error message.
   *
   * @throws NullPointerException if {@code client} is {@code null}
   *
   * @implNote Blocking network call. Do not invoke on main/UI thread.
   */
  public static ServerResults getServerResults(OkHttpClient client,String url, String data, String method,Map<String,String> headers, MediaType type) {
    return executeRequest(client,url,data,method,headers, type);
  }

  /**
   * Executes an HTTP request using the pinned SSL client.
   *
   * <p>The pinned client must be initialized first with {@link #createPinnedClient(String, String)}.</p>
   *
   * @param url      Target URL
   * @param data     Request body data (may be {@code null})
   * @param method   HTTP method (GET, POST, PUT, PATCH, DELETE). Defaults to GET if {@code null}.
   * @param headers  Optional request headers (may be {@code null})
   * @return a {@link ServerResults} instance containing either a response or an error message.
   *
   * @throws IllegalStateException if the pinned client has not been initialized
   *
   * @implNote Blocking network call. Do not call on main/UI thread.
   */
  public static ServerResults getPinnedServerResults(String url, String data, String method,Map<String,String> headers) {
    return executeRequest(getPinnedClient(),url,data,method,headers, null);
  }

  /**
   * Executes an HTTP request using the pinned SSL client with optional content type.
   *
   * @param url      Target URL
   * @param data     Request body data (may be {@code null})
   * @param method   HTTP method (GET, POST, PUT, PATCH, DELETE). Defaults to GET if {@code null}.
   * @param headers  Optional request headers (may be {@code null})
   * @param type     Optional content type for the request body. If {@code null}, defaults to plain text.
   * @return a {@link ServerResults} instance containing either a response or an error message.
   *
   * @throws IllegalStateException if the pinned client has not been initialized
   *
   * @implNote Blocking network call. Do not call on main/UI thread.
   */
  public static ServerResults getPinnedServerResults(String url, String data, String method,Map<String,String> headers, MediaType type) {
    return executeRequest(getPinnedClient(),url,data,method,headers, type);
  }

  /**
   * Executes a synchronous HTTP request using the provided OkHttpClient.
   *
   * <p>This method is the internal implementation for both {@link #getServerResults(String, String, String, Map)}
   * and {@link #getPinnedServerResults(String, String, String, Map)}. It never throws exceptions;
   * all errors are captured and returned inside the ServerResults object.</p>
   *
   * <p><b>Threading:</b> This method performs a blocking network call. It should not be called on
   * the Android main/UI thread.</p>
   *
   * <p><b>Request Handling:</b></p>
   * <ul>
   *   <li>Supports HTTP methods: GET, POST, PUT, PATCH, DELETE</li>
   *   <li>Request body is included for POST, PUT, PATCH, and optionally DELETE</li>
   *   <li>If request body is null, an empty body is sent for methods that require one</li>
   *   <li>Headers are added if provided</li>
   * </ul>
   *
   * <p><b>Response Handling:</b></p>
   * <ul>
   *   <li>If the request reaches the server successfully, {@link ServerResults#responseCode} is set to the HTTP status code
   *       and {@link ServerResults#responseText} contains the server response body</li>
   *   <li>If a network, SSL, or IO error occurs, {@link ServerResults#responseCode} is -1,
   *       {@link ServerResults#responseText} contains the error message,
   *       and {@link ServerResults#exceptionMsg} contains the full exception string</li>
   * </ul>
   *
   * @param client  OkHttpClient instance to use for the request
   * @param url     Target URL
   * @param data    Request body data (may be null)
   * @param method  HTTP method (GET, POST, PUT, PATCH, DELETE)
   * @param headers Optional request headers (may be null)
   * @return A ServerResults instance containing response code, response body, and exception message if applicable
   */
  private static ServerResults executeRequest(OkHttpClient client,String url,String data,String method,Map<String, String> headers,MediaType mediaType) {
    try {
      Request.Builder builder = new Request.Builder().url(url);

      // Handle body (POST, PUT, PATCH)
      RequestBody requestBody = null;

      if (data != null && !data.isEmpty()) {
        requestBody = RequestBody.create(data, mediaType);
      }

      if(method == null) method = "GET";

      switch (method.toUpperCase()) {
        case "POST":
          builder.post(requestBody != null ? requestBody : emptyBody());
          break;

        case "PUT":
          builder.put(requestBody != null ? requestBody : emptyBody());
          break;

        case "PATCH":
          builder.patch(requestBody != null ? requestBody : emptyBody());
          break;

        case "DELETE":
          // OkHttp DELETE can have a body (optional)
          if (requestBody != null) builder.delete(requestBody);
          else builder.delete();
          break;

        case "GET":
        default:
          builder.get();
          break;
      }

      // Add headers
      if (headers != null) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          builder.addHeader(entry.getKey(), entry.getValue());
        }
      }

      // Build request
      Request request = builder.build();

      // Execute synchronously (blocking)
      try (Response response = client.newCall(request).execute()) {
        String body = response.body() != null ? response.body().string() : null;
        Map<String, List<String>> responseHeaders = response.headers().toMultimap();

        return new ServerResults(response.code(),body,null,responseHeaders);
      }

    } catch (Exception ex) {
      return new ServerResults(-1,null,ex.toString(),null);
    }
  }

  private static RequestBody emptyBody() {
    return RequestBody.create(new byte[0], null);
  }
  
  /**
   * Returns HTTP response code.
   * 
   * @return HTTP status code if the request reached the server, or {@code -1} if a network, SSL,IO error occurred.
   */
  public int getResponseCode() { return responseCode; }

  /**
   * Returns the response text
   * 
   * <ul>
   *    <li>If {@link #getResponseCode()} is {@code >= 0}, this is the server response body.</li>
   *    <li>If {@link #getResponseCode()} is {@code -1}, this is an error message describing the failure.</li>
   * </ul>
   * 
   * @return response body or error message or {@code null} if an exception occurred.
   */
  public String getResponseText() { return responseText; }

  /**
   * Returns the underlying exception message if an exception occurred.
   * 
   * @return exception message, or {@code null} if the request succeeded
   */
  public String getExceptionMessage() { return exceptionMsg; }

  /**
   * Returns the HTTP response headers.
   *
   * <p>The returned map contains header names mapped to a list of values as returned
   * by the server.</p>
   *
   * @return response headers, or {@code null} if the request failed before a response
   *         was received
   */
  public Map<String, List<String>> getResponseHeaders() {
    return responseHeaders;
  }

  /**
   * Indicates whether the request failed due to a network, SSL, or IO error.
   *
   * <p>This method returns {@code true} when the request did not reach the server
   * or could not be completed due to a client-side failure.</p>
   *
   * @return {@code true} if {@link #getResponseCode()} is {@code -1},
   *         {@code false} otherwise
   */
  public boolean isNetworkError() {
      return responseCode == -1;
  }

  /**
   * Indicates whether the HTTP request completed successfully.
   *
   * <p>A request is considered successful if it reached the server and the
   * returned HTTP status code is in the {@code 2xx} range.</p>
   *
   * @return {@code true} if the response code is between {@code 200} and {@code 299},
   *         {@code false} otherwise
   */
  public boolean isSuccess() {
      return responseCode >= 200 && responseCode < 300;
  }
}
