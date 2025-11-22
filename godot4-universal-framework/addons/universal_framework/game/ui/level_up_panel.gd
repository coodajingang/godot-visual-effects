extends Control
class_name LevelUpPanel

signal skill_selected(skill: SkillDefinition)

@onready var level_label: Label = %LevelLabel
@onready var skill_buttons: VBoxContainer = %SkillButtons
@onready var title_label: Label = %TitleLabel

var _skills: Array[SkillDefinition] = []
var _selected_skill: SkillDefinition = null
var _skill_button_group: ButtonGroup = ButtonGroup.new()
var _current_skill_index: int = 0

func _ready() -> void:
	get_tree().paused = true
	%BackgroundPanel.gui_input.connect(_on_background_clicked)

func _input(event: InputEvent) -> void:
	if not visible:
		return
	
	if event is InputEventKey and event.pressed:
		match event.keycode:
			KEY_UP:
				_select_previous_skill()
				get_tree().root.set_input_as_handled()
			KEY_DOWN:
				_select_next_skill()
				get_tree().root.set_input_as_handled()
			KEY_1:
				if _skills.size() > 0:
					_confirm_skill(0)
					get_tree().root.set_input_as_handled()
			KEY_2:
				if _skills.size() > 1:
					_confirm_skill(1)
					get_tree().root.set_input_as_handled()
			KEY_3:
				if _skills.size() > 2:
					_confirm_skill(2)
					get_tree().root.set_input_as_handled()
			KEY_ENTER:
				_confirm_current_skill()
				get_tree().root.set_input_as_handled()

func display_level_up(level: int, skills: Array[SkillDefinition]) -> void:
	_skills = skills
	_selected_skill = null
	_current_skill_index = 0
	
	title_label.text = "LEVEL UP!"
	level_label.text = "Level %d" % level
	
	_render_skills()
	
	if skill_buttons.get_child_count() > 0:
		_select_skill_at_index(0)

func _render_skills() -> void:
	for child in skill_buttons.get_children():
		child.queue_free()
	
	for i in range(_skills.size()):
		var skill = _skills[i]
		var button = _create_skill_button(skill, i)
		skill_buttons.add_child(button)

func _create_skill_button(skill: SkillDefinition, index: int) -> Button:
	var button = Button.new()
	button.add_to_group("skill_buttons")
	button.button_group = _skill_button_group
	button.toggle_mode = true
	
	var button_text = "[%d] %s\n" % [index + 1, skill.get_display_name()]
	button_text += skill.get_display_description()
	if not skill.get_stat_preview().is_empty():
		button_text += "\n" + skill.get_stat_preview()
	
	button.text = button_text
	button.custom_minimum_size = Vector2(0, 80)
	button.pressed.connect(_on_skill_button_pressed.bind(index))
	
	return button

func _select_skill_at_index(index: int) -> void:
	if index < 0 or index >= _skills.size():
		return
	
	_current_skill_index = index
	var buttons = get_tree().get_nodes_in_group("skill_buttons")
	if index < buttons.size():
		var button = buttons[index] as Button
		button.button_pressed = true
		_selected_skill = _skills[index]

func _select_previous_skill() -> void:
	var new_index = _current_skill_index - 1
	if new_index < 0:
		new_index = _skills.size() - 1
	_select_skill_at_index(new_index)

func _select_next_skill() -> void:
	var new_index = _current_skill_index + 1
	if new_index >= _skills.size():
		new_index = 0
	_select_skill_at_index(new_index)

func _on_skill_button_pressed(index: int) -> void:
	_select_skill_at_index(index)
	_confirm_skill(index)

func _confirm_current_skill() -> void:
	if _selected_skill:
		skill_selected.emit(_selected_skill)
		_close_panel()

func _confirm_skill(index: int) -> void:
	if index >= 0 and index < _skills.size():
		skill_selected.emit(_skills[index])
		_close_panel()

func _close_panel() -> void:
	get_tree().paused = false
	queue_free()

func _on_background_clicked(_event: InputEvent) -> void:
	pass
