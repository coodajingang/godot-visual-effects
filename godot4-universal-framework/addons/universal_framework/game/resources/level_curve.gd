extends Resource
class_name LevelCurve

@export var xp_thresholds: Array[int] = [
	100,
	250,
	450,
	700,
	1000,
	1350,
	1750,
	2200,
	2700,
	3250
]

@export var hp_scaling: float = 1.1
@export var damage_scaling: float = 1.05
@export var move_speed_scaling: float = 1.02

func get_xp_for_level(level: int) -> int:
	if level <= 0:
		return 0
	if level - 1 < xp_thresholds.size():
		return xp_thresholds[level - 1]
	
	var base_xp: int = xp_thresholds[-1] if xp_thresholds.size() > 0 else 100
	return int(base_xp * pow(1.15, level - xp_thresholds.size()))

func apply_level_scaling(base_stats: HeroStats, level: int) -> HeroStats:
	var scaled_stats = base_stats.duplicate_deep()
	var level_bonus = level - 1.0
	
	scaled_stats.max_hp *= pow(hp_scaling, level_bonus)
	scaled_stats.damage *= pow(damage_scaling, level_bonus)
	scaled_stats.move_speed *= pow(move_speed_scaling, level_bonus)
	
	return scaled_stats

func get_total_xp_for_level(level: int) -> int:
	var total: int = 0
	for i in range(1, level):
		total += get_xp_for_level(i)
	return total
