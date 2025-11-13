extends Node
class_name EventBus

## A lightweight publish/subscribe message hub used by the framework.
## Modules interact exclusively through the event bus to avoid tight coupling.

var _subscribers: Dictionary = {}

func subscribe(event_name: String, callback: Callable, metadata: Dictionary = {}) -> void:
    if event_name.is_empty():
        push_warning("Attempted to subscribe to an empty event name.")
        return
    if callback.is_null():
        push_warning("Attempted to subscribe with an invalid callable for event %s" % event_name)
        return
    var subscribers: Array = _subscribers.get(event_name, [])
    # Avoid duplicate registrations for the same callable.
    for entry in subscribers:
        if entry.callable == callback:
            return
    subscribers.append({
        "callable": callback,
        "once": metadata.get("once", false)
    })
    _subscribers[event_name] = subscribers

func once(event_name: String, callback: Callable) -> void:
    subscribe(event_name, callback, {"once": true})

func unsubscribe(event_name: String, callback: Callable) -> void:
    if event_name not in _subscribers:
        return
    var subscribers: Array = _subscribers[event_name]
    subscribers = subscribers.filter(func(entry): return entry.callable != callback)
    if subscribers.is_empty():
        _subscribers.erase(event_name)
    else:
        _subscribers[event_name] = subscribers

func publish(event_name: String, payload: Dictionary = {}) -> void:
    if event_name.is_empty():
        push_warning("Attempted to publish an empty event name.")
        return
    var subscribers: Array = _subscribers.get(event_name, []).duplicate(true)
    var wildcard: Array = _subscribers.get("*", []).duplicate(true)
    if wildcard.size() > 0:
        subscribers.append_array(wildcard)
    if subscribers.is_empty():
        return
    payload = payload.duplicate(true)
    payload["event_name"] = event_name
    var to_remove: Array[int] = []
    for i in range(subscribers.size()):
        var entry: Dictionary = subscribers[i]
        var cb: Callable = entry.callable
        if cb.is_null():
            to_remove.append(i)
            continue
        cb.call(payload)
        if entry.get("once", false):
            to_remove.append(i)
    # Clean up one-shot callbacks and stale references.
    if to_remove.is_empty():
        return
    to_remove.sort() # Ensure descending removal when popping.
    to_remove.reverse()
    var master_list: Array = _subscribers.get(event_name, [])
    for index in to_remove:
        if index < master_list.size():
            master_list.remove_at(index)
    if master_list.is_empty():
        _subscribers.erase(event_name)
    else:
        _subscribers[event_name] = master_list

func clear(event_name: String = "") -> void:
    if event_name.is_empty():
        _subscribers.clear()
    else:
        _subscribers.erase(event_name)
