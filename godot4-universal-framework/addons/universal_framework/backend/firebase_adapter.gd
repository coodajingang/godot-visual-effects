extends BackendAdapter
class_name FirebaseAdapter

## Firebase implementation of the backend adapter.

var configuration: Dictionary = {}

func setup(config: Dictionary) -> void:
    configuration = config

func login_with_email(email: String, password: String) -> Dictionary:
    var result = await super.login_with_email(email, password)
    result["provider"] = "firebase"
    return result

func submit_analytics_event(event_name: String, payload: Dictionary) -> void:
    payload["provider"] = "firebase"
    await super.submit_analytics_event(event_name, payload)
