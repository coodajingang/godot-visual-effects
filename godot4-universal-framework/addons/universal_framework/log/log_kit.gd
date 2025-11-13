extends BaseModule
class_name LogKit

signal analytics_event_sent(event_name: String, payload: Dictionary)
signal exception_reported(details: Dictionary)

func log_event(event_name: String, payload: Dictionary = {}) -> void:
    payload["event_name"] = event_name
    payload["sent_at"] = Time.get_datetime_string_from_system()
    DebugConsole.info("Analytics event", payload)
    await backend_adapter.submit_analytics_event(event_name, payload)
    analytics_event_sent.emit(event_name, payload)
    event_bus.publish("log.analytics_event_sent", payload)

func report_exception(details: Dictionary) -> void:
    DebugConsole.error("Exception", details)
    await backend_adapter.report_exception(details)
    exception_reported.emit(details)
    event_bus.publish("log.exception_reported", details)

func trace_gameplay_flow(flow_step: String, extras: Dictionary = {}) -> void:
    var payload := {
        "flow_step": flow_step,
        "extras": extras
    }
    await log_event("gameplay_flow", payload)
