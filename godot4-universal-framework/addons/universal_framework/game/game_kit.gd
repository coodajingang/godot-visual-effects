extends BaseModule
class_name GameKit

signal battle_completed(summary: Dictionary)
signal stage_progressed(stage_info: Dictionary)
signal task_updated(task_info: Dictionary)
signal level_up_triggered(level: int)
signal skill_selection_required(skills: Array)

var active_stage: Dictionary = {
    "chapter": 1,
    "stage": 1
}

var experience_manager: ExperienceManager
var skill_service: SkillService
var level_curve: LevelCurve
var hero_stats: HeroStats

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
    
    var equipment_manager = game_app.get_equipment_manager()
    if equipment_manager and result.has("currency"):
        equipment_manager.add_currency(result.get("currency", 0))

    var summary = {
        "experience": exp_gain,
        "loot": result.get("loot", []),
        "currency": result.get("currency", 0),
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

func initialize_leveling_system(base_stats: HeroStats = null, curve: LevelCurve = null) -> void:
    hero_stats = base_stats or HeroStats.new()
    level_curve = curve or LevelCurve.new()
    experience_manager = ExperienceManager.new(hero_stats, level_curve)
    add_child(experience_manager)
    
    skill_service = SkillService.new(hero_stats)
    skill_service.setup(game_app)
    add_child(skill_service)
    
    experience_manager.level_up.connect(_on_hero_level_up)

func _on_hero_level_up(new_level: int, _remaining_xp: int) -> void:
    level_up_triggered.emit(new_level)
    event_bus.publish("game.hero_level_up", {
        "level": new_level,
        "timestamp": Time.get_ticks_msec()
    })

func get_experience_manager() -> ExperienceManager:
    return experience_manager

func get_skill_service() -> SkillService:
    return skill_service

func get_hero_stats() -> HeroStats:
    if experience_manager:
        return experience_manager.get_current_stats()
    return hero_stats
