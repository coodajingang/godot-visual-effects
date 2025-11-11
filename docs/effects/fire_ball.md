# 火球（Fire Ball）

- **场景路径**：`res://fire_ball/fire_ball.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [火焰与爆炸](../categories/fire_and_heat.md) · [能量束与投射物](../categories/energy_and_projectile.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

以魔法火球为主题的复合粒子效果，包含火尾、核心火焰、火花与辉光，并带有灯光脉冲。提供 `Ignition` 与 `Fade` 两个动画以控制出现与熄灭，可独立用于投射物或作为其它效果的子节点。

## 节点结构

- `FireBallEffect (Node2D)`：根节点。
- `FireTrail (GPUParticles2D)`：细长向后喷射的火尾，初速度沿 `Vector3(-1,0,0)`。
- `TrailSparkles (GPUParticles2D)`：在尾部散布亮点，增加能量感。
- `FireCore (GPUParticles2D)`：球状核心火焰，`direction = (0,-1,0)` 营造向上翻腾。
- `CoreSparkles (GPUParticles2D)`：核心火花，高频闪烁。
- `PointLight2D`：随动画缩放半径与能量。
- `Glow (Sprite2D)`：使用 `white_glowing_circle.png` 提供叠加辉光。
- `AnimationPlayer`：`Ignition`（点燃）与 `Fade`（熄灭）动画，默认自动播放 `Ignition`。

## 核心技术

- 通过三套颜色渐变与缩放曲线控制火焰从明亮转为暗红。
- `Ignition` 动画：
  - 将 `PointLight2D` 从关闭状态扩散到 6.0 倍纹理尺寸。
  - 同步启动所有粒子发射，并调整根节点 `modulate` 实现淡入。
- `Fade` 动画：
  - 停止粒子发射，让现有粒子自然消散。
  - 渐缩 `Glow` 与灯光能量，0.5 秒内完成熄灭。

## 关键参数

- `FireTrail.amount = 64`，`lifetime = 0.5`：尾迹密度与长度，降低可减压。
- `FireCore.process_material.initial_velocity_max = 100`：控制核心的翻腾高度。
- `PointLight2D.energy = 0.3`：基础亮度，可配合项目光照调节。
- `TrailSparkles.randomness = 1.0`：增加动态噪声感。

## 性能与常见陷阱

- 粒子纹理较大（512×512），移动端建议转换为压缩格式。
- `Glow` Sprite 占据较大区域，如存在排序问题，可将其移至独立 `CanvasLayer`。
- 若多次实例化，应避免重复播放 `Ignition` 未完成即 `Fade`，可在脚本中等待 `animation_finished`。

## 复用流程

1. 将 `fire_ball` 目录加入项目，实例化 `FireBallEffect` 节点。
2. 根据需要触发 `AnimationPlayer.play("Ignition")` 或 `play("Fade")`。
3. 要作为完整投射物可参考 `fire_ball_spell.tscn`：
   - `RigidBody2D` 脚本在碰撞后实例化 `fire_ball_explosion.tscn` 播放爆炸。
   - 可替换 `explosion_scene` 以自定义命中效果。
4. 若需要不同颜色的元素球，修改四组粒子的 `GradientTexture2D` 与 `Glow`/`PointLight2D` 的颜色即可。

## 资源关联

- 纹理：`puff_smooth.png`、`puff.png`、`sparkle.png`、`white_glowing_circle.png`。
- 动画：`Ignition`、`Fade`。
- 参考子场景：`fire_ball_spell.tscn`、`fire_ball_explosion.tscn`。

## 预览

- TODO：添加 `fire_ball.gif` 预览。
