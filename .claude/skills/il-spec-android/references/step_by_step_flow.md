# Step-by-step Flow in design.md

## Why

A one-line story ("người dùng đăng nhập") hides steps — the app calls login, saves tokens to secure
storage, updates the in-memory token provider, navigates; later a 401 triggers a refresh and a retry.
Unnamed steps get implemented wrong. The Step-by-step Flow writes each backend call and side effect
out with its trigger, precondition and every branch.

## When it is mandatory

When the flow makes **2 or more sequential backend calls or side effects** (a call plus a secure
storage write and navigation counts), including steps owned by another feature or by
`core/network` (auth interceptor, refresh).

## Shape

```
Step N — <name> (this feature | <other feature>, out of scope)
- Trigger: <what the user does, or which component runs it>
- Pre-condition: <what must already be true>
- Where: <XxxViewModel → XxxUseCase → XxxRepositoryImpl | core/network interceptor>
- API: <METHOD /v1/...> (key fields only — cite openapi.yaml, never paste the schema)
    - Success → <effect: storage write, UI state, navigation>
    - Failure (<case>) → <what the user sees — must match the Error → UI table>
```

Steps owned by another feature say so and link that feature's design instead of duplicating it.

## Illustrative example (shape only — Android has no Login spec yet; verify against `ui/screens/login/` and openapi before reusing)

```
Step 1 — Đăng nhập (this feature)
- Trigger: bấm "Đăng nhập", phone hợp lệ + password không rỗng
- Pre-condition: ViewModel đã validate phone/password
- Where: LoginViewModel → LoginUseCase → AuthRepositoryImpl (safeCall)
- API: POST /v1/auth/login (phone_number, password, device{platform:"android"})
    - Success → lưu access/refresh token vào secure storage, cập nhật AuthTokenProvider, điều hướng Home
    - Failure (INVALID_CREDENTIALS 401) → ở lại Login, một thông báo chung
    - Failure (AppError.NoConnection) → thông báo mất mạng, giữ dữ liệu đã nhập

Step 2 — Làm mới phiên (core/network, out of scope)
- Trigger: một request `@Authenticated` nhận 401
- API: POST /v1/auth/refresh ({refresh_token})
    - Success → lưu token mới, gửi lại request
    - Failure (4xx) → xoá token, về Login
```

## Unconfirmed steps

```
- API: POST /v1/...
  ⚠️ Not confirmed: <question> — <what it blocks>.
```

When answered, state the step as fact and record in the changelog whether the step structure
changed or only the marker went away.
