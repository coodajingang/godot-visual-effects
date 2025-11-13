# Cloud Backend Setup (Supabase & Firebase)

This document walks through provisioning Supabase and Firebase projects so the Godot4 Universal Game Framework can connect to production services instead of the built-in mock backend.

> **Prerequisites**
>
> - Godot Engine 4.2 or later
> - Node.js 18+ (for Firebase CLI tools)
> - A Supabase account and a Firebase (Google Cloud) account

---

## 1. Supabase configuration

### 1.1 Create a new project

1. Sign in to [Supabase](https://app.supabase.com/).
2. Click **New project** and select the desired organization.
3. Choose a database password and region, then create the project.
4. Once created, navigate to **Project Settings → API** and copy the following values:
   - `Project URL`
   - `anon` key (public client key)
   - `service_role` key (keep private, used for administrative operations)

### 1.2 Database schema

1. Open the SQL editor and run the migration script in [`docs/framework/modules/supabase_schema.sql`](docs/framework/modules/supabase_schema.sql) (create the file if you tailor the schema).
2. Suggested tables:
   - `profiles` – player profile metadata (nickname, level, experience, vip, etc.).
   - `saves` – JSONB column storing cloud save snapshots, keyed by user id.
   - `purchases` – transaction logs (product id, price, rewards, platform).
   - `leaderboards` – aggregated ranking entries (score, season, metadata).

### 1.3 Storage & functions

- Enable **Storage** buckets if you plan to persist binary save files.
- Create **Edge Functions** for security-critical logic (e.g., validating receipts or recalculating leaderboard scores). Example templates are provided under [`docs/framework/modules/backend_functions.md`](docs/framework/modules/backend_functions.md).

### 1.4 Connecting from Godot

1. Duplicate `framework/core/game_app.gd` configuration block:
   ```gdscript
   var config := {
       "backend": "supabase",
       "supabase": {
           "url": "https://<PROJECT_REF>.supabase.co",
           "anon_key": "<anon-key>",
           "service_key": "<service-role-key>"
       }
   }
   ```
2. Replace the mock service instantiation with your Supabase provider:
   ```gdscript
   backend_service = SupabaseRestService.new(config["supabase"])
   backend_adapter = SupabaseAdapter.new()
   backend_adapter.configure(backend_service, "supabase")
   ```
   (The repository includes the adapter interface; you supply the concrete REST/WebSocket service implementation.)
3. Ensure HTTPS requests are enabled in your export templates (Project Settings → Network → Allow HTTPS).

---

## 2. Firebase configuration

### 2.1 Create a project

1. Visit the [Firebase console](https://console.firebase.google.com/) and create a new project.
2. Enable **Google Analytics** if you plan to collect analytics events.
3. Add a **Web app** to retrieve the configuration snippet (API key, auth domain, project ID).

### 2.2 Authentication

1. Navigate to **Build → Authentication**.
2. Enable the providers you need (Anonymous, Email/Password, Google, Apple, etc.).
3. Record the API key and auth domain for your client configuration.

### 2.3 Firestore & Cloud Functions

1. Enable **Cloud Firestore** in Native/Production mode.
2. Create collections similar to the Supabase schema (`profiles`, `saves`, `leaderboards`, `purchases`).
3. Install the Firebase CLI and initialize functions:
   ```bash
   npm install -g firebase-tools
   firebase login
   firebase init functions
   ```
4. Deploy callable functions for sensitive workflows (purchase validation, anti-cheat reviews). Reference [`docs/framework/modules/backend_functions.md`](docs/framework/modules/backend_functions.md) for starter code.

### 2.4 Cloud Storage & Analytics

- Configure **Cloud Storage** rules to restrict access per user.
- Enable **Google Analytics** and create custom event definitions matching the ones emitted by `LogKit` (e.g., `gameplay_flow`, `store_purchase`).

### 2.5 Connecting from Godot

1. Provide Firebase credentials during initialization:
   ```gdscript
   var config := {
       "backend": "firebase",
       "firebase": {
           "api_key": "<api-key>",
           "auth_domain": "<project>.firebaseapp.com",
           "project_id": "<project-id>",
           "storage_bucket": "<project>.appspot.com"
       }
   }
   ```
2. Instantiate the Firebase adapter with your REST/HTTP client (REST API or custom C# plugin).
3. Exchange authentication tokens and attach them to subsequent REST/WebSocket requests handled by `NetKit`.
4. Use Firebase Cloud Messaging or Firestore snapshot listeners to drive real-time features.

---

## 3. Local environment variables

For both providers, prefer loading secrets from environment variables instead of hard-coding them in scripts. The simplest approach is to add an `.env` file next to `project.godot` and load it at runtime before initializing `GameApp`.

Example `.env` file:

```
SUPABASE_URL=https://<PROJECT_REF>.supabase.co
SUPABASE_ANON_KEY=<anon>
FIREBASE_API_KEY=<api-key>
```

Load the file using a small boot script (C# or GDScript) before `GameApp` runs and populate `GameApp.config` accordingly.

---

## 4. Testing integrations

1. Keep the mock backend enabled while you develop UI flows.
2. Swap to real adapters in development builds by toggling `config.backend`.
3. Add integration tests in Godot by scripting against the adapter interface and running them in headless mode (`godot --headless --run-tests`).
4. Validate purchase flows with platform-specific sandboxes (Google Play, App Store, Steam) and forward receipts to your cloud functions for verification.

---

## 5. Deployment checklist

- [ ] Secrets loaded from environment variables, not source control.
- [ ] Supabase/Firebase security rules reviewed and locked down.
- [ ] Cloud Functions deployed and versioned.
- [ ] Analytics and crash reporting toggles exposed via remote config.
- [ ] Automated backups enabled for databases (Supabase PG backups, Firestore export jobs).

Once these steps are complete, the Godot4 Universal Game Framework can run against production-grade backend infrastructure with minimal code changes.
