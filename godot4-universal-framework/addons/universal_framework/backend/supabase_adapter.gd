extends BackendAdapter
class_name SupabaseAdapter

## Supabase implementation of the backend adapter.

var configuration: Dictionary = {}

func setup(config: Dictionary) -> void:
    configuration = config

func login_with_email(email: String, password: String) -> Dictionary:
    var result = await super.login_with_email(email, password)
    result["provider"] = "supabase"
    return result

func purchase_product(product_id: String) -> Dictionary:
    var result = await super.purchase_product(product_id)
    if result.get("success", false):
        await submit_analytics_event("supabase_purchase", {"product_id": product_id})
    return result
