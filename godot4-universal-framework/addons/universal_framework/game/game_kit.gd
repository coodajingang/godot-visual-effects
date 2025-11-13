extends BaseModule
class_name GameKit

signal battle_completed(summary: Dictionary)
signal stage_progressed(stage_info: Dictionary)
signal task_updated(task_info: Dictionary)

var active_stage: Dictionary = {
    "chapter": 1,
    "stage": 1
}

func start_battle(context: Dictionary) -> void:
    event_bus.publish("game.battle_started", context)
    DebugConsole.info("Battle started", context)

func complete_battle(result: Dictionary) -> Dictionary:
    var data_kit: DataKit = game_app.get_data()
    var profile := data_kit.get_profile()
    var exp_gain: int = result.get("experience", 0)
    profile["experience"] = profile.get("experience", 0) + exp_gain
    if profile.get("experience", 0) > profile.get("level", 1) * 100:
        profile["level"] += 1
        profile["experience"] = 0
    data_kit.cache_profile(profile)

    var cloud := data_kit.get_cloud_save()
    cloud["progress"] = result.get("progress", cloud.get("progress", {}))
    data_kit.update_cloud_save(cloud)

    var summary = {
        "experience": exp_gain,
        "loot": result.get("loot", []),
        "profile": profile
    }
    battle_completed.emit(summary)
    event_bus.publish("game.battle_completed", summary)
    return summary

func advance_stage() -> Dictionary:
    active_stage["stage"] += 1
    if active_stage["stage"] > 10:
        active_stage["chapter"] += 1
        active_stage["stage"] = 1
    stage_progressed.emit(active_stage)
    event_bus.publish("game.stage_progressed", active_stage)
    return active_stage

func apply_buff_formula(base_value: float, modifiers: Array) -> float:
    var value: float = base_value
    for modifier in modifiers:
        match modifier.get("type", "add"):
            "add":
                value += modifier.get("value", 0.0)
            "mult":
                value *= modifier.get("value", 1.0)
            "percent":
                value += base_value * modifier.get("value", 0.0)
    return value

func schedule_activity(activity_id: String, parameters: Dictionary) -> void:
    event_bus.publish("game.activity_scheduled", {
        "activity_id": activity_id,
        "parameters": parameters
    })

func update_task_progress(task_id: String, progress: int, goal: int) -> void:
    var payload := {
        "task_id": task_id,
        "progress": progress,
        "goal": goal
    }
    task_updated.emit(payload)
    event_bus.publish("game.task_updated", payload)
