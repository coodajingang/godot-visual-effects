# 3D 效果总览

这些效果针对 3D 场景设计，涵盖粒子喷发、武器特效、地表破坏与植物生长。大多通过 `AnimationPlayer` 控制，同时结合 Shader 产生体积感。

| 效果 | 场景路径 | 关键节点 | 特点 |
| --- | --- | --- | --- |
| [火焰喷发](../effects/fire_burst_3d.md) | `res://fire_burst_3d/fire_burst.tscn` | GPUParticles3D + ShaderMaterial | 火焰帧序列 + UV 扭曲 |
| [火柱](../effects/flames_3d.md) | `res://flames_3d/flames_3d.tscn` | 粒子 + 自定义 Shader | 持续喷射火焰，Quad 面向摄像机 |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | `res://stylized_explosion_3d/stylized_explosion.tscn` | MeshInstance3D 多层材质 | 一次性爆炸，含冲击波与地面 Decal |
| [地表破碎](../effects/ground_destruction_3d.md) | `res://ground_destruction_3d/ground_destruction_effect.tscn` | MeshInstance3D + 多组粒子 | 剧烈破碎、粉尘与裂纹组合 |
| [枪口焰](../effects/muzzle_flash_3d.md) | `res://muzzle_flash_3d/muzzle_flash.tscn` | Mesh + Billboard Shader + 粒子 | 适合 FPS 武器枪口光效 |
| [剑气斩击](../effects/slash_3d.md) | `res://slash_3d/sword.tscn` | 武器模型 + Slash Shader | 包含攻动画面与粒子火花 |
| [藤蔓蔓延](../effects/vines_3d.md) | `res://vines_3d/vines.tscn` | MeshInstance3D + 顶点位移 Shader | 动态生长覆盖地表 |

## 集成建议

- 3D 效果通常需要配套的 `default_env.tres` 或光照设置，导入后检查项目环境是否一致。
- 若场景尺寸不同，可通过父节点缩放统一整体比例，但注意 Shader 中的偏移参数可能需要同步调整。
