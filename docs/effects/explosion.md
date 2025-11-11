# 爆炸（Explosion）

- **场景路径**：`res://explosion/explosion.tscn`
- **适用 Godot 版本**：4.3 及以上（Forward+ / Mobile）
- **分类**：[粒子系统](../categories/particles.md) · [火焰与爆炸](../categories/fire_and_heat.md) · [破坏与冲击](../categories/destruction_impact.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

多层粒子叠加的 2D 爆炸，包含核心火焰、放射火花、拖尾烟尘与火苗轨迹。通过 `AnimationPlayer` 统一触发/复位，适合瞬时爆发型技能或武器命中反馈。

## 节点结构

- `Explosion (Node2D)`: 根节点，包含全部子粒子。
- `SmokeParticles2D (GPUParticles2D)`: 使用 `ParticleProcessMaterial`（球形发射半径 40）制造向外扩散烟雾。
- `FireTrails (Node2D)`: 挂载 5 个实例化的 `fire_trails.tscn`，通过切向加速度形成旋转火舌。
- `FireBurstParticles2D (GPUParticles2D)`: 强力向外喷射的火焰核心，初速度 350。
- `SparkleParticles2D (GPUParticles2D)`: 少量亮色粒子，增强高光与噪点。
- `AnimationPlayer`: 包含 `Explode`、`RESET`、`DISABLE` 三个动画用于播放、复位与停用。

## 核心技术

- 粒子缩放与透明度均由 `CurveTexture` 与 `GradientTexture2D` 控制，实现先膨胀后收缩的火焰。
- `fire_trails.tscn` 内部使用极高的切向加速度与阻尼，使火舌在短时间内旋转并迅速熄灭。
- `AnimationPlayer` 的 `Explode` 动画在 0~0.5 秒间切换 `emitting` 状态，并在结束时统一关闭粒子，避免持续模拟。

## 关键参数

- `SmokeParticles2D.amount = 16`：控制烟雾密度。
- `FireBurstParticles2D.amount = 64`、`randomness = 1.0`：决定火焰丰度与散射。
- `SparkleParticles2D.amount = 32`：维持亮点数量，过高会产生噪点。
- `preprocess`（多个粒子设置为 0.6）：保证播放瞬间已有粒子生成，避免“弹出”现象。

## 性能与常见陷阱

- 同时播放多个爆炸时，建议降低 `FireTrails` 实例数量或调低 `amount_ratio`，以减少 GPU 压力。
- 若项目未开启 2D Glow，可适当调高 `modulate`，避免火焰显得黯淡。
- 移植到移动平台时，可将所有粒子的 `texture` 改为压缩纹理，或禁用拖尾（`trail_enabled`）。

## 复用流程

1. 将 `explosion` 文件夹复制到项目根目录，并在 Godot 中使用 `Scene` → `New Inherited Scene` 继承以便自定义。
2. 在需触发的脚本中实例化节点，调用 `AnimationPlayer.play("Explode")`。
3. 利用 `animation_finished` 信号在播放结束后回收节点或放回对象池。
4. 如需不同色彩，修改各 `ParticleProcessMaterial` 的 `color_ramp` 即可。

## 资源关联

- 纹理：`res://assets/puff.png`、`res://assets/sparkle.png` 等。
- 子场景：`res://explosion/fire_trails.tscn`。
- 动画：`AnimationPlayer` 中的 `Explode`、`RESET`、`DISABLE`。

## 预览

- TODO：添加爆炸效果 GIF（`docs/assets/explosion.gif`）。
