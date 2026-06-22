# ServerResults

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

if(result.isNetworkError()) {
    System.out.println(result.getExceptionMessage());
}
else if(result.isSuccess()) {
    System.out.println(result.getResponseText());
}
else {
    System.out.println("HTTP Error: " + result.getResponseCode());
}
```

---

## POST JSON

```java
MediaType json =
    MediaType.parse("application/json; charset=utf-8");

ServerResults result =
    ServerResults.getServerResults(
        "https://api.example.com/users",
        "{\"name\":\"John\"}",
        "POST",
        null,
        json
    );
```

---

## Custom Headers

```java
Map<String,String> headers = new HashMap<>();

headers.put(
    "Authorization",
    "Bearer token"
);

ServerResults result =
    ServerResults.getServerResults(
        url,
        null,
        "GET",
        headers
    );
```

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

    ServerResults result =
        ServerResults.getServerResults(
            uploadUrl,
            upload,
            "POST"
        );
}
```

Files are streamed directly from the provided InputStream and are not fully loaded into memory.

---

# File Downloads

Large files can be downloaded directly to an OutputStream.

```java
ServerResults result =
    ServerResults.getServerResults(
        fileUrl,
        null,
        "GET"
    );

try(FileOutputStream out =
        new FileOutputStream("archive.zip")) {

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
try(InputStream cert =
        context.getAssets().open("certificate.pem")) {

    ServerResults.createPinnedClient(
        cert,
        "api.example.com"
    );
}
```

Using a certificate file path:

```java
ServerResults.createPinnedClient(
    "/path/to/certificate.pem",
    "api.example.com"
);
```

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
