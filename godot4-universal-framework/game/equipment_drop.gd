extends Area2D
class_name EquipmentDrop

signal picked_up(item: EquipmentItem)

@export var equipment_item: EquipmentItem
@export var auto_pickup: bool = true
@export var pickup_range: float = 50.0
@export var float_animation: bool = true
@export var float_speed: float = 2.0
@export var float_amplitude: float = 10.0

var initial_y: float = 0.0
var time: float = 0.0
var is_being_collected: bool = false

@onready var sprite: Sprite2D = $Sprite2D
@onready var label: Label = $Label

func _ready() -> void:
    initial_y = global_position.y
    monitoring = true
    monitorable = true
    
    if equipment_item:
        _update_visuals()
    
    body_entered.connect(_on_body_entered)

func _process(delta: float) -> void:
    if float_animation and not is_being_collected:
        time += delta * float_speed
        global_position.y = initial_y + sin(time) * float_amplitude

func set_equipment_item(item: EquipmentItem) -> void:
    equipment_item = item
    if is_inside_tree():
        _update_visuals()

func _update_visuals() -> void:
    if equipment_item and sprite:
        if equipment_item.icon:
            sprite.texture = equipment_item.icon
        sprite.modulate = equipment_item.get_rarity_color()
    
    if equipment_item and label:
        label.text = equipment_item.display_name
        label.modulate = equipment_item.get_rarity_color()

func _on_body_entered(body: Node2D) -> void:
    if is_being_collected:
        return
    
    if body.has_method("get_equipment_manager"):
        _pickup(body)
    elif auto_pickup and body.is_in_group("player"):
        _pickup(body)

func _pickup(collector: Node2D) -> void:
    if not equipment_item:
        return
    
    is_being_collected = true
    monitoring = false
    
    var equipment_manager: EquipmentManager = null
    if collector.has_method("get_equipment_manager"):
        equipment_manager = collector.get_equipment_manager()
    elif GameApp.has_method("get_equipment_manager"):
        equipment_manager = GameApp.get_equipment_manager()
    
    if equipment_manager:
        equipment_manager.add_to_inventory(equipment_item.duplicate())
    
    picked_up.emit(equipment_item)
    GameApp.event_bus.publish("equipment.item_picked_up", {
        "item_id": equipment_item.item_id,
        "item_name": equipment_item.display_name,
        "rarity": equipment_item.get_rarity_name()
    })
    
    _play_pickup_animation()

func _play_pickup_animation() -> void:
    var tween := create_tween()
    tween.set_parallel(true)
    tween.tween_property(self, "global_position:y", global_position.y - 50, 0.3)
    tween.tween_property(self, "modulate:a", 0.0, 0.3)
    tween.finished.connect(queue_free)
