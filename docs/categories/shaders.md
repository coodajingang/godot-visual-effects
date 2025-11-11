# 自定义 Shader 类效果

以下效果以 Shader 为核心驱动，利用顶点位移、纹理扰动或 Fresnel 等技巧营造复杂的光效与几何变化。复用时请留意材质资源的 uniform 配置，以及是否依赖粒子系统的 `INSTANCE_CUSTOM` 数据。

| 效果 | Shader 入口 | 核心能力 | 注意事项 |
| --- | --- | --- | --- |
| [火焰喷发](../effects/fire_burst_3d.md) | `fire_burst_3d/fire_burst.gdshader` | 粒子帧序列 + UV 扭曲 + 噪声侵蚀 | 依赖 `INSTANCE_CUSTOM.z` 计算序列帧；warp 参数影响性能 |
| [火柱](../effects/flames_3d.md) | 场景内嵌 Shader | billboard 粒子面朝摄像机 + 多层噪声 | 顶点阶段使用 `INV_VIEW_MATRIX` 朝向摄像机，保持 Quad 朝向 |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | `stylized_explosion_3d/explosion.gdshader` 等 | 多曲线驱动网格拉伸、侵蚀、发光 | 需同步 `AnimationPlayer` 的驱动参数；材质共享时注意 uniform 覆盖 |
| [地表破碎](../effects/ground_destruction_3d.md) | `rocks_vfx.gdshader` & `cracks.gdshader` | Shader 控制碎石抬升、裂纹视差 | `animation_driver` 低于 0 代表初始状态；裂纹材质使用视差贴图需要深度缓冲 |
| [枪口焰](../effects/muzzle_flash_3d.md) | `muzzle_flash_3d/muzzle_flash.gdshader` | 法线 Fresnel、随机旋转与缩放 | 使用 `TIME` 计算伪随机数，多个实例共用材质需启用 `local_to_scene` |
| [剑气斩击](../effects/slash_3d.md) | `slash_3d/slash_3d.gdshader` | 发光贴图 + Alpha 剪切 | `alpha_offset` 控制消隐；搭配 `AnimationPlayer` 动画播放 |
| [藤蔓蔓延](../effects/vines_3d.md) | `vines_3d/vines.gdshader` | 顶点沿法线收缩实现生长 | `growth`、`thickness` 为关键；需要网格顶点颜色提供生长顺序 |

## 统一调试流程

1. 在 Godot 编辑器中选中对应 Mesh 或粒子，确认材质是否被多个实例共享；若需实例化参数，启用 `local_to_scene`。
2. 使用 `Remote Scene` 面板实时调整 uniform，观察动画驱动是否同步（特别是 `AnimationPlayer` 推动的 `shader_parameter/*`）。
3. 若需移植至 GLES3/Mobile，避免使用 `textureLod`、`VIEW` 等在兼容模式下受限的函数，可通过条件编译或宏替换。
