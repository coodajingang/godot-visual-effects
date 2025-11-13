# AuthKit

AuthKit centralizes player authentication flows and user identity management. It supports email/password, guest sessions, and OAuth providers, while exposing hooks for real-name verification and anti-addiction checks.

## Responsibilities

- Orchestrate login flows (guest, email, OAuth).
- Cache the active user profile via DataKit.
- Publish authentication lifecycle events on the EventBus.
- Provide helper methods to refresh profiles or trigger identity checks.

## API surface

```gdscript
var profile: Dictionary = await GameApp.get_auth().login_with_email(email, password)
await GameApp.get_auth().login_guest()
await GameApp.get_auth().login_with_oauth("google")
await GameApp.get_auth().sync_profile()
GameApp.get_auth().verify_identity({"country": "CN", "id_number": "..."})
GameApp.get_auth().logout()
GameApp.get_auth().is_authenticated()
GameApp.get_auth().get_profile()
```

All async methods return dictionaries. On failure they include `{"success": false, "error": "REASON"}`.

## EventBus topics

| Event | Payload |
|-------|---------|
| `auth.login_started` | `{ email?, mode? }`
| `auth.login_succeeded` | `{ profile, mode? }`
| `auth.login_failed` | `{ error, email? }`
| `auth.profile_refreshed` | `{ profile }`
| `auth.logout_completed` | `{}`

## Signals

- `login_started(context: Dictionary)`
- `login_succeeded(profile: Dictionary)`
- `login_failed(error: Dictionary)`
- `logout_completed()`

## Extending AuthKit

1. **Add new providers** – implement a method that forwards the request to the backend adapter (e.g., SMS OTP, console network login).
2. **Device binding** – store device identifiers in `DataKit` and include them in the login payload.
3. **Compliance checks** – override `verify_identity()` to talk to platform-specific APIs.

When extending, avoid direct references to other kits—use `GameApp.get_data()` and `GameApp.event_bus.publish()` to keep the module loosely coupled.
