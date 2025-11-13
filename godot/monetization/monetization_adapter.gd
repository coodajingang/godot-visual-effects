extends Resource
class_name MonetizationAdapter

var manager: MonetizationManager
var config: MonetizationConfig

func configure(monetization_manager: MonetizationManager, monetization_config: MonetizationConfig) -> void:
	manager = monetization_manager
	config = monetization_config

func show_ad(_placement: AdPlacementConfig) -> void:
	manager.notify_ad_failed(_placement, "Adapter not implemented.")

func purchase_product(_product: InAppProductConfig, _quantity := 1) -> void:
	manager.notify_purchase_failed(_product, "Adapter not implemented.")

func open_store_item(_item: StoreItemConfig) -> void:
	manager.notify_store_item_failed(_item, "Adapter not implemented.")

func restore_purchases() -> void:
	manager.notify_restore_failed("Adapter not implemented.")
