extends Node
class_name LocalizationManager

signal locale_changed(locale_code: String)

var supported_locales := ["en", "zh-Hans", "ja"]
var current_locale: String = "en"
var translations: Dictionary = {}

func load_translations(data: Dictionary) -> void:
    translations = data

func translate(key: String, fallback: String = "") -> String:
    var locale_data: Dictionary = translations.get(current_locale, {})
    return locale_data.get(key, fallback if fallback != "" else key)

func set_locale(locale_code: String) -> void:
    if locale_code not in supported_locales:
        push_warning("Unsupported locale %s" % locale_code)
        return
    current_locale = locale_code
    locale_changed.emit(locale_code)
