# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/).

---

## [2.0.0] – 2025-12-26

### Breaking Changes
- Removed **all unsafe SSL APIs**:
  - `getServerResultsUnsafe(...)`
  - `getUnsafeClient()` (no-args variant)
- Removed automatic creation of unsafe SSL clients

###  Security
- Introduced **certificate pinning** as the only supported SSL customization
- Ensures **Google Play Store compliance**
- Prevents accidental inclusion of unsafe SSL code in compiled APKs

### Added
- `SSLUtil.createUnsafeClient(InputStream certificate, String hostname)`
  - Allows secure, pinned SSL connections to trusted servers
- Lazy initialization of custom OkHttpClient instances
- Clear failure behavior when unsafe client is not explicitly initialized

### Improved
- Cleaner API surface with fewer foot-guns
- Clear separation between:
  - Safe default networking
  - Advanced user-controlled networking via `OkHttpClient`

### Deprecated
- **v1.x is deprecated** and should not be used for new projects

---

## [1.0.0] – Initial Release - 2025-12-24

### Features
- Blocking HTTP wrapper around OkHttp
- Unified `ServerResults` response object
- No checked exceptions exposed to callers
- Support for GET, POST, PUT, PATCH, DELETE
- Optional headers
- Response body, headers, and status code access
- Unsafe SSL support (removed in v2.0.0)

---

