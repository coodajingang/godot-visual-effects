# 闪电链（Lightning Beam）

- **场景路径**：`res://lightning_beam/lightning_beam.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [能量束与投射物](../categories/energy_and_projectile.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

可在多个目标之间连锁跳跃的闪电束。主节点通过 `RayCast2D` 确定首个命中目标，再利用 `Area2D` 捕获附近可继续跳跃的对象，按顺序生成 `lightning_jolt.tscn` 折线特效。

## 节点结构

- `LightningBeam (RayCast2D)`: 根节点，脚本 `lightning_beam.gd` 挂载于此。
- `JumpArea (Area2D)`: 圆形触发器，`CollisionShape2D` 半径 192，用于搜寻下一跳目标。
- `lightning_jolt.tscn`: 子场景，包含 `Line2D`、`GPUParticles2D` 火花与 `AnimationPlayer`。

## 核心技术

- 主脚本 `shoot()` 流程：
  ```gdscript
  var _primary_body = get_collider()
  var _secondary_bodies = jump_area.get_overlapping_bodies()
  if _primary_body:
      _secondary_bodies.erase(_primary_body)
      _target_point = _primary_body.global_position
  for flash in range(flashes):
      var jolt = lightning_jolt.instantiate()
      add_child(jolt)
      jolt.create(_start, target_point)
      # 后续对次级目标重复同样逻辑
      await get_tree().create_timer(flash_time).timeout
  ```
- `lightning_jolt.gd` 的 `create()` 方法将折线分段，加入随机旋转制造自然抖动，并在终点触发 `Sparks` 粒子：
  ```gdscript
  func create(start: Vector2, end: Vector2) -> void:
      ray_cast.global_position = start
      ray_cast.target_position = end - start
      ray_cast.force_raycast_update()
      if ray_cast.is_colliding():
          end = ray_cast.get_collision_point()
      # 随机分段生成折线
  ```
- `lightning_jolt.tscn` 通过 `AnimationPlayer` 的 `destroy` 动画在 0.5 秒后渐隐并 `queue_free()`。

## 关键参数

- `flashes`：同一目标连续闪烁次数（默认 3）。
- `flash_time`：每次闪烁间隔（默认 0.1 秒）。
- `bounces_max`：最大跳跃次数（默认 3）。
- `JumpArea` 半径：决定可连锁的范围，可调整 `CollisionShape2D.shape.radius`。
- `lightning_jolt` 中 `segments`、`spread_angle` 控制折线细节。

## 性能与常见陷阱

- 每次 `shoot()` 都会实例化多个 `lightning_jolt`，频繁触发时建议改用对象池。
- 连锁目标过多时折线可能穿插遮挡，可根据需求调整 `Line2D.width` 或开启 Z 排序。
- 若在 TileMap 场景中，需确保 `JumpArea` 的碰撞层与可跳目标一致。

## 复用流程

1. 将 `lightning_beam` 目录复制到项目中，在需要的位置实例化 `LightningBeam`。
2. 设置其 `global_position` 与 `rotation` 指向施法方向，并确保 `RayCast2D` 的 `target_position` 为射程向量。
3. 在脚本中调用 `shoot()`，可根据命中对象施加伤害。
4. 若要更换外观，只需修改 `lightning_jolt` 的 `width_curve`、`color` 与粒子材质。

## 资源关联

- 脚本：`lightning_beam.gd`、`lightning_jolt.gd`。
- 子场景：`lightning_jolt.tscn`。
- 粒子纹理：位于 `res://assets/`（火花贴图）。

## 预览

- TODO：添加 `lightning_beam.gif` 预览。
