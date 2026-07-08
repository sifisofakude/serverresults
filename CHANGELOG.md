# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog and this project adheres to Semantic Versioning.

---

## [3.0.0] – 2026-07-08

### Breaking Changes
- Renamed the public package to:
  - `io.github.sifisofakude.net.serverresults`
- Applications upgrading from v2.x must update their import statements.

### Added
- Command-line (CLI) support.
- `FileUpload` for streaming multipart file uploads.
- Direct access to the underlying OkHttp `Response` via `getResponse()`.
- Streaming downloads through `downloadFile(OutputStream)`.
- Support for `HEAD` requests.

### Improved
- Reworked API documentation across the library.
- Improved multipart upload implementation using streamed request bodies.
- Improved response handling for large downloads.
- Enhanced certificate pinning API.
- Better error reporting and response handling.

### Deprecated
- `createPinnedClient(String, String)` in favor of `createPinnedClient(InputStream, String)`.

---

## [2.0.0] – 2025-12-29

### Breaking Changes
- Removed all unsafe SSL APIs:
  - `getServerResultsUnsafe(...)`
  - `getUnsafeClient()`
- Removed automatic creation of unsafe SSL clients.

### Security
- Introduced certificate pinning for secure HTTPS communication.
- Improved Google Play Store compatibility.
- Eliminated insecure SSL bypass functionality.

### Added
- `createPinnedClient(String, String)` for certificate-pinned HTTPS connections.
- Shared pinned `OkHttpClient`.

### Improved
- Simplified networking API.
- Clear separation between standard networking and certificate-pinned networking.

### Deprecated
- Version 1.x is deprecated and no longer recommended for new projects.

---

## [1.0.0] – 2025-12-24

### Added
- Initial release.
- Blocking HTTP wrapper around OkHttp.
- Unified `ServerResults` response object.
- Support for GET, POST, PUT, PATCH, and DELETE requests.
- Optional request headers.
- Access to HTTP response body, headers, and status code.
- Shared secure `OkHttpClient`.
- Unsafe SSL support (removed in v2.0.0).