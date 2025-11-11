# 粒子系统类效果

本类别收录主要通过 `GPUParticles2D` / `GPUParticles3D` 构建的效果，常见优化手段包括曲线贴图控制缩放、颜色渐变以及发射速率调度。所有粒子均基于 GPU，适合大规模发射；若需移植到旧硬件，可根据下表建议降低 `amount`、`lifetime` 或 `trail` 相关开销。

| 效果 | 维度 | 关键节点 / Material | 性能提示 |
| --- | --- | --- | --- |
| [爆炸](../effects/explosion.md) | 2D | 多个 GPUParticles2D 分层、`AnimationPlayer` 统一触发 | 高峰粒子约 160+，可调 `amount` 与 `preprocess` 降压 |
| [火球](../effects/fire_ball.md) | 2D | 火尾、核心、火花四层粒子 | 缩短 `lifetime` 可减少残留；灯光可按需关闭 |
| [充能环](../effects/charging.md) | 2D | 单一 GPUParticles2D + 动画驱动 `speed_scale` | 预热需要 `preprocess`，移动端建议减小半径 |
| [激光束](../effects/laser_beam.md) | 2D | 发射、束体、碰撞三组粒子 | 束体发射盒长随脚本改变，应避免极端长度 |
| [闪电链](../effects/lightning_beam.md) | 2D | 折线末端火花粒子 | `flashes` 增大时注意 GPU 过载；粒子数量较小 |
| [鬼影拖尾](../effects/ghost_trail.md) | 2D | 残影粒子贴图 + Timer 复制 | 粒子数量低，主要开销来自 Sprite 渲染 |
| [星空背景](../effects/star_field.md) | 2D | 大范围粒子立方体发射 | `emission_box_extents` 影响批次，按相机尺寸调整 |
| [屏幕冲击波](../effects/shockwave.md) | 2D | 环状粒子 + 随机烟尘 | `amount` 极低，可放心使用 |
| [火焰喷发](../effects/fire_burst_3d.md) | 3D | Quad 粒子 + Shader 变形 | `amount`=300，Mobile 建议减半 |
| [火柱](../effects/flames_3d.md) | 3D | Quad 粒子 + 自定义 Shader | 持续发射，注意与其它碰撞体的排序 |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | 3D | 粒子负责火花、冲击波 | 配合 Mesh 一次性播放，`AnimationPlayer` 控速 |
| [地表破碎](../effects/ground_destruction_3d.md) | 3D | 三组碎块 + 三组粉尘粒子 | 每组 35~60 粒子，一次性爆发，可适当减少 `draw_pass` |
| [枪口焰](../effects/muzzle_flash_3d.md) | 3D | Sparks 粒子 + Billboard | `speed_scale`=20 造成高模拟频率，可降至 10 |
| [剑气斩击](../effects/slash_3d.md) | 3D | 刀身碎屑粒子 | 触发时间短，对性能影响小 |

## 调优建议

- 尽量使用 `visibility_rect`（2D）与 `visibility_aabb`（3D）裁剪粒子包围盒。
- 爆发型效果可减少 `amount`，同时提高 `explosiveness` 维持视觉密度。
- 对于 Mobile 平台，将纹理压缩为 ETC2/ASTC 并禁用多余的 `trail`。
