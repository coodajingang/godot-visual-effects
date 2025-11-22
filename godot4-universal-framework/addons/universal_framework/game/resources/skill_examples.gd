extends Resource
class_name SkillExamples

static func create_all_example_skills() -> Array[SkillDefinition]:
	var skills: Array[SkillDefinition] = []
	skills.append(create_crit_chance_skill())
	skills.append(create_extra_projectiles_skill())
	skills.append(create_nova_burst_skill())
	skills.append(create_lifesteal_skill())
	skills.append(create_armor_piercing_skill())
	skills.append(create_speed_boost_skill())
	skills.append(create_double_damage_skill())
	return skills

static func create_crit_chance_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "crit_chance"
	skill.name = "Sharpened Edge"
	skill.description = "Increase critical strike chance"
	skill.skill_type = "passive"
	skill.weight = 1.0
	skill.max_stacks = 3
	skill.stat_modifiers = {
		"critical_chance": {
			"type": "add",
			"value": 0.08
		}
	}
	return skill

static func create_extra_projectiles_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "extra_projectiles"
	skill.name = "Multishot"
	skill.description = "Fire additional projectiles per attack"
	skill.skill_type = "passive"
	skill.weight = 0.9
	skill.max_stacks = 2
	skill.stat_modifiers = {
		"damage": {
			"type": "mult",
			"value": 0.85
		}
	}
	return skill

static func create_nova_burst_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "nova_burst"
	skill.name = "Nova Burst"
	skill.description = "Emit a shockwave dealing AOE damage"
	skill.skill_type = "active"
	skill.weight = 0.7
	skill.max_stacks = 1
	skill.stat_modifiers = {
		"damage": {
			"type": "mult",
			"value": 1.2
		}
	}
	return skill

static func create_lifesteal_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "lifesteal"
	skill.name = "Blood Thirst"
	skill.description = "Restore HP when dealing damage"
	skill.skill_type = "passive"
	skill.weight = 1.0
	skill.max_stacks = 2
	skill.stat_modifiers = {
		"damage": {
			"type": "mult",
			"value": 1.1
		}
	}
	return skill

static func create_armor_piercing_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "armor_piercing"
	skill.name = "Armor Piercing"
	skill.description = "Ignore enemy armor"
	skill.skill_type = "passive"
	skill.weight = 0.8
	skill.max_stacks = 1
	skill.stat_modifiers = {
		"damage": {
			"type": "mult",
			"value": 1.15
		}
	}
	return skill

static func create_speed_boost_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "speed_boost"
	skill.name = "Swift Feet"
	skill.description = "Increase movement speed"
	skill.skill_type = "passive"
	skill.weight = 1.0
	skill.max_stacks = 3
	skill.stat_modifiers = {
		"move_speed": {
			"type": "mult",
			"value": 1.12
		}
	}
	return skill

static func create_double_damage_skill() -> SkillDefinition:
	var skill = SkillDefinition.new()
	skill.skill_id = "double_damage"
	skill.name = "Rage"
	skill.description = "Significant damage boost"
	skill.skill_type = "passive"
	skill.weight = 0.5
	skill.max_stacks = 1
	skill.stat_modifiers = {
		"damage": {
			"type": "mult",
			"value": 1.5
		}
	}
	return skill
