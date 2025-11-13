extends Resource
class_name MonetizationConfig

@export var profile_name := "Default"
@export var ad_placements: Array[AdPlacementConfig] = []
@export var in_app_products: Array[InAppProductConfig] = []
@export var store_items: Array[StoreItemConfig] = []

var _ads_by_id := {}
var _products_by_id := {}
var _items_by_id := {}

func rebuild_indexes() -> void:
	_ads_by_id.clear()
	_products_by_id.clear()
	_items_by_id.clear()
	for placement in ad_placements:
		if placement == null or placement.placement_id.is_empty():
			continue
		_ads_by_id[placement.placement_id] = placement
	for product in in_app_products:
		if product == null or product.product_id.is_empty():
			continue
		_products_by_id[product.product_id] = product
	for item in store_items:
		if item == null or item.item_id.is_empty():
			continue
		_items_by_id[item.item_id] = item

func get_ad_by_id(placement_id: String) -> AdPlacementConfig:
	return _ads_by_id.get(placement_id)

func get_product_by_id(product_id: String) -> InAppProductConfig:
	return _products_by_id.get(product_id)

func get_store_item_by_id(item_id: String) -> StoreItemConfig:
	return _items_by_id.get(item_id)

func has_placements() -> bool:
	return not ad_placements.is_empty()

func has_products() -> bool:
	return not in_app_products.is_empty()

func has_store_items() -> bool:
	return not store_items.is_empty()
