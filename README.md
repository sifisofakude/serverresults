# ServerResults

**ServerResults** is a lightweight Java utility that wraps **OkHttp** and executes **blocking HTTP requests**,
capturing the result (success or failure) in a single immutable object.

It is designed for developers who want:

* Simple synchronous HTTP calls
* No checked exceptions
* Clear success vs networkfailure handling
* Full control over threading

---

## Features

* Simple blocking HTTP API
* No checked exeptions
* Immutable result object
* HTTP status code + response body access
* Network error detection
* Optional SSL certificate pinning
* Android & JVM compatible
* PlayStore compliant

---

## Important: Blocking calls

All **ServerResults** request methods perform blocking network operations.

You must not call them on:

* Android main/UI thread
* Perfomance-critical threads

Threading is intentionally left to the user so the library remains flexible and framework-agnostic.

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

## Response Handling

### Success

* `isSuccess()` returns `true`
* `getResponseCode()` returns HTTP status code
* `getResponseText()` contains the response body

### Network/IO Error

* `isNetworkError()` returns `true`
* `getResponseCode()` returns `-1`
* `getExceptionMessage()` contains the error description

---

## SSL & Security

### Removed Unsafe APIs

The following methods where **removed**:

* `getServerResultsUnsafe(...)`
* `getUnsafeClient(...)`

**Why?**

* They disabled SSL verification entirely
* Their presence in compiled APKs  can cause **Google PlayStore rejection**
* Even unused unsafe SSL code may trigger security scans

---

## Recommended: Certificate pinning

ServerResults now supports **certificate-pinned OkHttp clients**, which are:

* Secure
* Production-ready
* PlayStore compliant

### Initializing a pinned client
```java
OkHttpClient client = ServerResults.getPinnedClient(
    "/path/to/certificate.pem",
    "api.example.com"
);
```

Once initialized, you can reuse it:
```java
OkHttpClient client = ServerResults.getPinnedClient();
```
if the pinned client is accessed before initialization,an `IllegalException` is thrown.

---

### OkHttp configuration

Advanced users can  customize the client

```java
OkHttpClient client = ServerResults.getPinnedClient()
    .newBuilder()
    .callTimeout(10,TimeUnit.SECONDS)
    .build();
```

ServerResults does not hide OkHttp - it embraces it.

---

## Thread Safety

* `ServerResults` itself is stateless and thread-safe
* OkHttp clients are shared and safe to reuse
* Thread management is the responsibility of the caller

---

### API
| **Method** |	**Description** |
|------------|------------------|
| `getSafeClient()` |	Returns `OkHttpClient` |
| `getPinnedClient()` |   Returns previously initialized `OkHttpClient` with custom certificate |
| `getServerResults(String url, String data, String method)` |  Sends a blocking HTTP request using the secure client|
| `getServerResults(String url, String data, String method, Map<String,String> headers)`  |	Sends a blocking HTTP request with headers |
| `getServerResults(OkHttpClient client, String url, String data, String method, Map<String,String> headers)` |	Uses a custom client |
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

## Versioning & Compatibility

**Unsafe SSL APIs have been permanently removed**

  * `getServerResultsUnsafe(...)`

  * `getUnsafeClient()`

* This change **introduces a breaking API change** and is released as v2.0.0

* **v1.x is depracated** and should not be used for new projects

**Starting from v2.0.0, ServerResults:**
  
  * Does not include any unsafe SSL code

  * Is **Google PlayStore compliant**

  * Uses **certificate pinning** as the only SSL customization mechanism

* Application that relied on unsafe SSL  behavior **must migrate** to pinned certificates befor upgrading

Refer to `CHANGELOG.md` for the full list of changes

---

## Contributing

We welcome contributions! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to report issues, suggest features, and submit pull requests.


## License

[MIT License](LICENSE) — free to use and modify.