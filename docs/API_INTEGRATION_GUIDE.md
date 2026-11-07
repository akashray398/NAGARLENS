# NAGARLENS — Android-to-Cloud Integration Guide

> **Target Package:** `com.nagarlens.app.core.network`  
> **Networking Stack:** Retrofit 2 + OkHttp 4 + Gson / Kotlinx Serialization  

---

## 1. Network Client Architecture

1. **Auth Interceptor**: Automatically attaches `Authorization: Bearer <token>` to outgoing REST requests.
2. **Token Rotation**: Intercepts `401 Unauthorized` responses and automatically triggers `POST /api/v1/auth/refresh` to obtain new Access Tokens without interrupting field workflow.
3. **Offline Fallback**: When network is disconnected, Android repositories read/write directly to local Room Database v5 (`nagarlens_evidence.db`), queuing upload tasks for background execution via `WorkManager`.
