extends BaseModule
class_name NetKit

signal request_started(context: Dictionary)
signal request_completed(response: Dictionary)
signal request_failed(error: Dictionary)

## Simplified network abstraction used by the framework. In production this
## would wrap HTTPClient/WebSocket clients and provide retry policies.

func request(endpoint: String, method: String = "GET", payload: Dictionary = {}) -> Dictionary:
    request_started.emit({
        "endpoint": endpoint,
        "method": method,
        "payload": payload
    })
    event_bus.publish("net.request_started", {
        "endpoint": endpoint,
        "method": method
    })
    # Delegates to the backend adapter for demo purposes.
    var response: Dictionary
    match endpoint:
        "profile":
            response = await backend_adapter.fetch_profile()
        "storefront":
            response = {"items": await backend_adapter.fetch_storefront()}
        "cloud_save":
            response = await backend_adapter.fetch_cloud_save()
        _:
            response = {"error": "ENDPOINT_NOT_MOCKED", "endpoint": endpoint}
    if response.has("error"):
        request_failed.emit(response)
        event_bus.publish("net.request_failed", response)
    else:
        request_completed.emit(response)
        event_bus.publish("net.request_completed", response)
    return response

func watch(endpoint: String, interval_seconds: float, callback: Callable) -> void:
    async func watcher():
        while true:
            var payload = await request(endpoint)
            if not callback.is_null():
                callback.call(payload)
            await get_tree().create_timer(interval_seconds).timeout
    watcher()
