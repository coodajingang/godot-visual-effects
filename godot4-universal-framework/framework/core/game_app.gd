extends Node
class_name GameApplication

const EventBus = preload("res://addons/universal_framework/core/event_bus.gd")
const BackendRegistry = preload("res://addons/universal_framework/backend/backend_registry.gd")
const MockBackendService = preload("res://addons/universal_framework/services/mock_backend_service.gd")
const LocalizationManager = preload("res://addons/universal_framework/localization/localization_manager.gd")
const BaseModule = preload("res://addons/universal_framework/core/base_module.gd")
const AuthKit = preload("res://addons/universal_framework/auth/auth_kit.gd")
const DataKit = preload("res://addons/universal_framework/data/data_kit.gd")
const PayKit = preload("res://addons/universal_framework/pay/pay_kit.gd")
const SocialKit = preload("res://addons/universal_framework/social/social_kit.gd")
const NetKit = preload("res://addons/universal_framework/net/net_kit.gd")
const LogKit = preload("res://addons/universal_framework/log/log_kit.gd")
const GameKit = preload("res://addons/universal_framework/game/game_kit.gd")

var event_bus: EventBus
var backend_service: MockBackendService
var backend_adapter
var localization_manager: LocalizationManager
var modules: Dictionary = {}

var config: Dictionary = {
    "backend": "supabase",
    "latency": 0.3,
    "locale": "en",
    "backend_config": {}
}

func _ready() -> void:
    _initialize_runtime()
    _initialize_backend()
    _initialize_modules()
    event_bus.publish("app.initialized", {
        "timestamp": Time.get_datetime_string_from_system(),
        "backend": config.get("backend", "supabase")
    })

func _initialize_runtime() -> void:
    event_bus = EventBus.new()
    event_bus.name = "EventBus"
    add_child(event_bus)

    localization_manager = LocalizationManager.new()
    localization_manager.name = "LocalizationManager"
    add_child(localization_manager)
    localization_manager.load_translations({
        "en": {
            "login_button": "Sign In",
            "store_button": "Open Store",
            "purchase_success": "Purchase Complete"
        },
        "zh-Hans": {
            "login_button": "登录",
            "store_button": "打开商城",
            "purchase_success": "购买成功"
        }
    })
    localization_manager.set_locale(config.get("locale", "en"))

func _initialize_backend() -> void:
    backend_service = MockBackendService.new()
    backend_service.name = "MockBackend"
    add_child(backend_service)
    backend_service.configure({
        "latency": config.get("latency", 0.35),
        "provider": config.get("backend", "mock")
    })
    backend_service.bootstrap()

    backend_adapter = BackendRegistry.create_adapter(
        config.get("backend", "supabase"),
        backend_service,
        config.get("backend_config", {})
    )
    backend_adapter.name = "BackendAdapter"
    add_child(backend_adapter)

func _initialize_modules() -> void:
    _register_module("data", DataKit.new())
    _register_module("net", NetKit.new())
    _register_module("auth", AuthKit.new())
    _register_module("pay", PayKit.new())
    _register_module("social", SocialKit.new())
    _register_module("log", LogKit.new())
    _register_module("game", GameKit.new())

func _register_module(name_key: String, module: BaseModule) -> void:
    module.name = name_key.capitalize() + "Module"
    add_child(module)
    modules[name_key] = module
    module.setup(self)

func get_module(name_key: String):
    return modules.get(name_key)

func get_auth():
    return modules.get("auth")

func get_data():
    return modules.get("data")

func get_pay():
    return modules.get("pay")

func get_social():
    return modules.get("social")

func get_net():
    return modules.get("net")

func get_log():
    return modules.get("log")

func get_game():
    return modules.get("game")
