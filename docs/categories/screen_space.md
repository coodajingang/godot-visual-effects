# 屏幕空间 / 后处理风格效果

仓库中虽未包含真正的全屏后处理 Shader，但以下效果通过大型半透明平面、摄像机震动或覆盖式粒子，营造出屏幕空间冲击感，可作为制作屏幕特效的参考。

| 效果 | 技术手段 | 应用场景 | 延伸思路 |
| --- | --- | --- | --- |
| [屏幕冲击波](../effects/shockwave.md) | 大尺度环形粒子叠加 + `Camera2D` 噪声震动 | UI 闪屏、爆炸冲击反馈 | 可改为 `CanvasLayer` 承载，实现 HUD 叠加 |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | `SphereMesh` Shockwave + 地面 Decal | 3D 场景爆炸冲击 | 将 Shockwave Mesh 替换为屏幕对齐 Quad，即可做全屏扭曲 |
| [激光束](../effects/laser_beam.md) | Line2D + 粒子覆盖摄像机前半屏 | HUD 激光瞄准、高亮提示 | Line2D 可改为屏幕 UV 采样 Shader，实现扭曲/溶解 |

## 与真正的后处理结合

若需要全屏扭曲或 Bloom，可以：

1. 将效果放入单独 `Viewport`，再映射至屏幕 `TextureRect`；
2. 在 Viewport 上应用 Godot 4 的屏幕后处理材质，并在 Shader 中叠加本仓库粒子；
3. 使用 `WorldEnvironment` 的 Glow/Bloom 与上述效果组合，增强亮度对比。
