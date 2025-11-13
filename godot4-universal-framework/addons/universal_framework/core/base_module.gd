extends Node
class_name BaseModule

var game_app
var event_bus: EventBus
var backend_adapter
var localization_manager

func setup(game_app_ref) -> void:
    game_app = game_app_ref
    event_bus = game_app.event_bus
    backend_adapter = game_app.backend_adapter
    localization_manager = game_app.localization_manager
    _on_setup()

func _on_setup() -> void:
    pass

func dispose() -> void:
    _on_dispose()
    queue_free()

func _on_dispose() -> void:
    pass
