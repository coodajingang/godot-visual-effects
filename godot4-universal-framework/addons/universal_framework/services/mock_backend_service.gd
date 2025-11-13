extends Node
class_name MockBackendService

## Mock backend service used when Supabase/Firebase integrations are not available.
## It simulates latency and predictable responses for demo and testing purposes.

var latency_seconds: float = 0.35
var provider_id: String = "mock"

var user_profile: Dictionary = {}
var cloud_save: Dictionary = {}
var storefront: Array = []

func configure(config: Dictionary) -> void:
    latency_seconds = config.get("latency", latency_seconds)
    provider_id = config.get("provider", provider_id)

func bootstrap() -> void:
    user_profile = {
        "user_id": "guest-0001",
        "email": "",
        "nickname": "Trailblazer",
        "level": 5,
        "experience": 1240,
        "vip": 0,
        "currencies": {
            "gold": 820,
            "gems": 30
        }
    }
    cloud_save = {
        "timestamp": Time.get_unix_time_from_system(),
        "progress": {
            "chapter": 1,
            "stage": 3,
            "score": 4200
        },
        "inventory": [
            {"id": "potion_small", "quantity": 5},
            {"id": "key_bronze", "quantity": 1}
        ]
    }
    storefront = [
        {
            "id": "starter_pack",
            "name": "Starter Pack",
            "description": "Includes 1,000 gold and an exclusive sword skin",
            "price": 4.99,
            "currency": "USD",
            "rewards": {
                "gold": 1000,
                "gems": 80,
                "items": [
                    {"id": "skin_sword_celestial", "quantity": 1}
                ]
            }
        },
        {
            "id": "daily_bundle",
            "name": "Daily Gem Bundle",
            "description": "Doubles your gems for the next purchase",
            "price": 1.99,
            "currency": "USD",
            "rewards": {
                "gems": 60
            }
        }
    ]

func login_with_email(email: String, _password: String) -> Dictionary:
    await _simulate_latency()
    user_profile["email"] = email
    user_profile["user_id"] = "email-" + email.sha1_text().substr(0, 8)
    user_profile["nickname"] = email.split("@")[0].capitalize()
    return {"success": true, "profile": user_profile.duplicate(true)}

func login_guest() -> Dictionary:
    await _simulate_latency()
    var suffix := str(randi() % 10000).pad_zeros(4)
    user_profile["user_id"] = "guest-%s" % suffix
    user_profile["nickname"] = "Guest" + suffix
    return {"success": true, "profile": user_profile.duplicate(true)}

func login_with_oauth(provider: String) -> Dictionary:
    await _simulate_latency()
    var suffix := str(randi() % 10000).pad_zeros(4)
    user_profile["email"] = "%s_user@example.com" % provider
    user_profile["user_id"] = "%s-%s" % [provider, suffix]
    user_profile["nickname"] = "%s Hero" % provider.capitalize()
    return {"success": true, "profile": user_profile.duplicate(true)}

func fetch_profile() -> Dictionary:
    await _simulate_latency()
    return user_profile.duplicate(true)

func fetch_storefront() -> Array:
    await _simulate_latency()
    return storefront.duplicate(true)

func purchase_product(product_id: String) -> Dictionary:
    await _simulate_latency()
    var product := storefront.filter(func(item): return item.get("id") == product_id)
    if product.is_empty():
        return {"success": false, "error": "PRODUCT_NOT_FOUND"}
    var rewards: Dictionary = product[0].get("rewards", {})
    _apply_rewards(rewards)
    return {
        "success": true,
        "rewards": rewards,
        "profile": user_profile.duplicate(true)
    }

func fetch_cloud_save() -> Dictionary:
    await _simulate_latency()
    cloud_save["timestamp"] = Time.get_unix_time_from_system()
    return cloud_save.duplicate(true)

func push_cloud_save(payload: Dictionary) -> Dictionary:
    await _simulate_latency()
    cloud_save = payload.duplicate(true)
    cloud_save["timestamp"] = Time.get_unix_time_from_system()
    return {"success": true, "snapshot": cloud_save.duplicate(true)}

func fetch_leaderboard(board_id: String) -> Dictionary:
    await _simulate_latency()
    return {
        "board_id": board_id,
        "entries": [
            {"rank": 1, "player": "Nova", "score": 12500},
            {"rank": 2, "player": user_profile.get("nickname", ""), "score": 11940},
            {"rank": 3, "player": "Atlas", "score": 9800}
        ]
    }

func fetch_missions() -> Array:
    await _simulate_latency()
    return [
        {
            "id": "daily_login",
            "name": "Daily Login",
            "progress": 1,
            "goal": 1,
            "reward": {"gems": 5}
        },
        {
            "id": "win_battles",
            "name": "Win 3 Battles",
            "progress": 2,
            "goal": 3,
            "reward": {"gold": 300}
        }
    ]

func submit_analytics_event(event_name: String, details: Dictionary) -> void:
    await _simulate_latency()
    DebugConsole.append_entry({
        "category": "analytics",
        "event": event_name,
        "details": details,
        "timestamp": Time.get_datetime_string_from_system()
    })

func report_exception(context: Dictionary) -> void:
    await _simulate_latency()
    DebugConsole.append_entry({
        "category": "exception",
        "context": context,
        "timestamp": Time.get_datetime_string_from_system()
    })

func _simulate_latency() -> void:
    var tree := get_tree()
    if tree == null:
        return
    await tree.create_timer(latency_seconds).timeout

func _apply_rewards(rewards: Dictionary) -> void:
    if rewards.is_empty():
        return
    for currency in ["gold", "coins", "gems", "diamonds"]:
        if rewards.has(currency):
            var normalized := currency == "coins" ? "gold" : (currency == "diamonds" ? "gems" : currency)
            var currencies: Dictionary = user_profile.get("currencies", {})
            currencies[normalized] = currencies.get(normalized, 0) + int(rewards.get(currency, 0))
            user_profile["currencies"] = currencies
    if rewards.has("items"):
        var inventory: Array = cloud_save.get("inventory", [])
        for item in rewards.get("items", []):
            var existing_idx := -1
            for i in range(inventory.size()):
                if inventory[i].get("id") == item.get("id"):
                    existing_idx = i
                    break
            if existing_idx == -1:
                inventory.append(item)
            else:
                inventory[existing_idx]["quantity"] = inventory[existing_idx].get("quantity", 1) + item.get("quantity", 1)
        cloud_save["inventory"] = inventory
    if rewards.has("rewards"):
        _apply_rewards(rewards.get("rewards"))
    if rewards.has("currencies"):
        _apply_rewards(rewards.get("currencies"))
