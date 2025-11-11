# Godot 可视化效果文档索引

## 项目概览

- **仓库**：`godot-visual-effects`
- **渲染管线**：Godot 4.3（兼容 4.2 及以上的 Forward+ / Mobile）
- **效果总览**：当前包含 2D 与 3D 共 16 组示例，涵盖粒子、网格、Shader、脚本驱动束流等主流 VFX 技法。
- **文档结构**：按分类（技术、维度、主题）和单独的效果页面组织，可快速定位实现细节。

> 提示：所有效果均使用 GPU 粒子或着色器实现，默认适配 Forward+。在 Mobile 渲染模式下请留意纹理尺寸与粒子数量的调优建议。

## 如何使用本文档

1. 先根据需求在下方 **分类导航** 中选择合适的类别，了解同类型效果的共性与差异。
2. 点击效果名称进入对应的 **效果详情页**，获取节点结构、关键参数、Shader/脚本片段以及复用流程。
3. 若需截图或录制预览，请参考 [`docs/assets/README.md`](assets/README.md) 中的捕捉指引。

## 分类导航

- 技术向：
  - [粒子系统](categories/particles.md)
  - [自定义 Shader](categories/shaders.md)
  - [几何/网格驱动效果](categories/geometry_mesh.md)
  - [屏幕空间 / 后处理](categories/screen_space.md)
- 维度：
  - [2D 效果](categories/dimension_2d.md)
  - [3D 效果](categories/dimension_3d.md)
- 主题：
  - [火焰与爆炸](categories/fire_and_heat.md)
  - [能量束与投射物](categories/energy_and_projectile.md)
  - [运动轨迹与残影](categories/motion_trails.md)
  - [自然与场景氛围](categories/environment_nature.md)
  - [破坏与冲击](categories/destruction_impact.md)

## 效果一览

| 效果 | 维度 | 技术要点 | 主题标签 |
| --- | --- | --- | --- |
| [爆炸（Explosion）](effects/explosion.md) | 2D | 多层 GPUParticles2D、动画驱动发射 | 火焰与爆炸 / 粒子 |
| [火球（Fire Ball）](effects/fire_ball.md) | 2D | 粒子叠加 + 2D 灯光 | 火焰与爆炸 / 能量束 |
| [充能环（Charging Particles）](effects/charging.md) | 2D | 放射状粒子缩放动画 | 能量束 |
| [激光束（Laser Beam 2D）](effects/laser_beam.md) | 2D | RayCast2D + 粒子 + Tween 宽度动画 | 能量束 |
| [闪电链（Lightning Beam）](effects/lightning_beam.md) | 2D | RayCast2D + 程序化折线 + 跳跃 Area2D | 能量束 / 粒子 |
| [鬼影拖尾（Ghost Trail）](effects/ghost_trail.md) | 2D | GPUParticles2D + Sprite 复制残影 | 运动轨迹 |
| [可编程拖尾（Trail2D）](effects/trail_2d.md) | 2D | Line2D 轨迹缓存脚本 | 运动轨迹 |
| [星空背景（Star Field）](effects/star_field.md) | 2D | 大范围粒子场 | 自然氛围 |
| [屏幕冲击波（Shockwave）](effects/shockwave.md) | 2D | 透明环粒子 + 摄像机震动 | 破坏冲击 / 屏幕空间 |
| [火焰喷发（Fire Burst 3D）](effects/fire_burst_3d.md) | 3D | GPUParticles3D + 扭曲 Shader | 火焰与爆炸 |
| [火柱（Flames 3D）](effects/flames_3d.md) | 3D | 带 UV 扭曲的粒子 Quad | 火焰与爆炸 |
| [风格化爆炸（Stylized Explosion 3D）](effects/stylized_explosion_3d.md) | 3D | MeshInstance3D 多 Shader 分层 | 火焰与爆炸 / 破坏 |
| [地表破碎（Ground Destruction 3D）](effects/ground_destruction_3d.md) | 3D | Shader 几何抬升 + 碎块粒子 | 破坏冲击 |
| [枪口焰（Muzzle Flash 3D）](effects/muzzle_flash_3d.md) | 3D | Billboard Shader + 随机旋转 | 能量束 |
| [剑气斩击（Slash 3D）](effects/slash_3d.md) | 3D | Mesh Shader Alpha 裁剪 + 粒子 | 运动轨迹 |
| [藤蔓蔓延（Vines 3D）](effects/vines_3d.md) | 3D | 顶点位移 Shader 控制生长 | 自然氛围 |

## 后续计划 / TODO

- 捕捉每个效果的 GIF 并保存至 `docs/assets/`，在效果页面中内嵌展示。
- 完善未列出的辅助场景（如 Demo Selector）的交互说明。
