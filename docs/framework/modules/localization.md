# Localization Manager

The LocalizationManager (`addons/universal_framework/localization/localization_manager.gd`) keeps track of the active locale and maps translation keys to strings. It is intentionally simple so projects can replace it with Godot's built-in translation system if preferred.

## Features

- Tracks current locale (`en`, `zh-Hans`, `ja` by default).
- Emits `locale_changed(locale_code)` when the language is switched.
- Provides a `translate(key, fallback)` helper for lightweight lookups.

## Example

```gdscript
var text := GameApp.localization_manager.translate("store_button", "Open Store")
GameApp.localization_manager.set_locale("zh-Hans")
```

## Extending

- Load `.po` or `.csv` files during `_initialize_runtime()` and feed them into `load_translations()`.
- Tie the locale change signal to UI refreshers so labels redraw automatically.
- Expose per-player locale preferences through DataKit and persist them in the backend.
