# ServerResults

<<<<<<< HEAD
**ServerResults** is a lightweight Java library built on top of OkHttp
that executes **blocking HTTP requests** and captures the result
(success or failure) in a single object.

It is designed for developers who want:

-   Simple synchronous HTTP calls
-   No checked exceptions
-   Unified success and failure handling
-   Optional SSL certificate pinning
-   Multipart file uploads
-   Streaming file downloads
-   Direct access to the underlying OkHttp `Response`

------------------------------------------------------------------------

# Features

-   Simple blocking HTTP API
-   No checked exceptions
-   HTTP status code, headers and body access
-   Direct access to the underlying OkHttp `Response`
-   Network error detection
-   Multipart file uploads
-   Streaming file downloads
-   Optional SSL certificate pinning
-   Command-line (CLI) support
-   Android & JVM compatible
-   Play Store compliant
-   Shared reusable `OkHttpClient` instances

------------------------------------------------------------------------

# Important: Blocking Calls

All request methods are **blocking**.

Do **not** invoke them on the Android main thread.

Thread management is intentionally left to the caller, allowing
integration with Java threads, Executors, Kotlin coroutines, RxJava, or
any other concurrency framework.

------------------------------------------------------------------------

# Installation

``` gradle
implementation("io.github.sifisofakude.serverresults:serverresults:3.0.0")
```

Import:

``` java
import io.github.sifisofakude.net.serverresults.*;
```

------------------------------------------------------------------------

# Basic Usage

## GET

``` java
ServerResults result =
    ServerResults.getServerResults(
        "https://api.example.com/data",
        null,
        "GET"
    );
=======
**ServerResults** is a lightweight Java utility built on top of OkHttp that executes **blocking HTTP requests** and captures the result (success or failure) in a single object.

It is designed for developers who want:

- Simple synchronous HTTP calls
- No checked exceptions
- Unified success and failure handling
- Optional certificate pinning
- Direct access to OkHttp when needed

---

## Features

- Simple blocking HTTP API
- No checked exceptions
- HTTP status code access
- Response body access
- Response header access
- Network error detection
- Multipart file uploads
- Streaming file downloads
- Optional SSL certificate pinning
- Android & JVM compatible
- Play Store compliant
- Reuses shared OkHttp clients

---

## Important: Blocking Calls

All request methods perform blocking network operations.

Do **not** call them on:

- Android main/UI thread
- Performance-critical threads

Thread management is intentionally left to the caller so the library remains framework-agnostic.

---

## Installation

Add OkHttp to your project:

```gradle
implementation("com.squareup.okhttp3:okhttp:4.11.0")
```

Then include the `com.slambyte.util.serverresults` package in your source tree.

---

# Basic Usage

## GET Request

```java
ServerResults result = ServerResults.getServerResults(
    "https://api.example.com/data",
    null,
    "GET"
);
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548

if(result.isNetworkError()) {
    System.out.println(result.getExceptionMessage());
}
else if(result.isSuccess()) {
    System.out.println(result.getResponseText());
}
<<<<<<< HEAD
```

## POST JSON

``` java
=======
else {
    System.out.println("HTTP Error: " + result.getResponseCode());
}
```

---

## POST JSON

```java
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548
MediaType json =
    MediaType.parse("application/json; charset=utf-8");

ServerResults result =
    ServerResults.getServerResults(
<<<<<<< HEAD
        url,
=======
        "https://api.example.com/users",
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548
        "{\"name\":\"John\"}",
        "POST",
        null,
        json
    );
```

<<<<<<< HEAD
## Custom Headers

``` java
Map<String,String> headers = new HashMap<>();
headers.put("Authorization","Bearer token");
=======
---

## Custom Headers

```java
Map<String,String> headers = new HashMap<>();

headers.put(
    "Authorization",
    "Bearer token"
);
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548

ServerResults result =
    ServerResults.getServerResults(
        url,
        null,
        "GET",
        headers
    );
```

<<<<<<< HEAD
------------------------------------------------------------------------

# Multipart Uploads

``` java
try(FileUpload upload =
        new FileUpload(
            "photo.jpg",
            fileSize,
            inputStream)) {
=======
---

# File Uploads

ServerResults supports streaming multipart file uploads.

```java
try(FileUpload upload =
    new FileUpload(
        "image.jpg",
        fileSize,
        inputStream
    )) {
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548

    ServerResults result =
        ServerResults.getServerResults(
            uploadUrl,
            upload,
            "POST"
        );
}
```

<<<<<<< HEAD
Uploads are streamed directly from the supplied `InputStream`.

------------------------------------------------------------------------

# Streaming Downloads

``` java
=======
Files are streamed directly from the provided InputStream and are not fully loaded into memory.

---

# File Downloads

Large files can be downloaded directly to an OutputStream.

```java
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548
ServerResults result =
    ServerResults.getServerResults(
        fileUrl,
        null,
        "GET"
    );

try(FileOutputStream out =
        new FileOutputStream("archive.zip")) {

<<<<<<< HEAD
    result.downloadFile(out);
}
```

------------------------------------------------------------------------

# Response Handling

Useful methods:

-   `getResponseCode()`
-   `getResponseText()`
-   `getResponseHeaders()`
-   `getResponse()`
-   `getExceptionMessage()`
-   `isSuccess()`
-   `isNetworkError()`

------------------------------------------------------------------------

# SSL Certificate Pinning

Recommended:

``` java
=======
    boolean success =
        result.downloadFile(out);
}
```

This avoids loading the entire response into memory.

---

# Response Handling

## Success

```java
result.isSuccess();
```

Returns `true` when the HTTP response code is between `200` and `299`.

Useful methods:

```java
result.getResponseCode();
result.getResponseText();
result.getResponseHeaders();
```

---

## Network Errors

```java
result.isNetworkError();
```

Returns `true` when a network, SSL, or IO failure occurred.

In this case:

```java
result.getResponseCode() == -1
```

and

```java
result.getExceptionMessage()
```

contains the error description.

---

# SSL Certificate Pinning

ServerResults supports certificate-pinned OkHttp clients.

Certificate pinning is:

- Secure
- Production-ready
- Play Store compliant

---

## Initialize Pinned Client

Using an InputStream (recommended):

```java
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548
try(InputStream cert =
        context.getAssets().open("certificate.pem")) {

    ServerResults.createPinnedClient(
        cert,
        "api.example.com"
    );
}
```

<<<<<<< HEAD
Deprecated compatibility overload:

``` java
=======
Using a certificate file path:

```java
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548
ServerResults.createPinnedClient(
    "/path/to/certificate.pem",
    "api.example.com"
);
```

<<<<<<< HEAD
Execute requests using:

``` java
ServerResults.getPinnedServerResults(
    url,
    null,
    "GET",
    null
);
```

------------------------------------------------------------------------

# Advanced OkHttp

``` java
OkHttpClient client =
    ServerResults.getSafeClient()
        .newBuilder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build();
```

------------------------------------------------------------------------

# CLI

``` bash
java -jar serverresults.jar --help
```

------------------------------------------------------------------------

# Supported HTTP Methods

-   GET
-   POST
-   PUT
-   PATCH
-   DELETE
-   HEAD

------------------------------------------------------------------------

# API Overview

  Method                          Description
  ------------------------------- -----------------------------------------
  `getSafeClient()`               Returns the shared secure client
  `createPinnedClient(...)`       Initializes a certificate-pinned client
  `getPinnedClient()`             Returns the pinned client
  `getServerResults(...)`         Executes a blocking request
  `getPinnedServerResults(...)`   Executes a pinned request
  `downloadFile(OutputStream)`    Streams a response body
  `getResponse()`                 Returns the raw OkHttp Response
  `getResponseText()`             Returns the response body
  `getResponseHeaders()`          Returns response headers
  `getExceptionMessage()`         Returns network error details
  `isSuccess()`                   True for 2xx responses
  `isNetworkError()`              True when no HTTP response was received

------------------------------------------------------------------------

# Security

ServerResults contains **no unsafe SSL implementation**.

HTTPS requests use the platform's default TLS configuration. Certificate
pinning is available for applications requiring additional server
verification.

------------------------------------------------------------------------

# Version Compatibility

## v3.x

-   Maven Central release
-   Package renamed to `io.github.sifisofakude.net.serverresults`
-   CLI support
-   Raw `Response` access
-   Improved documentation

## v2.x

-   Certificate pinning
-   Multipart uploads
-   Streaming downloads
-   Unsafe SSL APIs removed

## v1.x

Legacy release. Upgrade is recommended.

------------------------------------------------------------------------

# Contributing

Issues and pull requests are welcome.

------------------------------------------------------------------------

# License

MIT License.
=======
---

## Access Pinned Client

```java
OkHttpClient client =
    ServerResults.getPinnedClient();
```

Attempting to access the pinned client before initialization throws:

```java
IllegalStateException
```

---

## Using the Pinned Client

```java
ServerResults result =
    ServerResults.getPinnedServerResults(
        url,
        null,
        "GET",
        null
    );
```

---

# Deprecated APIs

## createPinnedClient(String certificate, String hostname)

**Deprecated since v2.1.0**

Use:

```java
ServerResults.createPinnedClient(
    certificateInputStream,
    "api.example.com"
);
```

instead.

The InputStream-based API works with:

- Android assets
- Android raw resources
- SAF documents
- Classpath resources
- Regular files

The file-path overload remains available for compatibility but is no longer recommended for new code.

---

# Advanced OkHttp Usage

ServerResults does not hide OkHttp.

The shared clients can be customized using OkHttp's builder API.

```java
OkHttpClient customClient =
    ServerResults.getSafeClient()
        .newBuilder()
        .callTimeout(
            10,
            TimeUnit.SECONDS
        )
        .build();
```

Then:

```java
ServerResults result =
    ServerResults.getServerResults(
        customClient,
        url,
        null,
        "GET",
        null
    );
```

---

# Supported HTTP Methods

ServerResults supports:

- GET
- POST
- PUT
- PATCH
- DELETE
- HEAD

---

# Thread Safety

- Shared OkHttp clients are thread-safe
- ServerResults instances are independent per request
- Thread management remains the caller's responsibility

---

# API Overview

| Method | Description |
|----------|----------|
| `getSafeClient()` | Returns the shared secure OkHttpClient |
| `createPinnedClient(InputStream,String)` | Creates a pinned client using a certificate stream |
| `createPinnedClient(String,String)` ⚠️ Deprecated | Creates a pinned client using a certificate file path |
| `getPinnedClient()` | Returns the initialized pinned client |
| `getServerResults(...)` | Executes a blocking HTTP request |
| `getPinnedServerResults(...)` | Executes a blocking request using the pinned client |
| `downloadFile(OutputStream)` | Streams the response body to an OutputStream |
| `getResponseCode()` | Returns HTTP status code |
| `getResponseText()` | Returns response body |
| `getExceptionMessage()` | Returns network failure information |
| `getResponseHeaders()` | Returns response headers |
| `isNetworkError()` | Returns true if the request failed before reaching the server |
| `isSuccess()` | Returns true for HTTP 2xx responses |

---

# Security

ServerResults contains **no unsafe SSL implementations**.

Unsafe SSL bypass APIs were permanently removed because:

- They disable certificate validation
- They may trigger Play Store security scans
- They encourage insecure production deployments

Certificate pinning is the only supported SSL customization mechanism.

---

# Version Compatibility

## v2.1.x

- Deprecated `createPinnedClient(String, String)`
- Recommended `createPinnedClient(InputStream, String)` for new code

## v2.0.x

- Unsafe SSL APIs removed
- Certificate pinning added
- Multipart file uploads added
- Streaming downloads added
- Play Store compliant

## v1.x

Deprecated and no longer recommended for new projects.

Applications relying on unsafe SSL behavior must migrate to certificate pinning before upgrading.

---

# Contributing

Contributions, bug reports, and feature requests are welcome.

See `CONTRIBUTING.md` for details.

---

# License

MIT License — free to use, modify, and distribute.
>>>>>>> a3bf19d7d1e84ec570183c8ef97967af79fb4548
