# 07 - Security Hardening Guide

This document describes the defensive security controls built into the application.

---

## 1. Magic Byte File Validation (Malware Mitigation)
Relying solely on HTTP `Content-Type` headers for validation is unsafe. To prevent uploading executable code masquerading as images/PDFs, the system validates the first few bytes (magic bytes):
- **PDF**: `%PDF-` (`0x25, 0x50, 0x44, 0x46`)
- **JPEG**: `0xFF, 0xD8, 0xFF`
- **PNG**: `0x89, 0x50, 0x4E, 0x47`
- **TIFF**: `II*` (`0x49, 0x49, 0x2A, 0x00`) or `MM*` (`0x4D, 0x4D, 0x00, 0x2A`)

---

## 2. Secure HTTP Headers
Custom security headers are configured globally inside the Spring Security filter chain:
- **Content Security Policy (CSP)**: `default-src 'self'; frame-ancestors 'none'; object-src 'none';` (restricts execution of unauthorized scripts).
- **HSTS / Frame Options**: Denies embedding the application inside frames to block clickjacking attacks.
- **X-Content-Type-Options**: `nosniff` prevents browsers from MIME-sniffing away from the declared content-type.

---

## 3. Session Anomaly Checker
- Logs detailed audits if a user attempts to refresh their JWT token from a different IP address than the one they signed in with.
- Enhances logging transparency for incident response teams.
