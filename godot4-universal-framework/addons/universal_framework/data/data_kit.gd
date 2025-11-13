extends BaseModule
class_name DataKit

signal profile_cached(profile: Dictionary)
signal cloud_sync_started()
signal cloud_sync_completed(snapshot: Dictionary)

var local_cache: Dictionary = {
    "profile": {},
    "cloud_save": {}
}

func _on_setup() -> void:
    event_bus.subscribe("auth.login_succeeded", Callable(self, "_on_login_succeeded"))

func cache_profile(profile: Dictionary) -> void:
    local_cache["profile"] = profile.duplicate(true)
    profile_cached.emit(local_cache["profile"])
    event_bus.publish("datakit.profile_cached", {"profile": local_cache["profile"]})

func get_profile() -> Dictionary:
    return local_cache.get("profile", {}).duplicate(true)

func synchronize_cloud_save() -> Dictionary:
    cloud_sync_started.emit()
    event_bus.publish("datakit.cloud_sync_started")
    var remote_snapshot = await backend_adapter.fetch_cloud_save()
    local_cache["cloud_save"] = _merge_save(remote_snapshot)
    var push_result = await backend_adapter.push_cloud_save(local_cache["cloud_save"])
    var snapshot = push_result.get("snapshot", local_cache["cloud_save"])
    local_cache["cloud_save"] = snapshot
    cloud_sync_completed.emit(snapshot)
    event_bus.publish("datakit.cloud_synced", {"snapshot": snapshot})
    return snapshot

func get_cloud_save() -> Dictionary:
    return local_cache.get("cloud_save", {}).duplicate(true)

func update_cloud_save(patch: Dictionary) -> Dictionary:
    local_cache["cloud_save"] = _merge_save(patch)
    return local_cache["cloud_save"]

func _on_login_succeeded(payload: Dictionary) -> void:
    if payload.has("profile"):
        cache_profile(payload["profile"])
    # Fire and forget cloud sync when the user logs in.
    async_sync_cloud_save()

func async_sync_cloud_save() -> void:
    call_deferred("_perform_cloud_sync")

func _perform_cloud_sync() -> void:
    await synchronize_cloud_save()

func _merge_save(remote: Dictionary) -> Dictionary:
    var snapshot = local_cache.get("cloud_save", {}).duplicate(true)
    for key in remote.keys():
        snapshot[key] = remote[key]
    return snapshot
