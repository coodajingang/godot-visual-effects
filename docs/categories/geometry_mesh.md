# 几何 / 网格驱动效果

此类别强调通过 `MeshInstance3D`、粒子绘制网格或自定义模型来塑造视觉几何体，而不仅仅依赖 2D 贴图。适合需要立体感、复杂表面或与场景交互的效果。

| 效果 | 主要网格资源 | 网格用途 | 复用建议 |
| --- | --- | --- | --- |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | `explosion_mesh.mesh`、`SphereMesh`、`QuadMesh` | 分别承担爆炸主体、冲击波与地面光斑 | 保持 Mesh 层级和材质绑定一致，否则动画驱动参数将失效 |
| [地表破碎](../effects/ground_destruction_3d.md) | `rocks-vfx.mesh`、`PlaneMesh` | Shader 扭曲地面、裂纹视差平面 | 需要配套 `AnimationPlayer` 推动 `animation_driver`，并保持 Mesh 尺寸与贴图匹配 |
| [枪口焰](../effects/muzzle_flash_3d.md) | `muzzle_flash_mesh.obj`、`spark_mesh.obj` | 枪口主体使用 Mesh 缩放，粒子发射网格碎片 | Mesh 模型朝向需与武器坐标一致，可通过 `Transform3D` 微调 |
| [剑气斩击](../effects/slash_3d.md) | `QuadMesh`、`PrismMesh`、`BoxMesh` | 武器模型 + Slash 面片 | 若替换武器模型，确保 Slash Mesh UV 区间符合 Shader 贴图 |
| [藤蔓蔓延](../effects/vines_3d.md) | `vines_mesh.mesh` | 顶点颜色决定生长时间 | 导出自建模型时需写入顶点颜色通道（红色）表示生长顺序 |

## 与场景几何的协同

- 在 3D 场景中需要与地形对齐的效果（如裂纹、藤蔓），可通过父节点的 `Transform3D` 缩放与旋转进行适配。
- 若场景存在阴影投射，请根据材质设置开启/关闭 `cast_shadow`，以避免错误的影子形状。
