# 剑气斩击（Slash 3D）

- **场景路径**：`res://slash_3d/sword.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [运动轨迹与残影](../categories/motion_trails.md) · [3D 效果](../categories/dimension_3d.md) · [几何 / 网格](../categories/geometry_mesh.md) · [自定义 Shader](../categories/shaders.md)

## 效果简介

包含武器模型、斩击光刃、粒子火花与完整攻击动画的 3D 近战特效。`AnimationPlayer` 提供 Idle 与 Attack 两个状态，可直接嵌入角色骨骼或作为测试 Demo。

## 节点结构

- `Sword (Node3D)`：根节点，包含武器模型与动画。
- 模型节点：
  - `Guard`、`Blade`、`BladeTip`、`Handle`：各自使用 `StandardMaterial3D`，构成武器本体。
  - `Slash3D (MeshInstance3D)`：使用 Shader `slash_3d.gdshader` 的 Quad 网格，作为剑气可视化。
- `Blade/Particles (GPUParticles3D)`：在攻击期间喷射火花，材质使用点状贴图。
- `AnimationPlayer`：
  - `Idle` 循环轻微摇摆。
  - `Attack` 动画 2 秒，驱动 `Slash3D` 的透明度、粒子发射、武器轨迹。

## 核心技术

- Shader 片段：
  ```glsl
  shader_type spatial;
  render_mode blend_add, cull_disabled, unshaded;
  uniform vec4 albedo : source_color;
  uniform sampler2D texture_albedo : source_color;
  uniform float emission_energy;
  uniform float alpha_offset = 0.0;

  void fragment() {
      vec4 albedo_tex = texture(texture_albedo, UV);
      ALBEDO = albedo.rgb * albedo_tex.rgb * emission_energy;
      ALPHA = max(albedo_tex.a - alpha_offset, 0.0);
  }
  ```
- `AnimationPlayer.Attack` 动画关键帧：
  - 在 0~0.2 秒内将 `Slash3D:alpha_offset` 降至 0，迅速显现剑气。
  - 0.2 秒后将粒子 `Blade/Particles.emitting` 设为 false，让火花自然消散。
  - 轨迹 `Sword` 位置/旋转曲线描绘出挥砍动作。
- `Trail` 粒子使用 `GradientTexture2D` 从白到黑渐隐，模拟金属火花。

## 关键参数

- Shader uniform：
  - `emission_energy`（默认 4）：决定剑气亮度。
  - `alpha_offset`：由动画驱动，控制剑气溶解。
- 粒子：
  - `Blade/Particles.amount = 100`，`lifetime = 0.5`。
- 动画：
  - 攻击结束后通过 `next` 自动回到 `Idle`。

## 性能与常见陷阱

- `Slash3D` 使用宽大的 Quad，需确保 `blend_add` 与背景颜色相协调，避免过曝。
  如需避免背面可见，可开启 `cull_back`。
- 粒子发射箱较小，如与角色绑定时出现错位，可调整 `Particles` 的 `Transform3D`。
- 若替换武器模型，请保持 `Slash3D` UV 与贴图匹配，或重新烘焙贴图。

## 复用流程

1. 在角色武器骨骼下实例化 `Sword` 场景，并根据角色比例调整缩放。
2. 触发攻击时调用 `$AnimationPlayer.play("Attack")`，动画结束自动回到 `Idle`。
3. 若与角色动画混合，可将 `Sword` 作为武器子节点，并在角色动画中同步播放或触发。
4. 替换 Shader 贴图 `slash_pattern.png` 可打造不同颜色与纹理的剑气。

## 资源关联

- Shader：`res://slash_3d/slash_3d.gdshader`
- 纹理：`slash_pattern.png`、`assets/dot.png`
- 动画：`Attack`、`Idle`、`RESET`

## 预览

- TODO：添加 `slash_3d.gif` 预览。
