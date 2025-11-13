# EventBus

The EventBus (`addons/universal_framework/core/event_bus.gd`) implements a lightweight publish/subscribe mechanism used throughout the framework.

## Key features

- String-based topics with optional wildcard (`*`).
- Support for one-shot listeners (`once()` helper).
- Automatic cleanup of invalid callables.
- Payloads are plain dictionaries with the originating `event_name` attached.

## Usage

```gdscript
var callback := Callable(self, "_on_purchase")
GameApp.event_bus.subscribe("pay.purchase_succeeded", callback)
GameApp.event_bus.publish("pay.purchase_succeeded", {"product_id": "starter_pack"})
GameApp.event_bus.unsubscribe("pay.purchase_succeeded", callback)
```

Listeners should expect a dictionary payload. The published payload is always duplicated to avoid accidental shared-state mutations.

## Best practices

- Prefix topics with the module (`auth.*`, `pay.*`, `net.*`) for discoverability.
- Include enough metadata in payloads to be self-descriptive (`product_id`, `rewards`, `timestamp`).
- Use the event bus for cross-module communication instead of direct method calls to maintain loose coupling.
- Remember to unsubscribe long-lived callables on cleanup (e.g., when removing UI nodes).
