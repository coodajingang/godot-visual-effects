extends Node
class_name MonetizationManager

signal config_changed(config)

signal ad_requested(placement)
signal ad_loaded(placement)
signal ad_shown(placement)
signal ad_closed(placement)
signal ad_reward_granted(placement, amount)
signal ad_failed(placement, reason)

signal purchase_requested(product, quantity)
signal purchase_succeeded(product, receipt)
signal purchase_failed(product, reason)

signal store_item_inspected(item)
signal store_item_purchased(item)
signal store_item_failed(item, reason)

signal restore_started
signal restore_finished(restored_products)
signal restore_failed(reason)

const SimulatedMonetizationAdapter = preload("res://monetization/adapters/simulated_monetization_adapter.gd")

@export var config: MonetizationConfig: set = set_config
@export var adapter: MonetizationAdapter: set = set_adapter
@export var auto_rebuild_indexes := true

var _ad_cooldowns := {}

func _ready() -> void:
    if config and auto_rebuild_indexes:
        config.rebuild_indexes()
    if adapter == null:
        adapter = SimulatedMonetizationAdapter.new()
    if adapter:
        adapter.configure(self, config)

func set_config(value: MonetizationConfig) -> void:
    config = value
    if not is_inside_tree():
        await ready
    if config and auto_rebuild_indexes:
        config.rebuild_indexes()
    if adapter:
        adapter.configure(self, config)
    config_changed.emit(config)

func set_adapter(value: MonetizationAdapter) -> void:
    adapter = value
    if not is_inside_tree():
        await ready
    if adapter:
        adapter.configure(self, config)

func show_ad(placement_id: String) -> void:
    if config == null:
        notify_ad_failed(null, "No monetization config assigned.")
        return
    var placement := config.get_ad_by_id(placement_id)
    if placement == null:
        notify_ad_failed(null, "Unknown ad placement: %s" % placement_id)
        return
    if not _can_show_ad(placement):
        notify_ad_failed(placement, "Placement cooldown active.")
        return
    ad_requested.emit(placement)
    _ensure_adapter()
    adapter.show_ad(placement)

func purchase_product(product_id: String, quantity := 1) -> void:
    if config == null:
        notify_purchase_failed(null, "No monetization config assigned.")
        return
    var product := config.get_product_by_id(product_id)
    if product == null:
        notify_purchase_failed(null, "Unknown product: %s" % product_id)
        return
    purchase_requested.emit(product, quantity)
    _ensure_adapter()
    adapter.purchase_product(product, quantity)

func open_store_item(item_id: String) -> void:
    if config == null:
        notify_store_item_failed(null, "No monetization config assigned.")
        return
    var item := config.get_store_item_by_id(item_id)
    if item == null:
        notify_store_item_failed(null, "Unknown store item: %s" % item_id)
        return
    store_item_inspected.emit(item)
    _ensure_adapter()
    adapter.open_store_item(item)

func restore_purchases() -> void:
    if config == null:
        notify_restore_failed("No monetization config assigned.")
        return
    restore_started.emit()
    _ensure_adapter()
    adapter.restore_purchases()

func notify_ad_loaded(placement: AdPlacementConfig) -> void:
    ad_loaded.emit(placement)

func notify_ad_shown(placement: AdPlacementConfig) -> void:
    _ad_cooldowns[placement.placement_id] = Time.get_ticks_msec()
    ad_shown.emit(placement)

func notify_ad_closed(placement: AdPlacementConfig) -> void:
    ad_closed.emit(placement)

func notify_ad_reward_granted(placement: AdPlacementConfig, amount: int) -> void:
    ad_reward_granted.emit(placement, amount)

func notify_ad_failed(placement: AdPlacementConfig, reason: String) -> void:
    ad_failed.emit(placement, reason)

func notify_purchase_succeeded(product: InAppProductConfig, receipt: Dictionary) -> void:
    purchase_succeeded.emit(product, receipt)

func notify_purchase_failed(product: InAppProductConfig, reason: String) -> void:
    purchase_failed.emit(product, reason)

func notify_store_item_purchased(item: StoreItemConfig) -> void:
    store_item_purchased.emit(item)

func notify_store_item_failed(item: StoreItemConfig, reason: String) -> void:
    store_item_failed.emit(item, reason)

func notify_restore_finished(restored_products: Array) -> void:
    restore_finished.emit(restored_products)

func notify_restore_failed(reason: String) -> void:
    restore_failed.emit(reason)

func await_time(duration: float) -> void:
    if duration <= 0.0:
        return
    await get_tree().create_timer(duration).timeout

func _ensure_adapter() -> void:
    if adapter == null:
        adapter = SimulatedMonetizationAdapter.new()
    if adapter.manager != self or adapter.config != config:
        adapter.configure(self, config)

func _can_show_ad(placement: AdPlacementConfig) -> bool:
    if placement.cooldown_minutes <= 0.0:
        return true
    var last_shown := _ad_cooldowns.get(placement.placement_id, -1)
    if last_shown == -1:
        return true
    var now_ms := Time.get_ticks_msec()
    var cooldown_ms := int(placement.cooldown_minutes * 60.0 * 1000.0)
    return now_ms - last_shown >= cooldown_ms
