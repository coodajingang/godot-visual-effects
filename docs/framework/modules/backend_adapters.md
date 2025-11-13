# Backend Adapters

Backend adapters translate kit requests into provider-specific API calls. They shield the rest of the framework from authentication mechanics, REST routing, and response normalization.

## Interface

All adapters extend `addons/universal_framework/backend/backend_adapter.gd` and must implement the following asynchronous methods:

```gdscript
login_with_email(email: String, password: String) -> Dictionary
login_guest() -> Dictionary
login_with_oauth(provider: String) -> Dictionary
fetch_profile() -> Dictionary
fetch_storefront() -> Array
purchase_product(product_id: String) -> Dictionary
fetch_cloud_save() -> Dictionary
push_cloud_save(payload: Dictionary) -> Dictionary
fetch_leaderboard(board_id: String) -> Dictionary
fetch_missions() -> Array
submit_analytics_event(event_name: String, payload: Dictionary) -> void
report_exception(context: Dictionary) -> void
```

Use structured dictionaries in responses. For example `purchase_product` should return `{ "success": true, "rewards": { ... }, "profile": { ... } }` or `{ "success": false, "error": "REASON" }`.

## SupabaseAdapter

Located at `addons/universal_framework/backend/supabase_adapter.gd`, this adapter is optimized for RESTful Supabase APIs:

- REST endpoints served via the generated API (`/auth/v1/login`, `/rest/v1/profiles`, etc.).
- Realtime features can be added by wiring Supabase Realtime channels into NetKit.
- Use service role keys on the server only; the Godot client should operate with the `anon` role.

## FirebaseAdapter

Located at `addons/universal_framework/backend/firebase_adapter.gd`, this adapter targets Firebase web endpoints:

- Authentication uses `identitytoolkit.googleapis.com` (email/password) and `securetoken.googleapis.com` (token refresh).
- Profile, saves, and leaderboard data can live in Firestore or Realtime Database.
- Analytics events can be sent through the Measurement Protocol or bridged via native SDKs.

## MockBackendService

For offline development the repository includes `MockBackendService`, a lightweight Node that mimics all adapter responses, including:

- Deterministic profile data and storefront catalog.
- Purchase reward application (currency + inventory updates).
- Cloud save fetch/push cycle with timestamps.
- Leaderboard and mission placeholders.

## Adding a new adapter

1. Create a class extending `BackendAdapter`.
2. Implement the required methods and handle provider-specific authentication.
3. Register the class inside `BackendRegistry.create_adapter`.
4. Supply any configuration via `GameApp.config["backend_config"]`.
5. Update documentation so consumers know how to provision credentials.
