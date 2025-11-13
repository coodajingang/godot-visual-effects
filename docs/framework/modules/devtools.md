# Developer Tooling

The framework bundles small helpers to streamline debugging and live operations experimentation.

## DebugConsole

- Autoload located at `addons/universal_framework/devtools/debug_console.gd`.
- Stores the latest `max_entries` log entries in memory.
- Emits `entry_added(entry)` whenever a new message arrives.
- Used by LogKit and MockBackendService to capture analytics events, exceptions, and custom traces.

Hook example:

```gdscript
DebugConsole.entry_added.connect(func(entry):
    print("[DEBUG] %s: %s" % [entry.get("timestamp"), entry.get("message", entry.get("event", ""))])
)
```

## Event inspection

Because every module emits EventBus topics, you can attach a temporary listener during development:

```gdscript
GameApp.event_bus.subscribe("*", func(payload):
    DebugConsole.info("EventBus", payload)
)
```

## Hot reload / config tweaking

The repository does not include a full hot-reload tool by default, but the directory structure leaves room to add one:

1. Create `addons/universal_framework/devtools/hot_reload.gd`.
2. Watch `res://config/` or remote endpoints for updates.
3. Publish `config.updated` events carrying the new configuration to interested kits.

## Build metadata

Expose build numbers or commit hashes by extending `GameApp.config` at build time. For example, run a CI script that writes `res://build_info.json` and load it on startup to display Version/Channel in the UI.
