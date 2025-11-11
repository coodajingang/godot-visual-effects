# 枪口焰（Muzzle Flash 3D）

- **场景路径**：`res://muzzle_flash_3d/muzzle_flash.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [能量束与投射物](../categories/energy_and_projectile.md) · [破坏与冲击](../categories/destruction_impact.md) · [3D 效果](../categories/dimension_3d.md) · [自定义 Shader](../categories/shaders.md) · [几何 / 网格](../categories/geometry_mesh.md)

## 效果简介

面向 FPS/TPS 武器的枪口火光，结合 Mesh 缩放、粒子火花和 Fresnel Shader。`WorldEnvironment` 可选用于离线演示，实际项目可移除或替换。

## 节点结构

- `Sparks (GPUParticles3D)`：火花粒子，`process_material` 使用 `color_ramp` 控制颜色渐隐，`speed_scale = 20` 实现高频闪烁。
- `muzzle_mesh1~5 (MeshInstance3D)`：多份枪口网格使用 `muzzle_flash_material.tres`，在 Shader 中随机旋转，增加体积感。
- `muzzle_core (MeshInstance3D)`：中心 Billboard Quad，半透明贴图增强亮度。
- `WorldEnvironment`：示例环境，可在项目中移除。

## 核心技术

- Shader `muzzle_flash.gdshader` 关键逻辑：
  ```glsl
  render_mode cull_disabled, unshaded, blend_add;
  uniform float rate_of_fire = 10.0;
  uniform float size_randomization = 0.3;
  uniform sampler2D scale_curve;
  
  void vertex() {
      vec3 origin = (MODEL_MATRIX * vec4(0, 0, 0, 1)).xyz;
      float unique_seed = origin.x + origin.y + origin.z;
      float random_value = random(floor(TIME * rate_of_fire) + unique_seed);
      VERTEX = rotate(VERTEX, vec3(0.0, 1.0, 0.0), random_value * TAU);
      VERTEX *= 1.0 + (random_value * 2.0 - 1.0) * size_randomization;
      float curve = texture(scale_curve, vec2(fract(TIME * rate_of_fire))).r;
      VERTEX *= curve;
  }
  
  void fragment() {
      float muzzle_tex = texture(muzzle_pattern, UV).r;
      vec4 muzzle_color = texture(colorize_gradient, vec2(muzzle_tex));
      float fresnel = smoothstep(0.0, fresnel_threshold, dot(NORMAL, VIEW));
      ALBEDO = muzzle_color.rgb;
      ALPHA = muzzle_tex * fresnel;
  }
  ```
- 利用 `TIME * rate_of_fire` 生成伪随机数，保证每个网格在快速触发时也拥有不同旋转与缩放。
- 粒子火花基于 `spark_mesh.obj`，可用于模拟金属火星。

## 关键参数

- Shader uniform：
  - `rate_of_fire`：决定缩放曲线循环频率（默认 10）。
  - `size_randomization`：缩放扰动幅度（默认 0.3）。
  - `fresnel_threshold`：控制正面对准摄像机时的透明度。
- 粒子：
  - `Sparks.amount` 与 `initial_velocity` 控制火花数量与飞出速度。

## 性能与常见陷阱

- 多个网格共享同一材质，为避免全局随机相同，材质设置为场景资源（`local_to_scene = true`）。
- 粒子速度高，若存在穿模可减小 `speed_scale` 或限制发射锥角。
- Shader 使用 `TIME`，在暂停或慢速回放时需注意时间线同步。

## 复用流程

1. 导入 `muzzle_flash_3d` 目录，在武器节点下实例化 `MuzzleFlash`。
2. 将根节点朝向枪口方向，必要时调整 `Transform3D`。
3. 在开火时设置 `visible = true` 并短暂启用，可结合 `AnimationPlayer` 或 `Timer` 自动隐藏。
4. 若需匹配武器节奏，修改材质中的 `rate_of_fire` 或替换 `scale_curve` 纹理。
5. 使用对象池管理火花粒子可减少开销。

## 资源关联

- Shader：`muzzle_flash.gdshader`
- 材质：`muzzle_flash_material.tres`、`sparks_material.tres`
- 网格：`muzzle_flash_mesh.obj`、`spark_mesh.obj`
- 纹理：`muzzle_flash.png`、`new_gradient_texture__1d.tres`、`spark.png`

## 预览

- TODO：添加 `muzzle_flash_3d.gif` 预览。
