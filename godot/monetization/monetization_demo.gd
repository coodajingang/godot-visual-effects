extends Control

const CONFIG_PATH := "res://monetization/config/demo_profile.tres"

@onready var manager: MonetizationManager = $MonetizationManager
@onready var ad_selector: OptionButton = %AdOptionButton
@onready var ad_info: RichTextLabel = %AdInfo
@onready var ad_button: Button = %AdShowButton
@onready var product_selector: OptionButton = %ProductOptionButton
@onready var product_info: RichTextLabel = %ProductInfo
@onready var purchase_button: Button = %PurchaseButton
@onready var store_selector: OptionButton = %StoreOptionButton
@onready var store_info: RichTextLabel = %StoreInfo
@onready var store_button: Button = %StoreButton
@onready var restore_button: Button = %RestoreButton
@onready var clear_log_button: Button = %ClearLogButton
@onready var log_output: RichTextLabel = %LogOutput

func _ready() -> void:
    log_output.clear()
    manager.adapter = SimulatedMonetizationAdapter.new()
    var monetization_config := load(CONFIG_PATH)
    if monetization_config is MonetizationConfig:
        manager.config = monetization_config
    else:
        _log("[color=#ff5555]Unable to load monetization profile.[/color]")
    _connect_signals()
    _populate_selectors()
    _update_buttons_state()

func _connect_signals() -> void:
    ad_button.pressed.connect(_on_ad_button_pressed)
    purchase_button.pressed.connect(_on_purchase_button_pressed)
    store_button.pressed.connect(_on_store_button_pressed)
    restore_button.pressed.connect(_on_restore_button_pressed)
    clear_log_button.pressed.connect(_on_clear_log_pressed)
    ad_selector.item_selected.connect(_update_ad_info)
    product_selector.item_selected.connect(_update_product_info)
    store_selector.item_selected.connect(_update_store_info)
    manager.ad_loaded.connect(_on_ad_loaded)
    manager.ad_shown.connect(_on_ad_shown)
    manager.ad_closed.connect(_on_ad_closed)
    manager.ad_reward_granted.connect(_on_ad_reward_granted)
    manager.ad_failed.connect(_on_ad_failed)
    manager.purchase_requested.connect(_on_purchase_requested)
    manager.purchase_succeeded.connect(_on_purchase_succeeded)
    manager.purchase_failed.connect(_on_purchase_failed)
    manager.store_item_inspected.connect(_on_store_item_inspected)
    manager.store_item_purchased.connect(_on_store_item_purchased)
    manager.store_item_failed.connect(_on_store_item_failed)
    manager.restore_started.connect(func(): _log("Restore started"))
    manager.restore_finished.connect(func(restored): _log("Restore finished: %s" % restored))
    manager.restore_failed.connect(func(reason): _log("[color=#ff5555]Restore failed:[/color] %s" % reason))
    manager.config_changed.connect(func(_cfg): _populate_selectors())

func _populate_selectors() -> void:
    ad_selector.clear()
    product_selector.clear()
    store_selector.clear()
    if manager.config:
        for placement in manager.config.ad_placements:
            if placement == null:
                continue
            var idx := ad_selector.get_item_count()
            ad_selector.add_item(placement.get_display_label())
            ad_selector.set_item_metadata(idx, placement.placement_id)
        for product in manager.config.in_app_products:
            if product == null:
                continue
            var p_idx := product_selector.get_item_count()
            product_selector.add_item(product.get_display_label())
            product_selector.set_item_metadata(p_idx, product.product_id)
        for item in manager.config.store_items:
            if item == null:
                continue
            var s_idx := store_selector.get_item_count()
            store_selector.add_item(item.get_display_label())
            store_selector.set_item_metadata(s_idx, item.item_id)
    if ad_selector.get_item_count() > 0:
        ad_selector.select(0)
    _update_ad_info()
    if product_selector.get_item_count() > 0:
        product_selector.select(0)
    _update_product_info()
    if store_selector.get_item_count() > 0:
        store_selector.select(0)
    _update_store_info()
    _update_buttons_state()

func _update_buttons_state() -> void:
    var has_config := manager.config != null
    ad_button.disabled = not has_config or ad_selector.get_item_count() == 0
    purchase_button.disabled = not has_config or product_selector.get_item_count() == 0
    store_button.disabled = not has_config or store_selector.get_item_count() == 0
    restore_button.disabled = not has_config

func _update_ad_info(_index := 0) -> void:
    var placement := _get_selected_ad_config()
    if placement:
        var reward_text := "Non-rewarded"
        if placement.is_rewarded():
            var reward_amount := placement.reward_amount
            if reward_amount <= 0:
                reward_amount = 1
            if placement.reward_currency.is_empty():
                reward_text = "Reward: %d" % reward_amount
            else:
                reward_text = "Reward: %d %s" % [reward_amount, placement.reward_currency]
        ad_info.text = "Placement ID: %s\nType: %s\nProvider: %s\nCooldown: %.1f min\n%s" % [
            placement.placement_id,
            placement.placement_type.capitalize(),
            placement.provider,
            placement.cooldown_minutes,
            reward_text
        ]
    else:
        ad_info.text = "Select an ad placement"

func _update_product_info(_index := 0) -> void:
    var product := _get_selected_product()
    if product:
        product_info.text = "Product ID: %s\nPrice: %.2f %s\nConsumable: %s\n%s" % [
            product.product_id,
            product.price,
            product.currency,
            product.consumable,
            product.description
        ]
    else:
        product_info.text = "Select a product"

func _update_store_info(_index := 0) -> void:
    var item := _get_selected_store_item()
    if item:
        store_info.text = "Item ID: %s\nCost: %d %s\n%s" % [
            item.item_id,
            item.price,
            item.currency,
            item.description
        ]
    else:
        store_info.text = "Select a store item"

func _on_ad_button_pressed() -> void:
    var placement := _get_selected_ad_config()
    if placement == null:
        _log("[color=#ff5555]No ad placement selected.[/color]")
        return
    _log("Requesting ad: %s" % placement.placement_id)
    manager.show_ad(placement.placement_id)

func _on_purchase_button_pressed() -> void:
    var product := _get_selected_product()
    if product == null:
        _log("[color=#ff5555]No product selected.[/color]")
        return
    _log("Purchasing product: %s" % product.product_id)
    manager.purchase_product(product.product_id)

func _on_store_button_pressed() -> void:
    var item := _get_selected_store_item()
    if item == null:
        _log("[color=#ff5555]No store item selected.[/color]")
        return
    _log("Opening store item: %s" % item.item_id)
    manager.open_store_item(item.item_id)

func _on_restore_button_pressed() -> void:
    _log("Starting restore flow")
    manager.restore_purchases()

func _on_clear_log_pressed() -> void:
    log_output.clear()

func _on_ad_loaded(placement: AdPlacementConfig) -> void:
    _log("Ad loaded: %s" % placement.placement_id)

func _on_ad_shown(placement: AdPlacementConfig) -> void:
    _log("Ad shown: %s" % placement.placement_id)

func _on_ad_closed(placement: AdPlacementConfig) -> void:
    _log("Ad closed: %s" % placement.placement_id)

func _on_ad_reward_granted(placement: AdPlacementConfig, amount: int) -> void:
    _log("Reward granted: %d %s" % [amount, placement.reward_currency])

func _on_ad_failed(_placement: AdPlacementConfig, reason: String) -> void:
    _log("[color=#ff5555]Ad failed:[/color] %s" % reason)

func _on_purchase_requested(product: InAppProductConfig, quantity: int) -> void:
    _log("Purchase requested: %s x%d" % [product.product_id, quantity])

func _on_purchase_succeeded(product: InAppProductConfig, receipt: Dictionary) -> void:
    _log("Purchase succeeded: %s" % product.product_id)
    _log("Receipt: %s" % receipt)

func _on_purchase_failed(product: InAppProductConfig, reason: String) -> void:
    var product_id := product == null ? "(unknown)" : product.product_id
    _log("[color=#ff5555]Purchase failed for %s:[/color] %s" % [product_id, reason])

func _on_store_item_inspected(item: StoreItemConfig) -> void:
    _log("Store item inspected: %s" % item.item_id)

func _on_store_item_purchased(item: StoreItemConfig) -> void:
    _log("Store item purchased: %s" % item.item_id)

func _on_store_item_failed(item: StoreItemConfig, reason: String) -> void:
    var item_id := item == null ? "(unknown)" : item.item_id
    _log("[color=#ff5555]Store item failed for %s:[/color] %s" % [item_id, reason])

func _get_selected_ad_config() -> AdPlacementConfig:
    if manager.config == null or ad_selector.get_item_count() == 0:
        return null
    var index := ad_selector.get_selected()
    if index < 0:
        return null
    var meta := ad_selector.get_item_metadata(index)
    return manager.config.get_ad_by_id(meta)

func _get_selected_product() -> InAppProductConfig:
    if manager.config == null or product_selector.get_item_count() == 0:
        return null
    var index := product_selector.get_selected()
    if index < 0:
        return null
    var meta := product_selector.get_item_metadata(index)
    return manager.config.get_product_by_id(meta)

func _get_selected_store_item() -> StoreItemConfig:
    if manager.config == null or store_selector.get_item_count() == 0:
        return null
    var index := store_selector.get_selected()
    if index < 0:
        return null
    var meta := store_selector.get_item_metadata(index)
    return manager.config.get_store_item_by_id(meta)

func _log(message: String) -> void:
    var timestamp := Time.get_time_string_from_system()
    log_output.append_text("[%s] %s\n" % [timestamp, message])
    log_output.scroll_vertical = log_output.get_line_count()
