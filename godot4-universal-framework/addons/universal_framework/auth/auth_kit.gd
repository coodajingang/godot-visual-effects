extends BaseModule
class_name AuthKit

signal login_started(context: Dictionary)
signal login_succeeded(profile: Dictionary)
signal login_failed(error: Dictionary)
signal logout_completed()

var _profile: Dictionary = {}
var _is_authenticated := false

func _on_setup() -> void:
    event_bus.subscribe("datakit.cloud_synced", Callable(self, "_on_cloud_synced"))
    DebugConsole.info("AuthKit initialized", {"backend": backend_adapter.provider})

func login_with_email(email: String, password: String) -> Dictionary:
    login_started.emit({"email": email})
    event_bus.publish("auth.login_started", {"email": email})
    var result: Dictionary = await backend_adapter.login_with_email(email, password)
    if result.get("success", false):
        _profile = result.get("profile", {})
        _is_authenticated = true
        login_succeeded.emit(_profile)
        event_bus.publish("auth.login_succeeded", {"profile": _profile})
        var data_kit: DataKit = game_app.get_data()
        data_kit.cache_profile(_profile)
        return _profile
    login_failed.emit(result)
    event_bus.publish("auth.login_failed", result)
    return result

func login_guest() -> Dictionary:
    login_started.emit({"mode": "guest"})
    event_bus.publish("auth.login_started", {"mode": "guest"})
    var result: Dictionary = await backend_adapter.login_guest()
    if result.get("success", false):
        _profile = result.get("profile", {})
        _is_authenticated = true
        login_succeeded.emit(_profile)
        event_bus.publish("auth.login_succeeded", {"profile": _profile, "mode": "guest"})
        var data_kit: DataKit = game_app.get_data()
        data_kit.cache_profile(_profile)
        return _profile
    login_failed.emit(result)
    return result

func login_with_oauth(provider: String) -> Dictionary:
    login_started.emit({"oauth_provider": provider})
    var result: Dictionary = await backend_adapter.login_with_oauth(provider)
    if result.get("success", false):
        _profile = result.get("profile", {})
        _is_authenticated = true
        login_succeeded.emit(_profile)
        event_bus.publish("auth.login_succeeded", {"profile": _profile, "mode": provider})
        var data_kit: DataKit = game_app.get_data()
        data_kit.cache_profile(_profile)
        return _profile
    login_failed.emit(result)
    return result

func sync_profile() -> Dictionary:
    var profile = await backend_adapter.fetch_profile()
    _profile = profile
    if _is_authenticated:
        event_bus.publish("auth.profile_refreshed", {"profile": _profile})
    return _profile

func verify_identity(payload: Dictionary) -> Dictionary:
    # Stubbed implementation for real-name verification / anti-addiction compliance.
    await backend_adapter.submit_analytics_event("auth.verify_identity", payload)
    return {"success": true, "status": "verified"}

func logout() -> void:
    _profile = {}
    _is_authenticated = false
    logout_completed.emit()
    event_bus.publish("auth.logout_completed")

func is_authenticated() -> bool:
    return _is_authenticated

func get_profile() -> Dictionary:
    return _profile.duplicate(true)

func _on_cloud_synced(payload: Dictionary) -> void:
    DebugConsole.info("Cloud data synchronized", payload)
