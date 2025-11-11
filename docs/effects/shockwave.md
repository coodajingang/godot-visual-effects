# 屏幕冲击波（Shockwave）

- **场景路径**：`res://shockwave/shockwave.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [屏幕空间 / 后处理](../categories/screen_space.md) · [破坏与冲击](../categories/destruction_impact.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

以透明白环展示的屏幕冲击波，适合作为爆炸或重击后的画面反馈。项目还提供 `shockwave_world.tscn`，整合星空背景、陨石碰撞与摄像机震动，展示完整应用场景。

## 节点结构

- `Shockwave (GPUParticles2D)`：根节点，`amount = 3`，通过粒子循环产生连续波纹。
- 粒子材质：
  - `scale_min/max = 7.0`，配合 `CurveTexture` 快速扩散。
  - `color_ramp` 让透明度在初期保持亮度，随后渐隐。
- 扩展场景 `shockwave_world.tscn`：
  - `StarField` 背景。
  - `Asteroid` 带火尾粒子与动画：`FireTrail`、`TrailSparkles`。
  - `Explosion` 子场景。
  - `Camera` 继承 `shaking_camera.tscn`，脚本 `player_camera.gd` 控制噪声震动。

## 核心技术

- 粒子 `preprocess` 默认为 0，以保证每次实例化时从缩放最小状态开始。
- 在扩展示例中，`AnimationPlayer` 调整 `Camera.shake_amount`，与冲击波同时发生，增强冲击感。
- 可结合 `CanvasLayer` 叠加效果，实现 HUD 闪烁或屏幕扭曲。

## 关键参数

- `lifetime = 2.0`：波纹持续时间。
- `scale_curve`：控制波纹从小到大的节奏，可修改 `CurveTexture` 调整扩散速度。
- `amount`：同时存在的冲击波数量，默认 3，可改为 1 以减少重复。

## 性能与常见陷阱

- 粒子纹理为透明 PNG，大小较小，对性能影响可忽略。
- 在使用 `shockwave_world.tscn` 时，记得禁用或替换示例中的 `Camera` 脚本，以避免与项目自身摄像机冲突。
- 冲击波层级需位于最前（可通过 `z_index` 调节），否则可能被场景对象遮挡。

## 复用流程

1. 直接实例化 `shockwave.tscn`，在脚本中触发 `emitting = true` 并在完成后设为 `false`。
2. 需要一次性爆发时，可配合 `Timer` 控制开启 0.2 秒后关闭。
3. 若要根据事件强度调整波纹大小，可在触发前修改 `scale_min/max` 或 `process_material.scale_curve`。
4. 摄像机震动可引用 `shaking_camera.tscn`，在播放冲击波时设置 `shake_amount`。

## 资源关联

- 纹理：`res://assets/white_ring.png`。
- 脚本：`player_camera.gd`（可选）。
- Demo：`shockwave_world.tscn`、`shaking_camera.tscn`。

## 预览

- TODO：添加 `shockwave.gif` 预览。
