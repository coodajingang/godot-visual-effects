extends Resource
class_name HeroStats

@export var max_hp: float = 100.0
@export var damage: float = 10.0
@export var move_speed: float = 300.0
@export var attack_speed: float = 1.0
@export var critical_chance: float = 0.05
@export var critical_multiplier: float = 1.5
@export var armor: float = 0.0
@export var dodge_chance: float = 0.0

func _init(p_max_hp: float = 100.0, p_damage: float = 10.0, p_move_speed: float = 300.0) -> void:
	max_hp = p_max_hp
	damage = p_damage
	move_speed = p_move_speed

func duplicate_deep() -> HeroStats:
	var new_stats = HeroStats.new()
	new_stats.max_hp = max_hp
	new_stats.damage = damage
	new_stats.move_speed = move_speed
	new_stats.attack_speed = attack_speed
	new_stats.critical_chance = critical_chance
	new_stats.critical_multiplier = critical_multiplier
	new_stats.armor = armor
	new_stats.dodge_chance = dodge_chance
	return new_stats
