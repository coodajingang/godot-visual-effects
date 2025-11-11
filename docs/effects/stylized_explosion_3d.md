# 风格化爆炸（Stylized Explosion 3D）

- **场景路径**：`res://stylized_explosion_3d/stylized_explosion.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [火焰与爆炸](../categories/fire_and_heat.md) · [破坏与冲击](../categories/destruction_impact.md) · [3D 效果](../categories/dimension_3d.md) · [自定义 Shader](../categories/shaders.md) · [几何 / 网格](../categories/geometry_mesh.md)

## 效果简介

大型 3D 爆炸示例，结合 MeshInstance3D、多种 Shader 与粒子，构成热浪、冲击波、地面灼烧与全局光亮。`AnimationPlayer` 统一驱动所有 Shader uniform，为一次性播放的爆炸演出提供完整流程。

## 节点结构

- `Explosion (MeshInstance3D)`：爆炸主体，使用 `explosion_mesh.mesh` 与 Shader `explosion.gdshader`。
- `Shockwave (MeshInstance3D)`：球形冲击波，Shader `shockwave.gdshader` 实现侵蚀式透明度。
- `Blast (MeshInstance3D)`：平面冲击层，Shader `blast.gdshader` 控制阈值缩放。
- `Ground Decal (MeshInstance3D)`：地面光斑，`StandardMaterial3D` Alpha 逐渐透明。
- `OmniLight3D`：瞬时照亮周围环境。
- `AnimationPlayer`：`Explosion` 动画 1.5 秒，驱动各节点参数。

## 核心技术

- 主 Shader `explosion.gdshader`：
  ```glsl
  uniform sampler2D movement_curve;
  uniform sampler2D fire_curve;
  uniform sampler2D erosion_curve;
  uniform float movement_driver;
  
  void vertex() {
      float disp_noise = texture(pattern_texture, UV + vec2(0.0, uv_offset)).r;
      movement = movement_driver * (1.0 + offset) - (1.0 - COLOR.r) * offset;
      VERTEX *= texture(movement_curve, vec2(movement)).r;
      VERTEX += NORMAL * disp_noise * displacement;
  }
  
  void fragment() {
      float erosion_noise = texture(erosion_texture, UV * 2.0 + vec2(COLOR.g, uv_offset + COLOR.g)).r;
      float fire_value = 1.0 - texture(fire_curve, vec2(movement)).r;
      float erosion_value = texture(erosion_curve, vec2(dissapation)).r;
      float fire_mix = smoothstep(fire_value - fire_smooth, fire_value + fire_smooth, fire_noise);
      ALBEDO = mix(smoke_color.rgb, emission_color.rgb, fire_mix);
      ALPHA = smoothstep(erosion_value - erosion_smooth, erosion_value + erosion_smooth, erosion_noise);
  }
  ```
- `AnimationPlayer` 关键帧：
  - 增加 Shader `movement_driver`，推动 Mesh 膨胀。
  - 调整 `Shockwave.scale` 与 `shader_parameter/erosion` 实现冲击波扩散与透明。
  - 修改 `Blast.shader_parameter/threshold` 与 `Ground Decal.scale`。
  - 调节 `OmniLight3D.omni_range` 在 0.25 秒内从 0 拉至 20，再降回 0。
- 所有 Shader uniform 通过 `AnimationPlayer` 统一管理，确保时间同步。

## 关键参数

- `AnimationPlayer.Explosion.length = 1.5`：整体播放时长。
- Shader uniform：
  - `emission_color`、`smoke_color`：火焰与烟尘主色。
  - `displacement`：爆炸表面凸起强度。
  - `shockwave.shader_parameter/erosion_width`：冲击波边缘厚度。
- 粒子（来自 Explosion 子节点）：使用 2D 爆炸粒子作为火花补充，可继续调节其 `speed_scale`。

## 性能与常见陷阱

- 多个 Mesh 同时使用半透明材质，需注意渲染顺序；默认层级已按体积从内至外安排。
- Shader 多次采样噪声纹理，若在移动端使用，可降低纹理分辨率或减少动画分辨率。
- `AnimationPlayer` 自动播放 `Explosion`；若需要循环 Demo，可在结束后调用 `play("RESET")` 然后再次触发。

## 复用流程

1. 将 `stylized_explosion_3d` 文件夹复制进入项目，实例化 `StylizedExplosion`。
2. 通过脚本触发：`$StylizedExplosion/AnimationPlayer.play("Explosion")`。
3. 若需与对象池结合，可在动画结束时调用 `queue_free()` 或 `play("RESET")`。
4. 调整不同颜色主题时，修改 `ShaderMaterial` 中的 `emission_color`、`smoke_color` 与 `Ground Decal` 颜色。
5. 若场景比例不同，按比例缩放根节点，并同步调整光源范围。

## 资源关联

- Shader：`explosion.gdshader`、`blast.gdshader`、`shockwave.gdshader`
- Mesh：`explosion_mesh.mesh`、`SphereMesh`、`QuadMesh`、`PlaneMesh`
- 纹理：`explosion-pattern.png`、`smooth_noise.png`、`blast.png`、`ground_decal.png`

## 预览

- TODO：添加 `stylized_explosion_3d.gif` 预览。
