extends Node
class_name DebugConsoleSingleton

## Runtime debug console storing in-memory logs for inspection.

signal entry_added(entry: Dictionary)

var entries: Array = []
var max_entries: int = 200

func append_entry(entry: Dictionary) -> void:
    var enriched := entry.duplicate(true)
    if not enriched.has("timestamp"):
        enriched["timestamp"] = Time.get_datetime_string_from_system()
    entries.append(enriched)
    if entries.size() > max_entries:
        entries.pop_front()
    entry_added.emit(enriched)

func clear() -> void:
    entries.clear()

# Convenience logging helpers --------------------------------------------------

func info(message: String, details: Dictionary = {}) -> void:
    append_entry({"level": "INFO", "message": message, "details": details})

func warn(message: String, details: Dictionary = {}) -> void:
    append_entry({"level": "WARN", "message": message, "details": details})

func error(message: String, details: Dictionary = {}) -> void:
    append_entry({"level": "ERROR", "message": message, "details": details})
