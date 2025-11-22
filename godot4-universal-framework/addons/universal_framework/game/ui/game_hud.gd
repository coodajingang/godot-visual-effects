extends CanvasLayer
class_name GameHUD

@onready var level_label: Label = %LevelLabel
@onready var xp_bar: ProgressBar = %XPBar
@onready var xp_text: Label = %XPText
@onready var skills_container: HBoxContainer = %SkillsContainer
@onready var buffs_container: HBoxContainer = %BuffsContainer

var _experience_manager: ExperienceManager
var _skill_service: SkillService

func _ready() -> void:
	_update_all()

func setup(experience_manager: ExperienceManager, skill_service: SkillService) -> void:
	_experience_manager = experience_manager
	_skill_service = skill_service
	
	if _experience_manager:
		_experience_manager.level_up.connect(_on_level_up)
		_experience_manager.xp_gained.connect(_on_xp_gained)
		_experience_manager.level_progress_changed.connect(_on_xp_progress_changed)
	
	if _skill_service:
		_skill_service.skill_applied.connect(_on_skill_applied)
	
	_update_all()

func _on_level_up(new_level: int, _remaining_xp: int) -> void:
	_update_level_label()
	_update_xp_bar()

func _on_xp_gained(_amount: int, _current_xp: int, _total_xp: int) -> void:
	_update_xp_bar()

func _on_xp_progress_changed(current_xp: int, xp_needed: int) -> void:
	_update_xp_bar()

func _on_skill_applied(skill: SkillDefinition, _stats: HeroStats) -> void:
	_update_skills_display()

func _update_all() -> void:
	_update_level_label()
	_update_xp_bar()
	_update_skills_display()

func _update_level_label() -> void:
	if not _experience_manager or not level_label:
		return
	
	var level = _experience_manager.get_current_level()
	level_label.text = "Level: %d" % level

func _update_xp_bar() -> void:
	if not _experience_manager:
		return
	
	if xp_bar:
		xp_bar.value = _experience_manager.get_xp_progress_percentage() * 100
	
	if xp_text:
		var current = _experience_manager.get_current_xp()
		var needed = _experience_manager.get_xp_needed_for_next_level()
		xp_text.text = "%d / %d" % [current, needed]

func _update_skills_display() -> void:
	if not _skill_service or not skills_container:
		return
	
	for child in skills_container.get_children():
		child.queue_free()
	
	var applied_skills = _skill_service.get_all_applied_skills()
	for skill_id in applied_skills:
		var count = applied_skills[skill_id]
		var label = Label.new()
		label.text = "%s x%d" % [skill_id, count]
		skills_container.add_child(label)

func resize_gracefully() -> void:
	if not is_node_ready():
		return
	
	var viewport_size = get_viewport_rect().size
	
	if level_label:
		level_label.custom_minimum_size = Vector2(0, 0)
	
	if xp_bar:
		xp_bar.custom_minimum_size = Vector2(min(300, viewport_size.x * 0.3), 20)
	
	if skills_container:
		skills_container.custom_minimum_size = Vector2(0, 0)
