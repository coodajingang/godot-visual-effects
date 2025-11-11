# 充能环（Charging Particles）

- **场景路径**：`res://charging/charging_particles.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [能量束与投射物](../categories/energy_and_projectile.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

围绕角色或物体的径向充能特效，粒子从外向内收缩构成能量聚拢的视觉。`AnimationPlayer` 循环调节粒子速度与调色，可直接用于技能读条或拾取道具的提示圈。

## 节点结构

- `ChargingParticles (GPUParticles2D)`：唯一节点，执行粒子发射。
- `AnimationPlayer`：包含 `Charge` 动画，周期 2 秒。

## 核心技术

- 粒子材质设置：
  - `emission_shape = SPHERE`，`emission_sphere_radius = 80`，实现环状分布。
  - `radial_velocity = -80` 让粒子朝中心移动。
  - `scale_curve` 控制粒子在运动中先放大后缩回。
- `AnimationPlayer` 动画关键帧：
  - `speed_scale` 在 0.5~4.0 之间摆动，形成吸气加速的节奏感。
  - `self_modulate` 在 (1.1,1.1,1.1) 与 (1.5,1.5,1.5) 之间过渡，增强亮度脉动。

## 关键参数

- `amount = 16`：决定同时存在的粒子数量。
- `lifetime`（默认 1 秒）：影响半径与速度的实际感受，可适当增加以形成更长尾迹。
- `speed_scale`：由动画驱动，可根据项目节奏调整关键帧时间。

## 性能与常见陷阱

- 单节点粒子负载低，可安全叠加多个实例。
- 由于 radial velocity 为负值，若将 `emission_sphere_radius` 设为 0 会导致粒子重叠闪烁。
- 若需要在 Gamepad HUD 中使用，可将节点父级设为 `CanvasLayer`，避免摄像机移动影响位置。

## 复用流程

1. 拷贝 `charging` 目录到项目中，实例化 `ChargingParticles`。
2. 通过脚本控制 `AnimationPlayer.play("Charge")` 开始充能，可在技能完成时调用 `stop()`。
3. 如需手动控制节奏，可关闭 `AnimationPlayer`，直接调整 `speed_scale` 和 `self_modulate`。
4. 修改 `color` 与纹理可转换为电能、冰霜等主题。

## 资源关联

- 纹理：`res://assets/glowing_circle.png`。
- 动画：`Charge`。

## 预览

- TODO：添加 `charging.gif` 预览。
