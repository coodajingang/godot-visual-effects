# 2D 效果总览

本页汇总所有 2D 场景中的视觉效果，适合 UI、横版或俯视角游戏使用。多数实现依赖 `Node2D`、`Line2D` 与 `GPUParticles2D`，并通过 `AnimationPlayer` 统一驱动时间轴。

| 效果 | 场景路径 | 关键节点 | 特点 |
| --- | --- | --- | --- |
| [爆炸](../effects/explosion.md) | `res://explosion/explosion.tscn` | 多组 GPUParticles2D + `AnimationPlayer` | 组合式爆炸，含火花与烟雾 |
| [火球](../effects/fire_ball.md) | `res://fire_ball/fire_ball.tscn` | 粒子 + `PointLight2D` | 带残留光晕，可作为投射物核心 |
| [充能环](../effects/charging.md) | `res://charging/charging_particles.tscn` | 单节点粒子 | 2 秒周期循环放缩 |
| [激光束](../effects/laser_beam.md) | `res://laser_beam/laser_beam_2d.tscn` | RayCast2D + Line2D + 粒子 | 支持射线碰撞检测 |
| [闪电链](../effects/lightning_beam.md) | `res://lightning_beam/lightning_beam.tscn` | RayCast2D + 折线 Shader | 可最多跳跃 3 个目标 |
| [鬼影拖尾](../effects/ghost_trail.md) | `res://ghost_trail/ghost_trail.tscn` | 粒子残影 | 支持 Sprite 残影淡出 |
| [可编程拖尾](../effects/trail_2d.md) | `res://trail/trail_2d.tscn` | Line2D + 脚本缓存轨迹 | 任意 Node2D 可附加 |
| [星空背景](../effects/star_field.md) | `res://star_field/star_field.tscn` | 1080p 覆盖粒子场 | 适合背景循环 |
| [屏幕冲击波](../effects/shockwave.md) | `res://shockwave/shockwave.tscn` | 环形粒子 + Shake 摄像机 | 与 2D 关卡共用摄像机 |

## 集成建议

- 所有效果均为独立目录，可直接复制进入项目根目录，通过 `Scene` 加载。
- 若需批量播放爆发型效果，建议使用对象池复用节点，避免频繁实例化。
