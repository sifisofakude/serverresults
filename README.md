# ServerResults

ServerResults is a lightweight, immutable wrapper around OkHttp
 that simplifies blocking HTTP requests. It captures success or failure in a single object, exposing response code, body, headers, and error messages without forcing callers to handle exceptions.

---

## Features

* Simple API: `getServerResults()` and `getServerResultsUnsafe()`

* Fully blocking requests (threading warning: do not call on Android main/UI thread)

* Built-in handling for network, SSL, and IO errors

* Optional headers and request body support for GET, POST, PUT, PATCH, DELETE

* Access to response headers

* Advanced user support: retrieve the `OkHttpClient` and customize via `.newBuilder()`

* Convenience helpers: `isSuccess()` and `isNetworkError()`

---

## Installation

Include the `ServerResults` source in your project. Requires OkHttp 4.x.

Example Gradle dependency for OkHttp (if not already present):

```gradle
implementation("com.squareup.okhttp3:okhttp:4.11.0")
```

Then include the `com.slambyte.util.serverresults` package in your source tree.

---

## Usage
### Basic request
```java
import com.slambyte.util.serverresults.ServerResults;

ServerResults result = ServerResults.getServerResults(
        "https://api.example.com/data",
        null,
        "GET"
);

if (result.isNetworkError()) {
    System.out.println("Network error: " + result.getExceptionMessage());
} else if (result.isSuccess()) {
    System.out.println("Response: " + result.getResponseText());
} else {
    System.out.println("HTTP error: " + result.getResponseCode());
}
```
---

### POST request with headers
```java
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer <token>");
headers.put("Content-Type", "application/json");

String jsonData = "{\"name\":\"John\"}";

ServerResults result = ServerResults.getServerResults(
        "https://api.example.com/create",
        jsonData,
        "POST",
        headers
);
```
---

### Unsafe request (SSL verification disabled)

⚠️ Use only for testing or trusted internal servers. Vulnerable to man-in-the-middle attacks.

```java
ServerResults unsafeResult = ServerResults.getServerResultsUnsafe(
        "https://self-signed.example.com",
        null,
        "GET",
        null
);
```
---

### Advanced: custom OkHttp client
```java
OkHttpClient client = ServerResults.getSafeClient()
        .newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build();

ServerResults result = ServerResults.getServerResults(
        client,
        "https://api.example.com/data",
        null,
        "GET",
        null
);
```

### API
| **Method** |	**Description** |
|------------|------------------|
| `getServerResults(String url, String data, String method)` |	Sends a blocking HTTP request using the secure client|
| `getServerResults(String url, String data, String method, Map<String,String> headers)`  |	Sends a blocking HTTP request with headers |
| `getServerResults(OkHttpClient client, String url, String data, String method, Map<String,String> headers)` |	Uses a custom client |
| `getServerResultsUnsafe(String url, String data, String method, Map<String,String> headers)` |	Sends request with SSL verification disabled (unsafe) |
| `getResponseCode()` |	Returns HTTP status code or -1 for network/IO errors |
| `getResponseText()` |	Returns response body or error message |
| `getExceptionMessage()` |	Returns underlying exception message if a failure occurred |
| `getResponseHeaders()` |	Returns response headers as Map<String, List<String>> |
| `isNetworkError()` |	Returns true if the request failed before reaching the server |
| `isSuccess()` |	Returns true if the response code is 2xx |

---

## Threading

All methods perform blocking network calls. Do not call on Android main/UI thread. Use your own threads, Executors, or coroutines if necessary.

---

## Error Handling

* Network, SSL, or IO errors result in `responseCode = -1` and `responseText` containing an error message.

* HTTP errors (non-2xx) are returned normally with the response code and body.

* Unsafe client creation failure throws `IllegalStateException`.

---

## Security

* `getServerResults()` uses a standard OkHttp client with full SSL verification.

* `getServerResultsUnsafe()` disables SSL certificate and hostname verification — use with caution.

---

## Contributing

We welcome contributions! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to report issues, suggest features, and submit pull requests.


## License

[MIT License](LICENSE) — free to use and modify.