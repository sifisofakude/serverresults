package com.slambyte.util.serverresults;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import okhttp3.MediaType;
import okhttp3.Response;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

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

  private String responseText = null;
  
  private int responseCode = -1;
  private String exceptionMsg = null;
  private Response response = null;
  private Map<String, List<String>> responseHeaders = null;

  private ServerResults(int code, Response response, String exceptionMsg) {
    this.responseCode = code;
    this.response = response;
    this.exceptionMsg = exceptionMsg;

    if(response != null)	{
    	this.responseHeaders = response.headers()
    		.toMultimap();
    }
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
   * Initializes a pinned SSL OkHttpClient using a certificate file.
   *
   * <p>This method is deprecated in favor of
   * {@link #createPinnedClient(InputStream, String)} because file paths are
   * not universally available across all platforms and environments,
   * particularly on Android when using assets, resources, or SAF URIs.</p>
   *
   * @param certificate path to the PEM/X.509 certificate file
   * @param hostname trusted server hostname
   *
   * @deprecated Use {@link #createPinnedClient(InputStream, String)} instead.
   */
  @Deprecated
  public static synchronized void createPinnedClient(
  	String certificate, 
  	String hostname
  )  {
    if(pinnedClient == null)  {
      try(InputStream certStream = new FileInputStream(certificate))   {
        hostname = hostname.replaceAll("^[a-zA-Z]+://","");
        pinnedClient = SSLUtil.createPinnedClient(certStream,hostname);
      }catch(Exception e) {
        throw new IllegalStateException("Failed to initialize pinned SSL client",e);
      }
    }
  }
  
  public static synchronized void createPinnedClient(
  	InputStream certificate, 
  	String hostname
  )  {
    if(pinnedClient == null)  {
      try	{
        hostname = hostname.replaceAll("^[a-zA-Z]+://","");
        pinnedClient = SSLUtil.createPinnedClient(certificate,hostname);
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
  public static ServerResults getServerResults(
  	String url, 
  	String data, 
  	String method
  ) {
    return executeRequest(getSafeClient(),url, data, method, null, null);
  }

  /**
   * Creates and executes a multipart file upload request using the shared secure client.
   *
   * <p>The supplied file is streamed directly to the server without loading the
   * entire file into memory. The request is sent as
   * {@code multipart/form-data} with a single form field named {@code file}.</p>
   *
   * <p>If the file cannot be read, does not exist, or does not contain a valid
   * file name, a {@link ServerResults} instance with response code {@code -1}
   * is returned.</p>
   *
   * @param url target URL
   * @param file file descriptor containing the file name, size, and input stream
   * @param method HTTP method to use, typically {@code POST}
   *
   * @return a {@link ServerResults} instance containing the server response or
   *         an error description
   *
   * @implNote This method performs a blocking network operation.
   */
  public static ServerResults getServerResults(
  	String url, 
  	FileUpload file, 
  	String method
  ) {
    return getServerResults(url, file, method, null, null);
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
  public static ServerResults getServerResults(
  	String url, 
  	String data, 
  	String method,
  	Map<String,String> headers
  ) {
    return executeRequest(getSafeClient(),url,data,method,headers, null);
  }

  /**
   * Creates and executes a multipart file upload request using the shared secure client.
   *
   * <p>The supplied file is streamed directly to the server using
   * {@code multipart/form-data}. Request headers may be supplied to support
   * authentication, API keys, or custom server requirements.</p>
   *
   * @param url target URL
   * @param file file descriptor containing the file name, size, and input stream
   * @param method HTTP method to use, typically {@code POST}
   * @param headers optional request headers, may be {@code null}
   *
   * @return a {@link ServerResults} instance containing the server response or
   *         an error description
   *
   * @implNote This method performs a blocking network operation.
   */
  public static ServerResults getServerResults(
  	String url, 
  	FileUpload file, 
  	String method,
  	Map<String,String> headers
  ) {
    return getServerResults(url,file,method,headers, null);
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
  public static ServerResults getServerResults(
  	String url, 
  	String data, 
  	String method,
  	Map<String,String> headers, 
  	MediaType type
  ) {
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
  public static ServerResults getServerResults(
  	OkHttpClient client,
  	String url, 
  	String data, 
  	String method,
  	Map<String,String> headers
  ) {
    return executeRequest(client,url,data,method,headers, null);
  }
  
  // public static ServerResults getServerResults(
  // 	OkHttpClient client,
  // 	String url, 
  // 	RequestBody requestBody, 
  // 	String method,
  // 	Map<String,String> headers
  // ) {
  //   return executeRequest(client,url,requestBody,method,headers, null);
  // }

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
  public static ServerResults getServerResults(
  	OkHttpClient client,
  	String url, 
  	String data, 
  	String method,
  	Map<String,String> headers, 
  	MediaType type
  ) {
    return executeRequest(client,url,data,method,headers, type);
  }

	/**
	 * Creates and executes a multipart file upload request using the shared secure client.
	 *
	 * <p>The file is transmitted using {@code multipart/form-data} and streamed
	 * directly from the provided input stream.</p>
	 *
	 * <p>The supplied content type parameter is currently ignored for the uploaded
	 * file body, which is sent as {@code application/octet-stream}.</p>
	 *
	 * @param url target URL
	 * @param file file descriptor containing the file name, size, and input stream
	 * @param method HTTP method to use, typically {@code POST}
	 * @param headers optional request headers, may be {@code null}
	 * @param type optional content type parameter
	 *
	 * @return a {@link ServerResults} instance containing the server response or
	 *         an error description
	 *
	 * @implNote This method performs a blocking network operation.
	 */
  public static ServerResults getServerResults(
  	String url,
  	FileUpload file, 
  	String method,
  	Map<String,String> headers, 
  	MediaType type
  ) {
  	String errorMessage = null;
  	if(file == null)	{
  		errorMessage = "Could not find file to upload";
  	}

  	if(file != null && file.getFileName() == null && errorMessage == null)	{
  		errorMessage = "File name not provided";
  	}

  	if(file != null && file.getInputStream() == null && errorMessage == null)	{
  		errorMessage = "Unreadable file";
  	}

  	if(errorMessage != null)	{
  		return new ServerResults(-1,null,errorMessage);
  	}

  	RequestBody streamBody = new RequestBody()	{
  		@Override
  		public MediaType contentType()	{
  			return type;
  		}
  		

  		@Override
  		public long contentLength()	{
  			return file.getFileSize();
  		}

  		@Override
  		public void writeTo(BufferedSink sink) throws IOException	{
  			try(Source source = Okio.source(file.getInputStream()))	{
  				sink.writeAll(source);
  			}
  		}
  	};

  	RequestBody requestBody = new MultipartBody.Builder()
  		.setType(MultipartBody.FORM)
  		.addFormDataPart("file",file.getFileName(),streamBody)
  		.build();
  		
    return executeRequest(getSafeClient(),url,requestBody,method,headers,type);
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
  public static ServerResults getPinnedServerResults(
  	String url, String data, 
  	String method,
  	Map<String,String> headers
  ) {
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
  public static ServerResults getPinnedServerResults(
  	String url, 
  	String data, 
  	String method,
  	Map<String,String> headers, 
  	MediaType type
  ) {
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
  private static ServerResults executeRequest(
  	OkHttpClient client,
  	String url,
  	String data,
  	String method,
  	Map<String, String> headers,
  	MediaType mediaType
  ) {
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

        case "HEAD":
        	builder.head();
        	break;

        case "GET":
        default:
          builder.get();
          break;
      }

      // Add headers
      if (headers != null) {
        for (Object key : headers.keySet()) {
        	String value = headers.get(key);
          builder.addHeader((String) key, value);
        }
      }

      // Build request
      Request request = builder.build();

      // Execute synchronously (blocking)
      Response response = client.newCall(request).execute();

      return new ServerResults(response.code(),response,null);

    } catch (Exception ex) {
      return new ServerResults(-1,null,ex.toString());
    }
  }

  
  private static ServerResults executeRequest(
  	OkHttpClient client,
  	String url,
  	RequestBody requestBody,
  	String method,
  	Map<String, String> headers,
  	MediaType mediaType
  ) {
    try {
      Request.Builder builder = new Request.Builder().url(url);

      if(method == null) method = "POST";

      switch (method.toUpperCase()) {
        case "PUT":
          builder.put(requestBody != null ? requestBody : emptyBody());
          break;

        case "PATCH":
          builder.patch(requestBody != null ? requestBody : emptyBody());
          break;

        case "POST":
        default:
          builder.post(requestBody != null ? requestBody : emptyBody());
      }

      // Add headers
      if (headers != null) {
        for (Object rawEntry : headers.entrySet()) {
        	if(rawEntry instanceof Map.Entry)	{
	        	Map.Entry<String, String> entry = (Map.Entry<String,String>) 
	        		rawEntry;
	        		
	          builder.addHeader(entry.getKey(), entry.getValue());
          }
        }
      }

      // Build request
      Request request = builder.build();

      // Execute synchronously (blocking)
      Response response = client.newCall(request).execute();

      return new ServerResults(response.code(),response,null);

    } catch (Exception ex) {
      return new ServerResults(-1,null,ex.toString());
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
   * Returns the raw OkHttp {@link Response} object associated with this request.
   *
   * <p>This method may return {@code null} if the request failed before a
   * response was received.</p>
   *
   * <p>Advanced users can access response metadata not exposed by
   * {@link ServerResults}.</p>
   *
   * @return underlying OkHttp response or {@code null}
   */
  public Response getResponse()	{
  	return response;
  }

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
  public String getResponseText() {
  	if(responseText == null && response != null)	{
  		if(responseCode == 200)	{
  			if(response.body() != null)	{
  				try	{
  					responseText = response.body().string();
  				}catch(Exception e) {}
  			}
  		}
			response.close();
			response = null;
  	}
		
  	return responseText;
  }

	/**
	 * Returns the response body as a stream and writes it to the provided output stream.
	 *
	 * <p>This method is intended for downloading binary content such as files,
	 * images, archives, or media streams.</p>
	 *
	 * <p>The download succeeds only when the HTTP response code is {@code 200}.
	 * The provided output stream is flushed and closed automatically after the
	 * download completes.</p>
	 *
	 * @param output destination stream
	 *
	 * @return {@code true} if the file was successfully downloaded and written,
	 *         {@code false} otherwise
	 */
  public boolean downloadFile(OutputStream output)	{
  	boolean complete = false;
  	
  	if(response != null && responseCode == 200)	{
  		try(InputStream in = response.body().byteStream())	{
  			int bufSize = 8*1024;
  			byte[] buffer = new byte[bufSize];
  			int bytesRead = -1;
  			while((bytesRead = in.read(buffer)) != -1)	{
  				output.write(buffer,0,bytesRead);
  			}
  			output.flush();
  			output.close();

  			complete = true;
  		}catch(Exception e) {}
  	}
  	return complete;
  }

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
