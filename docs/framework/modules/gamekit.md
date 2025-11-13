# GameKit

GameKit aggregates gameplay-centric helpers so higher level systems can stay lightweight. It offers battle settlement utilities, progression updates, buff calculation helpers, and a simple scheduler for time-based activities.

## Responsibilities

- Track active stage/chapter progression.
- Calculate experience gains and level ups after battles.
- Merge loot results into DataKit-provided inventories.
- Publish gameplay milestones (battle completed, stage progressed, task updated).

## API surface

```gdscript
GameApp.get_game().start_battle({"stage": 1, "team_power": 4800})
var summary := GameApp.get_game().complete_battle({
    "experience": 120,
    "loot": [{"id": "potion_small", "quantity": 1}],
    "progress": {"chapter": 1, "stage": 2}
})
var buffed := GameApp.get_game().apply_buff_formula(100.0, [
    {"type": "add", "value": 10},
    {"type": "percent", "value": 0.2}
])
GameApp.get_game().schedule_activity("double_drop_weekend", {"start": 1700000000})
GameApp.get_game().update_task_progress("daily_login", 1, 1)
```

## EventBus topics

| Event | Payload |
|-------|---------|
| `game.battle_started` | `{ stage, team_power, ... }`
| `game.battle_completed` | `{ experience, loot, profile }`
| `game.stage_progressed` | `{ chapter, stage }`
| `game.activity_scheduled` | `{ activity_id, parameters }`
| `game.task_updated` | `{ task_id, progress, goal }`

## Signals

- `battle_completed(summary: Dictionary)`
- `stage_progressed(stage_info: Dictionary)`
- `task_updated(task_info: Dictionary)`

## Extending GameKit

- Replace the simplistic leveling curve with data-driven formulas (e.g., fetched from `DataKit` configs).
- Feed battle telemetry into `LogKit` for analytics dashboards.
- Integrate with server-authoritative combat by relaying results received from NetKit or WebSocket streams.
