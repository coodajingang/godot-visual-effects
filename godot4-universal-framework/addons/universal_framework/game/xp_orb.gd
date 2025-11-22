extends Area2D
class_name XPOrb

signal collected(amount: int)

@export var xp_amount: int = 10
@export var move_speed: float = 200.0
@export var despawn_time: float = 30.0

var _target: Node2D = null
var _time_alive: float = 0.0

func _ready() -> void:
	area_entered.connect(_on_area_entered)
	var timer = Timer.new()
	add_child(timer)
	timer.wait_time = despawn_time
	timer.timeout.connect(_on_despawn_timer)
	timer.start()

func _physics_process(delta: float) -> void:
	_time_alive += delta
	
	if _target and is_instance_valid(_target):
		var direction = (_target.global_position - global_position).normalized()
		global_position += direction * move_speed * delta

func set_target(target: Node2D) -> void:
	_target = target

func set_xp_amount(amount: int) -> void:
	xp_amount = amount

func _on_area_entered(area: Area2D) -> void:
	if area and area.is_in_group("xp_collectors"):
		collected.emit(xp_amount)
		queue_free()

func _on_despawn_timer() -> void:
	queue_free()
