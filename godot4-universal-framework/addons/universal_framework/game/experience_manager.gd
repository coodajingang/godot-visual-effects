extends Node
class_name ExperienceManager

signal xp_gained(amount: int, current_xp: int, total_xp: int)
signal level_up(new_level: int, remaining_xp: int)
signal level_progress_changed(current_xp: int, xp_needed: int)

var current_level: int = 1
var current_xp: int = 0
var level_curve: LevelCurve

var _base_stats: HeroStats
var _current_stats: HeroStats

func _init(base_stats: HeroStats = null, level_curve_resource: LevelCurve = null) -> void:
	_base_stats = base_stats or HeroStats.new()
	level_curve = level_curve_resource or LevelCurve.new()
	_current_stats = _base_stats.duplicate_deep()
	
func add_xp(amount: int) -> void:
	if amount <= 0:
		return
	
	current_xp += amount
	xp_gained.emit(amount, current_xp, level_curve.get_total_xp_for_level(current_level) + current_xp)
	
	_check_level_up()

func _check_level_up() -> void:
	while current_xp >= level_curve.get_xp_for_level(current_level):
		current_xp -= level_curve.get_xp_for_level(current_level)
		current_level += 1
		_update_stats_for_level()
		level_up.emit(current_level, current_xp)
	
	level_progress_changed.emit(current_xp, level_curve.get_xp_for_level(current_level))

func _update_stats_for_level() -> void:
	_current_stats = level_curve.apply_level_scaling(_base_stats, current_level)

func get_current_level() -> int:
	return current_level

func get_current_xp() -> int:
	return current_xp

func get_xp_needed_for_next_level() -> int:
	return level_curve.get_xp_for_level(current_level)

func get_xp_progress_percentage() -> float:
	var xp_needed = get_xp_needed_for_next_level()
	if xp_needed <= 0:
		return 0.0
	return float(current_xp) / float(xp_needed)

func get_current_stats() -> HeroStats:
	return _current_stats

func set_base_stats(stats: HeroStats) -> void:
	_base_stats = stats
	_update_stats_for_level()

func reset_level() -> void:
	current_level = 1
	current_xp = 0
	_current_stats = _base_stats.duplicate_deep()
	level_progress_changed.emit(current_xp, level_curve.get_xp_for_level(current_level))
