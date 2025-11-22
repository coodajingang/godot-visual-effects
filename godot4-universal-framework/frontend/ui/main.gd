extends Control

@onready var email_input: LineEdit = %EmailInput
@onready var password_input: LineEdit = %PasswordInput
@onready var login_button: Button = %LoginButton
@onready var guest_button: Button = %GuestButton
@onready var status_label: Label = %StatusLabel
@onready var profile_panel: PanelContainer = %ProfilePanel
@onready var nickname_label: Label = %NicknameLabel
@onready var level_label: Label = %LevelLabel
@onready var currency_label: Label = %CurrencyLabel
@onready var store_button: Button = %StoreButton
@onready var sync_button: Button = %SyncButton
@onready var store_panel: Panel = %StorePanel
@onready var product_list: VBoxContainer = %ProductList
@onready var purchase_feedback: Label = %PurchaseFeedback
@onready var close_store_button: Button = %CloseStoreButton
@onready var equipment_button: Button = %EquipmentButton

var _catalog: Array = []
var _equipment_panel: Control = null

func _ready() -> void:
    profile_panel.visible = false
    store_panel.visible = false
    purchase_feedback.text = ""

    login_button.pressed.connect(_on_login_pressed)
    guest_button.pressed.connect(_on_guest_login_pressed)
    store_button.pressed.connect(_on_store_button_pressed)
    sync_button.pressed.connect(_on_sync_button_pressed)
    close_store_button.pressed.connect(_on_close_store_pressed)
    
    if equipment_button:
        equipment_button.pressed.connect(_on_equipment_button_pressed)
    
    _setup_equipment_panel()

    GameApp.event_bus.subscribe("auth.login_succeeded", Callable(self, "_on_login_succeeded"))
    GameApp.event_bus.subscribe("datakit.cloud_synced", Callable(self, "_on_cloud_synced"))
    GameApp.event_bus.subscribe("pay.purchase_succeeded", Callable(self, "_on_purchase_succeeded"))
    GameApp.event_bus.subscribe("pay.purchase_failed", Callable(self, "_on_purchase_failed"))
    GameApp.event_bus.subscribe("equipment.currency_changed", Callable(self, "_on_equipment_currency_changed"))

func _on_login_pressed() -> void:
    _perform_login(email_input.text.strip_edges(), password_input.text)

func _on_guest_login_pressed() -> void:
    login_button.disabled = true
    guest_button.disabled = true
    status_label.text = "Signing in as guest..."
    var result := await GameApp.get_auth().login_guest()
    _handle_login_result(result)

func _perform_login(email: String, password: String) -> void:
    login_button.disabled = true
    guest_button.disabled = true
    if email.is_empty():
        email = "player@example.com"
    if password.is_empty():
        password = "password123"
    status_label.text = "Signing in..."
    var result := await GameApp.get_auth().login_with_email(email, password)
    _handle_login_result(result)

func _handle_login_result(result: Dictionary) -> void:
    login_button.disabled = false
    guest_button.disabled = false
    if result.get("success", true) == false:
        status_label.text = "Login failed: %s" % result.get("error", "Unknown")
        return
    status_label.text = "Welcome, %s" % result.get("nickname", "Player")
    profile_panel.visible = true
    _update_profile_panel()

func _update_profile_panel() -> void:
    var profile := GameApp.get_data().get_profile()
    nickname_label.text = "Nickname: %s" % profile.get("nickname", "-")
    level_label.text = "Level: %s | EXP: %s" % [str(profile.get("level", 1)), str(profile.get("experience", 0))]
    var currencies: Dictionary = profile.get("currencies", {})
    currency_label.text = "Gold: %s | Gems: %s" % [str(currencies.get("gold", 0)), str(currencies.get("gems", 0))]

func _on_store_button_pressed() -> void:
    store_panel.visible = true
    purchase_feedback.text = "Loading store..."
    _catalog = await GameApp.get_pay().load_catalog()
    _render_catalog()
    purchase_feedback.text = ""

func _render_catalog() -> void:
    for child in product_list.get_children():
        child.queue_free()
    for product in _catalog:
        var button := Button.new()
        button.text = "%s - $%s" % [product.get("name", "Unknown"), product.get("price", 0.0)]
        button.tooltip_text = product.get("description", "")
        button.pressed.connect(_on_product_pressed.bind(product.get("id", "")))
        product_list.add_child(button)

func _on_product_pressed(product_id: String) -> void:
    if product_id.is_empty():
        return
    purchase_feedback.text = "Processing %s..." % product_id
    var result := await GameApp.get_pay().purchase_product(product_id)
    if not result.get("success", false):
        purchase_feedback.text = "Purchase failed"

func _on_close_store_pressed() -> void:
    store_panel.visible = false

func _on_sync_button_pressed() -> void:
    purchase_feedback.text = "Syncing cloud save..."
    await GameApp.get_data().synchronize_cloud_save()
    purchase_feedback.text = "Cloud save synced"

func _on_login_succeeded(_payload: Dictionary) -> void:
    _update_profile_panel()

func _on_cloud_synced(payload: Dictionary) -> void:
    purchase_feedback.text = "Cloud sync complete"
    _update_profile_panel()

func _on_purchase_failed(payload: Dictionary) -> void:
    purchase_feedback.text = "Purchase failed: %s" % payload.get("error", "UNKNOWN")

func _on_purchase_succeeded(payload: Dictionary) -> void:
    var rewards := payload.get("rewards", {})
    purchase_feedback.text = "Purchase successful! Rewards: %s" % JSON.stringify(rewards)
    _update_profile_panel()

func _setup_equipment_panel() -> void:
    var equipment_panel_scene := load("res://ui/equipment_panel.tscn")
    if equipment_panel_scene:
        _equipment_panel = equipment_panel_scene.instantiate()
        add_child(_equipment_panel)
        _equipment_panel.hide()

func _on_equipment_button_pressed() -> void:
    if _equipment_panel:
        if _equipment_panel.visible:
            _equipment_panel.hide()
        else:
            _equipment_panel.call("open_panel")

func _on_equipment_currency_changed(payload: Dictionary) -> void:
    purchase_feedback.text = "Equipment currency: %d" % payload.get("currency", 0)
