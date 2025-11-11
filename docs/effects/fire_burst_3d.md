# 火焰喷发（Fire Burst 3D）

- **场景路径**：`res://fire_burst_3d/fire_burst.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [火焰与爆炸](../categories/fire_and_heat.md) · [3D 效果](../categories/dimension_3d.md) · [自定义 Shader](../categories/shaders.md)

## 效果简介

以 3D 粒子结合 Shader 实现的火焰喷发效果，适合喷射器、爆炸初始火焰柱。单个 `GPUParticles3D` 即可完成体积火焰、扰动与淡出。

## 节点结构

- `FireBurst (GPUParticles3D)`：核心粒子节点，`amount = 300`，`lifetime = 2.0`，启用 `trail`。
- `material_override`：`ShaderMaterial` 引用 `fire_burst.gdshader`，并绑定火焰渐变、帧序列纹理。
- `process_material`：控制发射角度、重力、缩放曲线与颜色渐变。
- `draw_pass_1 = QuadMesh`：使用四边形面向摄像机渲染火焰贴图。

## 核心技术

- Shader 关键片段：
  ```glsl
  shader_type spatial;
  render_mode unshaded;
  uniform sampler2D fire_gradient;
  uniform sampler2D texture_pattern;
  uniform float warp_strength = 0.025;
  
  void vertex() {
      float particle_frame = floor(INSTANCE_CUSTOM.z * float(particles_h_frames * particles_v_frames));
      UV /= vec2(particles_h_frames, particles_v_frames);
      UV += vec2(mod(particle_frame, h_frames) / h_frames,
                 floor((particle_frame + 0.5) / h_frames) / v_frames);
  }
  
  void fragment() {
      float animation_progress = 1.0 - COLOR.a;
      vec2 distortion_offset = vec2(
          sin(animation_progress * warp_speed + UV.y * TAU / warp_scale),
          sin(animation_progress * warp_speed + UV.x * TAU / warp_scale)) * warp_strength;
      float pattern = texture(texture_pattern, UV + distortion_offset).r;
      pattern = clamp(pattern - animation_progress, 0.0, 1.0);
      vec4 gradient_color = texture(fire_gradient, vec2(pattern));
      ALBEDO = gradient_color.rgb;
      ALPHA = pattern;
  }
  ```
- 利用 `particles_h_frames`/`v_frames` 将 2×2 的粒子序列拆分，使每个粒子播放不同帧序列。
- `warp_*` 系列 uniform 通过噪声波动模拟火焰扭曲。

## 关键参数

- `ParticleProcessMaterial.initial_velocity = 1.5~2.0`：控制火焰喷发速度。
- `gravity = (0, 0.5, 0)`：轻微向上漂浮。
- `scale_curve`：决定粒子在生命周期内由小变大的节奏。
- Shader uniform：
  - `warp_strength`：扭曲幅度（默认 0.025）。
  - `warp_speed`：扭曲速度（默认 8.0）。
  - `warp_scale`：扭曲波长（默认 0.5）。

## 性能与常见陷阱

- `amount = 300` 对 GPU 有一定压力，若需要多个实例可降低粒子数量并关闭 `trail`。
- Shader 使用 `sin` 与多次纹理采样，移动平台可适当降低 `warp_speed` 并减小纹理尺寸。
- 使用 `blend_add` 渲染，需搭配暗背景或确保场景 Bloom 设置不过曝。

## 复用流程

1. 复制 `fire_burst_3d` 文件夹到项目，实例化 `FireBurst`。
2. 根据场景缩放调整火焰体积，可通过修改父节点缩放或 `process_material.scale_curve`。
3. 通过脚本设置 `emitting = true/false` 控制喷射；可与 `Timer` 配合实现间歇喷发。
4. 若需不同色调，替换 `fire_gradient` 纹理或修改 Shader 中的 `albedo`。

## 资源关联

- Shader：`res://fire_burst_3d/fire_burst.gdshader`
- 纹理：`fire_burst.png`（序列帧）、`fire_gradient.tres`

## 预览

- TODO：添加 `fire_burst_3d.gif` 预览。
