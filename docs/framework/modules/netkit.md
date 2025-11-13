# NetKit

NetKit abstracts HTTP and WebSocket communication for the framework. In the demo it proxies calls to the backend adapter, but in production it can host HTTP clients, websocket managers, retry strategies, and request queues.

## Responsibilities

- Provide a single entry point for outbound network requests.
- Emit lifecycle events for analytics or debugging.
- Support periodic polling/watch functionality.

## API surface

```gdscript
var response := await GameApp.get_net().request("profile")
GameApp.get_net().watch("leaderboard/global", 10.0, Callable(self, "_on_leaderboard"))
```

`request(endpoint, method := "GET", payload := {})` returns a dictionary. In the template implementation the endpoint is matched against known mock handlers.

## EventBus topics

| Event | Payload |
|-------|---------|
| `net.request_started` | `{ endpoint, method }`
| `net.request_completed` | `{ endpoint, data }`
| `net.request_failed` | `{ endpoint, error }`

## Extending NetKit

1. Replace the mock switch with real HTTP requests (use `HTTPRequest` or a custom GDNative module).
2. Implement exponential backoff and failure classification.
3. Provide WebSocket helpers that map incoming server pushes into EventBus topics (`net.stream_update`).
4. Integrate network diagnostics (latency logging, packet compression stats) for the debug console.
