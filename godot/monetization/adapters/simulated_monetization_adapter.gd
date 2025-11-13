extends MonetizationAdapter
class_name SimulatedMonetizationAdapter

@export_range(0.0, 5.0, 0.1) var ad_load_delay := 0.6
@export_range(0.0, 10.0, 0.1) var ad_display_duration := 2.0
@export_range(0.0, 1.0, 0.05) var ad_failure_rate := 0.0
@export_range(0.0, 5.0, 0.1) var purchase_delay := 1.0
@export_range(0.0, 1.0, 0.05) var purchase_failure_rate := 0.0
@export_range(0.0, 5.0, 0.1) var store_delay := 0.5
@export_range(0.0, 1.0, 0.05) var restore_failure_rate := 0.0
@export_range(0.0, 5.0, 1.0) var reward_multiplier := 1

var _rng := RandomNumberGenerator.new()

func configure(monetization_manager: MonetizationManager, monetization_config: MonetizationConfig) -> void:
	super.configure(monetization_manager, monetization_config)
	_rng.randomize()

func show_ad(placement: AdPlacementConfig) -> void:
	if manager == null:
		return
	await manager.await_time(ad_load_delay)
	manager.notify_ad_loaded(placement)
	if _rng.randf() < ad_failure_rate:
		manager.notify_ad_failed(placement, "Ad failed to load.")
		return
	manager.notify_ad_shown(placement)
	await manager.await_time(ad_display_duration)
	if _rng.randf() < ad_failure_rate:
		manager.notify_ad_failed(placement, "The player closed the ad early.")
		manager.notify_ad_closed(placement)
		return
	if placement.is_rewarded():
		var amount := placement.reward_amount
		if amount <= 0:
			amount = 1
		amount = int(round(amount * reward_multiplier))
		manager.notify_ad_reward_granted(placement, max(amount, 1))
	manager.notify_ad_closed(placement)

func purchase_product(product: InAppProductConfig, quantity := 1) -> void:
	if manager == null:
		return
	await manager.await_time(purchase_delay)
	if _rng.randf() < purchase_failure_rate:
		manager.notify_purchase_failed(product, "Transaction failed.")
		return
	var receipt := {
		"product_id": product.product_id,
		"quantity": quantity,
		"timestamp": Time.get_unix_time_from_system()
	}
	manager.notify_purchase_succeeded(product, receipt)

func open_store_item(item: StoreItemConfig) -> void:
	if manager == null:
		return
	await manager.await_time(store_delay)
	manager.notify_store_item_purchased(item)

func restore_purchases() -> void:
	if manager == null:
		return
	await manager.await_time(purchase_delay)
	if _rng.randf() < restore_failure_rate:
		manager.notify_restore_failed("Restore failed.")
		return
	var restored := []
	if config:
		for product in config.in_app_products:
			if not product.consumable:
				restored.append(product.product_id)
	manager.notify_restore_finished(restored)
