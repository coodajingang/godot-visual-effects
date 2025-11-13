# LogKit

LogKit funnels analytics events, runtime traces, and exception reports into your analytics provider (Firebase Analytics by default). It also mirrors events into the in-engine DebugConsole for quick inspection.

## Responsibilities

- Normalize gameplay events before sending them to the backend adapter.
- Report handled and unhandled exceptions with contextual metadata.
- Offer convenience helpers for instrumenting gameplay flows.

## API surface

```gdscript
await GameApp.get_log().log_event("store_view", {"source": "main_menu"})
await GameApp.get_log().report_exception({"message": "Inventory overflow", "context": stack_trace})
await GameApp.get_log().trace_gameplay_flow("chapter_complete", {"chapter": 3})
```

## EventBus topics

| Event | Payload |
|-------|---------|
| `log.analytics_event_sent` | `{ event_name, ... }`
| `log.exception_reported` | `{ message, stacktrace? }`

## Signals

- `analytics_event_sent(event_name: String, payload: Dictionary)`
- `exception_reported(details: Dictionary)`

## Firebase Analytics integration

Implement `BackendAdapter.submit_analytics_event` to call [Analytics REST Measurement Protocol](https://firebase.google.com/docs/analytics/measure-events?platform=web) or bridge to a native SDK. LogKit already packages timestamps and custom parameters for you.

## Extending LogKit

- Add local persistence for offline event queues.
- Tie into crash reporters (Sentry, Bugsnag) by forwarding `report_exception` payloads.
- Offer sampling & rate limiting for verbose telemetry channels.
