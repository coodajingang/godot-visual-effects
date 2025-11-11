# 地表破碎（Ground Destruction 3D）

- **场景路径**：`res://ground_destruction_3d/ground_destruction_effect.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [破坏与冲击](../categories/destruction_impact.md) · [3D 效果](../categories/dimension_3d.md) · [自定义 Shader](../categories/shaders.md) · [几何 / 网格](../categories/geometry_mesh.md)

## 效果简介

大型地面破裂与碎块喷射特效，结合 Shader 控制地面抬升、裂纹扩散，配合三组碎块粒子与粉尘粒子。`AnimationPlayer` 统一时间线，适用于 Boss 技能或重型武器击中地面时的演出。

## 节点结构

- `RocksEffectMesh (MeshInstance3D)`：使用 Shader `rocks_vfx.gdshader`，驱动地表块向上抬升并震动。
- `CracksEffectMesh (MeshInstance3D)`：使用 `cracks.gdshader` 的视差贴图在地面投射裂纹。
- `Chunks`、`Chunks2`、`Chunks3`（GPUParticles3D）：一次性喷射岩石碎块，材质 `ground_destruction_chunk_ppm.tres`。
- `DustParticles`、`DustParticles2`、`DustParticles3`：灰尘粒子，采用自定义 ShaderMaterial 溶解粉尘。
- `AnimationPlayer`：`Attack` 动画长度 3.6 秒，驱动所有元素。

## 核心技术

- `rocks_vfx.gdshader` 使用曲线贴图控制各块抬升时序：
  ```glsl
  uniform sampler2D timing_curve;
  uniform sampler2D scale_curve_rock;
  uniform sampler2D shake_curve;
  uniform float animation_driver;
  
  // 顶点位移根据顶点颜色、随机数与动画驱动混合，生成破碎感
  ```
- `cracks.gdshader` 通过视差位移与 `fade` 参数控制裂纹从中心向外扩散。
- `AnimationPlayer` 中的关键帧：
  - 0.55 秒开始激活 `Chunks`，随后 0.8、1.3 秒激活第二、第三批碎块。
  - 粉尘粒子在 0.2/0.45/0.95 秒陆续发射。
  - `RocksEffectMesh.animation_driver` 从 -1.038 过渡到 0.85，控制抬升与最终停留高度。
  - `CracksEffectMesh.offset/fade` 调节裂纹平铺与透明度。
  - 动画结束调用 `queue_free()`，便于一次性播放。

## 关键参数

- Shader uniform：
  - `bb_start` / `bb_size`：定义抬升区域包围盒。
  - `appear_percentage`：控制同时抬升的碎块比例。
  - `randomness`：扰动幅度。
- 粒子：
  - `Chunks.amount = 35`（三组），`one_shot = true`，`explosiveness = 0.9`。
  - `DustParticles.amount = 60`，`lifetime = 2.5`。
- 动画：`Attack.length = 3.6`，在 3.6 秒时自动 `queue_free()`。

## 性能与常见陷阱

- Shader 使用多张曲线纹理，需要保证导入为 `Filter = Linear`、`Repeat = Clamp`，否则插值不正确。
- 粒子可见范围较大（`visibility_aabb` 明确指定），若场景较小可减小该值以减少不必要的绘制。
- 若与地形交互，需要确保地面材质/法线与效果 Mesh 颜色匹配，否则颜色差异明显。

## 复用流程

1. 导入 `ground_destruction_3d` 目录，实例化 `GroundDestructionEffect`。
2. 通过脚本播放 `$AnimationPlayer.play("Attack")`，播放结束即自动销毁。
3. 若要重复播放，可禁用动画末尾的 `queue_free`（移除动画中的 `method` 轨道），改为回收对象。
4. 调整破坏区域时：
   - 修改 `ShaderMaterial` 中的 `bb_start` 与 `bb_size`。
   - 根据场景缩放 `RocksEffectMesh` 与裂纹 `PlaneMesh`。
5. 替换碎块贴图可快速变化材质风格。

## 资源关联

- Shader：`rocks_vfx.gdshader`、`cracks.gdshader`
- 纹理：`rock_chunks_alb.png`、`rock_chunks_nor.png`、`dust-alb.png`、`dust-erode.png`、`cracks.png`
- 贴图曲线：`CurveTexture_*` 资源定义抬升/抖动/缩放曲线。

## 预览

- TODO：添加 `ground_destruction_3d.gif` 预览。
