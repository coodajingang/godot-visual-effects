# 能量束与投射物类效果

聚焦于高科技或魔法能量相关的视觉表现，特征为强烈的方向性、快速移动和辉光。

| 效果 | 技术亮点 | 用途 | 自定义参数 |
| --- | --- | --- | --- |
| [火球](../effects/fire_ball.md) | 多层粒子 + 2D 光源点亮路径 | 魔法弹、火焰箭矢 | `Ignition`/`Fade` 动画控制出入场 |
| [充能环](../effects/charging.md) | 径向粒子倒吸至中心 | 技能读条、Buff 提示 | `AnimationPlayer` 调整速度范围 |
| [激光束](../effects/laser_beam.md) | RayCast2D 更新碰撞点，Line2D Tween 宽度 | 科幻武器、采集射线 | `cast_speed`、`max_length` 控制反应速度 |
| [闪电链](../effects/lightning_beam.md) | 多次 `lightning_jolt` 实例化 + 区域跳跃 | 暴风法术、连锁闪电 | `flashes`、`bounces_max` 决定连锁次数 |
| [枪口焰](../effects/muzzle_flash_3d.md) | 随机旋转的 Mesh + Fresnel 抑制正对视角 | FPS 枪口火、能量炮口光 | `rate_of_fire`、`size_randomization` 匹配开火频率 |

## 设计要点

- 方向性效果可叠加 `Bloom` 与镜头光晕增强亮度对比。
- 若需要命中反馈，可配合 [屏幕冲击波](../effects/shockwave.md) 或火花粒子作为终点特效。
