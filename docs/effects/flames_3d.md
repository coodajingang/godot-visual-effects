# 火柱（Flames 3D）

- **场景路径**：`res://flames_3d/flames_3d.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [火焰与爆炸](../categories/fire_and_heat.md) · [3D 效果](../categories/dimension_3d.md) · [自定义 Shader](../categories/shaders.md)

## 效果简介

持续喷射的火柱效果，由单个 `GPUParticles3D` 结合自定义 Shader 构成。Shader 在顶点阶段使用 `INV_VIEW_MATRIX` 保持 Quad 面向摄像机，同时在片元阶段叠加多重噪声，模拟火焰翻腾感。

## 节点结构

- `Flames3D (GPUParticles3D)`：粒子数量 50，`lifetime = 1.5`，`draw_order = BACK_TO_FRONT`。
- `material_override`：嵌入式 ShaderMaterial，引用形状、纹理与噪声贴图。
- `process_material`：
  - `emission_shape = BOX`，`emission_box_extents = (3, 0.1, 0.1)`，使火焰从窄线状底部喷出。
  - `direction = (0, 1, 0)`，粒子向上喷射。
  - `scale_curve` 维持柱状体积。

## 核心技术

- Shader 片段节选：
  ```glsl
  shader_type spatial;
  render_mode blend_add, depth_draw_opaque, unshaded;
  
  void vertex() {
      mat4 billboard = mat4(
          normalize(INV_VIEW_MATRIX[0])*length(MODEL_MATRIX[0]),
          normalize(INV_VIEW_MATRIX[1])*length(MODEL_MATRIX[0]),
          normalize(INV_VIEW_MATRIX[2])*length(MODEL_MATRIX[2]),
          MODEL_MATRIX[3]);
      billboard = billboard * mat4( ... ); // 依据 INSTANCE_CUSTOM.x 旋转
      MODELVIEW_MATRIX = VIEW_MATRIX * billboard;
      particle_time = INSTANCE_CUSTOM.z;
  }
  
  void fragment() {
      vec4 fire_shape = texture(texture_fire_shape, UV);
      vec4 fire_pattern = texture(texture_fire_pattern, UV * vec2(0.5, 0.25) + fire_pattern_uv_offset);
      vec4 fire_grain1 = texture(texture_fire_grain, UV * vec2(0.5) + fire_grain1_uv_offset);
      vec4 fire_grain2 = texture(texture_fire_grain, UV * vec2(0.25) + fire_grain2_uv_offset);
      vec4 albedo_tex = fire_shape * fire_pattern * 2.0 * fire_grain1 * 2.0 * fire_grain2 * 2.0;
      ALBEDO = albedo.rgb * albedo_tex.rgb;
      ALPHA = albedo.a * albedo_tex.a;
  }
  ```
- 通过多层噪声偏移（`fire_pattern_uv_offset`、`fire_grain*_uv_offset`）模拟火焰内部流动。
- 粒子颜色渐变 `GradientTexture2D` 从黑到亮白再到透明，表现火焰中心到边缘的温度变化。

## 关键参数

- `amount = 50`：火焰密度，移动平台可降至 30。
- `initial_velocity = 0.5`：火焰上升速度，增大可形成喷火枪效果。
- Shader uniform：
  - `texture_fire_shape`、`texture_fire_pattern`、`texture_fire_grain`：可替换以获得不同纹理细节。
  - `albedo`：整体色温。

## 性能与常见陷阱

- 多次纹理采样会增加片元开销，尽量使用压缩纹理或降低纹理分辨率。
- Shader 使用加法混合，多个火柱叠加可能导致过曝，可在环境中开启 Bloom 调整阈值。
- Quad 面片保持面向摄像机，若需要锁定朝向可移除 `billboard` 变换。

## 复用流程

1. 导入 `flames_3d` 目录，实例化 `Flames3D`。
2. 调整 `emission_box_extents` 控制火柱直径，修改 `scale_curve` 决定高度。
3. 若需循环开关，可通过脚本设置 `emitting` 并配合 `Timer`。
4. 想打造多色火焰，可修改 Shader uniform `albedo` 或替换 `GradientTexture2D`。

## 资源关联

- 纹理：`fire_shape.png`、`fire_pattern.png`、`fire_grain.png`。
- Shader：场景内嵌 `ShaderMaterial`。

## 预览

- TODO：添加 `flames_3d.gif` 预览。
