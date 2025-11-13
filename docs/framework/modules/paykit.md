# PayKit

PayKit encapsulates storefront retrieval, virtual currencies, and purchase flows. It integrates with platform-specific billing via the backend adapter, making it easy to support Apple/Google/Steam payments or ad-monetized rewards.

## Responsibilities

- Load storefront metadata and expose it to the UI.
- Trigger purchase requests and handle receipts.
- Apply currency/item rewards to local caches through DataKit.
- Emit analytics-friendly events (`pay.purchase_*`).

## API surface

```gdscript
var catalog: Array = await GameApp.get_pay().load_catalog()
var result: Dictionary = await GameApp.get_pay().purchase_product("starter_pack")
```

`load_catalog(force_refresh := false)` caches the result in memory for the session.

## EventBus topics

| Event | Payload |
|-------|---------|
| `pay.catalog_loaded` | `{ catalog }`
| `pay.purchase_started` | `{ product_id }`
| `pay.purchase_succeeded` | `{ product_id, rewards, profile }`
| `pay.purchase_failed` | `{ product_id, error }`

## Signals

- `catalog_loaded(catalog: Array)`
- `purchase_started(context: Dictionary)`
- `purchase_succeeded(receipt: Dictionary)`
- `purchase_failed(error: Dictionary)`

## Reward application

When a purchase succeeds, PayKit delegates to DataKit to mutate the profile and cloud save reserves:

```gdscript
var profile := data_kit.get_profile()
profile["currencies"]["gold"] += rewards.get("gold", 0)
profile["currencies"]["gems"] += rewards.get("gems", 0)
data_kit.cache_profile(profile)
```

Inventory items are merged into `cloud_save["inventory"]` to keep equipment and consumables in sync across devices.

## Extending PayKit

- **In-app purchases** – integrate platform billing validations inside the backend adapter and return normalized receipts.
- **Ad monetization** – extend the module with `show_rewarded_ad()` that emits `pay.rewarded_ad_completed` events.
- **Subscription management** – add helpers to expose entitlement states and renewal dates fetched from the backend.
