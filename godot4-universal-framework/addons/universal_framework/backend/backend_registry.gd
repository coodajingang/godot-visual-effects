extends Resource
class_name BackendRegistry

const BackendAdapter = preload("res://addons/universal_framework/backend/backend_adapter.gd")
const SupabaseAdapter = preload("res://addons/universal_framework/backend/supabase_adapter.gd")
const FirebaseAdapter = preload("res://addons/universal_framework/backend/firebase_adapter.gd")
const MockBackendService = preload("res://addons/universal_framework/services/mock_backend_service.gd")

static func create_adapter(provider: String, service: MockBackendService, config: Dictionary = {}) -> BackendAdapter:
    var adapter: BackendAdapter
    match provider:
        "supabase":
            adapter = SupabaseAdapter.new()
        "firebase":
            adapter = FirebaseAdapter.new()
        _:
            adapter = BackendAdapter.new()
    adapter.configure(service, provider)
    if adapter.has_method("setup"):
        adapter.setup(config)
    return adapter
