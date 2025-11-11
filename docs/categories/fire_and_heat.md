# 火焰与爆炸类效果

覆盖火焰、爆炸、火球等高温视觉主题，主要依赖暖色渐变、快速膨胀与残余烟雾来表现温度与冲击感。

| 效果 | 温度表现技巧 | 适用场景 | 延伸调节 |
| --- | --- | --- | --- |
| [爆炸](../effects/explosion.md) | 曲线控制火焰缩放、烟雾渐隐 | 2D 爆炸、火箭弹撞击 | 调整 `color_ramp` 可转换为毒爆、冰爆 |
| [火球](../effects/fire_ball.md) | 粒子叠加 + 光源亮度脉冲 | 魔法射弹、BOSS 技能 | 修改 `PointLight2D` 颜色实现冰球/毒球 |
| [火焰喷发](../effects/fire_burst_3d.md) | Shader 动态侵蚀 + 粒子尾迹 | 3D 火山、喷射器 | `warp_strength` 控制火焰扭动强度 |
| [火柱](../effects/flames_3d.md) | 三层噪声纹理叠乘 | 篝火、机关陷阱 | 缩小 `emission_box_extents` 可做火把 |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | Mesh 位移驱动火焰体积 + Shockwave | 3D Boss 爆炸、导弹爆破 | 调整 `emission_color` 可换成等离子爆炸 |

## 色彩与亮度建议

- 使用橙、红、黄的高饱和度渐变，并在边缘叠加白色，制造炙热中心。
- 结合 Godot Glow（`WorldEnvironment`）或 2D `CanvasItem` Bloom，可显著提升亮度冲击感。
