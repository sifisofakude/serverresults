# ServerResults

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

if(result.isNetworkError()) {
    System.out.println(result.getExceptionMessage());
}
else if(result.isSuccess()) {
    System.out.println(result.getResponseText());
}
```

## POST JSON

``` java
MediaType json =
    MediaType.parse("application/json; charset=utf-8");

ServerResults result =
    ServerResults.getServerResults(
        url,
        "{\"name\":\"John\"}",
        "POST",
        null,
        json
    );
```

## Custom Headers

``` java
Map<String,String> headers = new HashMap<>();
headers.put("Authorization","Bearer token");

ServerResults result =
    ServerResults.getServerResults(
        url,
        null,
        "GET",
        headers
    );
```

------------------------------------------------------------------------

# Multipart Uploads

``` java
try(FileUpload upload =
        new FileUpload(
            "photo.jpg",
            fileSize,
            inputStream)) {

    ServerResults result =
        ServerResults.getServerResults(
            uploadUrl,
            upload,
            "POST"
        );
}
```

Uploads are streamed directly from the supplied `InputStream`.

------------------------------------------------------------------------

# Streaming Downloads

``` java
ServerResults result =
    ServerResults.getServerResults(
        fileUrl,
        null,
        "GET"
    );

try(FileOutputStream out =
        new FileOutputStream("archive.zip")) {

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
try(InputStream cert =
        context.getAssets().open("certificate.pem")) {

    ServerResults.createPinnedClient(
        cert,
        "api.example.com"
    );
}
```

Deprecated compatibility overload:

``` java
ServerResults.createPinnedClient(
    "/path/to/certificate.pem",
    "api.example.com"
);
```

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
