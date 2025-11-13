extends Node
class_name BackendAdapter

## Base adapter that wraps calls to a backend service provider (Supabase / Firebase / Mock).

var service: MockBackendService
var provider: String = "mock"

func configure(service_ref: MockBackendService, provider_id: String) -> void:
    service = service_ref
    provider = provider_id

func login_with_email(email: String, password: String) -> Dictionary:
    return await service.login_with_email(email, password)

func login_guest() -> Dictionary:
    return await service.login_guest()

func login_with_oauth(provider_id: String) -> Dictionary:
    return await service.login_with_oauth(provider_id)

func fetch_profile() -> Dictionary:
    return await service.fetch_profile()

func fetch_storefront() -> Array:
    return await service.fetch_storefront()

func purchase_product(product_id: String) -> Dictionary:
    return await service.purchase_product(product_id)

func fetch_cloud_save() -> Dictionary:
    return await service.fetch_cloud_save()

func push_cloud_save(payload: Dictionary) -> Dictionary:
    return await service.push_cloud_save(payload)

func fetch_leaderboard(board_id: String) -> Dictionary:
    return await service.fetch_leaderboard(board_id)

func fetch_missions() -> Array:
    return await service.fetch_missions()

func submit_analytics_event(event_name: String, payload: Dictionary) -> void:
    await service.submit_analytics_event(event_name, payload)

func report_exception(context: Dictionary) -> void:
    await service.report_exception(context)
