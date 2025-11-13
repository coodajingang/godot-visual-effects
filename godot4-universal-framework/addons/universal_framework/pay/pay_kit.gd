extends BaseModule
class_name PayKit

signal catalog_loaded(catalog: Array)
signal purchase_started(context: Dictionary)
signal purchase_succeeded(receipt: Dictionary)
signal purchase_failed(error: Dictionary)

var _catalog: Array = []

func _on_setup() -> void:
    event_bus.subscribe("auth.login_succeeded", Callable(self, "_on_login"))

func load_catalog(force_refresh: bool = false) -> Array:
    if force_refresh or _catalog.is_empty():
        _catalog = await backend_adapter.fetch_storefront()
        catalog_loaded.emit(_catalog)
        event_bus.publish("pay.catalog_loaded", {"catalog": _catalog})
    return _catalog

func purchase_product(product_id: String) -> Dictionary:
    purchase_started.emit({"product_id": product_id})
    event_bus.publish("pay.purchase_started", {"product_id": product_id})
    var result: Dictionary = await backend_adapter.purchase_product(product_id)
    if result.get("success", false):
        _apply_rewards(result.get("rewards", {}))
        purchase_succeeded.emit(result)
        event_bus.publish("pay.purchase_succeeded", result)
        return result
    purchase_failed.emit(result)
    event_bus.publish("pay.purchase_failed", result)
    return result

func _on_login(_payload: Dictionary) -> void:
    # Preload catalog after login for smoother UX.
    async func preload_store():
        await load_catalog()
    preload_store()

func _apply_rewards(rewards: Dictionary) -> void:
    if rewards.is_empty():
        return
    var data_kit: DataKit = game_app.get_data()
    var profile := data_kit.get_profile()
    var currencies: Dictionary = profile.get("currencies", {})
    for currency_key in ["gold", "coins", "gems", "diamonds"]:
        if rewards.has(currency_key):
            var normalized_key = currency_key == "coins" ? "gold" : (currency_key == "diamonds" ? "gems" : currency_key)
            var current_value: int = currencies.get(normalized_key, 0)
            currencies[normalized_key] = current_value + int(rewards[currency_key])
    profile["currencies"] = currencies
    data_kit.cache_profile(profile)

    if rewards.has("items"):
        var snapshot = data_kit.get_cloud_save()
        var inventory: Array = snapshot.get("inventory", [])
        for item in rewards["items"]:
            var existing_index = -1
            for i in range(inventory.size()):
                if inventory[i].get("id") == item.get("id"):
                    existing_index = i
                    break
            if existing_index == -1:
                inventory.append(item)
            else:
                inventory[existing_index]["quantity"] += item.get("quantity", 1)
        snapshot["inventory"] = inventory
        data_kit.update_cloud_save(snapshot)
