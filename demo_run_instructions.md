# Demo Run Instructions

This guide walks you through the playable scenario included with the Godot4 Universal Game Framework. The demo simulates the typical free-to-play onboarding loop using a mock backend service.

## Prerequisites

- Godot Engine **4.2+**
- The repository cloned locally

## Launching the project

1. Start Godot and open the project located in `godot4-universal-framework/`.
2. Run the main scene (`scenes/Main.tscn`) or press <kbd>F5</kbd> from the editor to launch the game.

## Guided flow

1. **Login**
   - Enter any email/password combination and press **Sign In**, or click **Guest Login** for an instant session.
   - The mock backend returns a sample profile. The status label updates once the login completes.
2. **Profile & Cloud Sync**
   - After login the profile panel becomes visible, displaying nickname, level, and currency balances.
   - A background cloud sync is triggered automatically. You can manually resync by pressing **Sync Cloud Save**.
3. **Open the Store**
   - Press **Open Store** to fetch the storefront catalog and render the available products.
   - The product list is populated dynamically using `PayKit.load_catalog()`.
4. **Purchase an item**
   - Click any product button to trigger a simulated purchase.
   - Upon success the profile balances update, rewards are added to the inventory, and a success banner appears.
   - Failures (invalid product id, network error) display an error banner.

## Developer utilities

- Enable the in-game debug console by watching the `DebugConsole` autoload in the Remote Scene tree while running the game. Entries capture analytics events, purchase receipts, and exception reports.
- Subscribe to additional events by connecting listeners to `GameApp.event_bus` (e.g., print payloads for `auth.login_succeeded`).

## Switching backends

The demo defaults to the mock backend for ease of use. To test the Supabase or Firebase adapters:

1. Edit `framework/core/game_app.gd` and change `config["backend"]` to either `"supabase"` or `"firebase"`.
2. Provide the necessary credentials (see [`SETUP.md`](./SETUP.md)).
3. Replace the mock service wiring with your provider-specific service implementation.

## Troubleshooting

- **No UI updates after login** – ensure the `GameApp` autoload is active (Project Settings → AutoLoad) and that the scene references are correct.
- **Missing fonts/colors** – Godot will fall back to default theme settings if custom fonts are unavailable. The demo uses default theme values.
- **Network timeout** – When using real backends, make sure HTTPS is enabled in Project Settings → Network.

Enjoy exploring the framework! Refer to the [architecture doc](./docs/framework/architecture.md) for a deep dive into the data flow powering this demo.
