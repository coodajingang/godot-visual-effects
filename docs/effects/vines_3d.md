# 藤蔓蔓延（Vines 3D）

- **场景路径**：`res://vines_3d/vines.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[自然与场景氛围](../categories/environment_nature.md) · [3D 效果](../categories/dimension_3d.md) · [自定义 Shader](../categories/shaders.md) · [几何 / 网格](../categories/geometry_mesh.md)

## 效果简介

通过 Mesh 顶点位移模拟藤蔓逐渐覆盖的效果。Shader 根据顶点颜色与 `growth` uniform 控制每段藤蔓出现时间，并支持 `thickness` 调节厚度。适合环境叙事、关卡机关或自然侵蚀演出。

## 节点结构

- `Vines (MeshInstance3D)`：使用 `vines_mesh.mesh` 与 Shader `vines.gdshader`。
- 材质 `ShaderMaterial_m64vv`：提供 `uv_scale`、`growth`、`thickness`、`sss_strength`、纹理等参数。

## 核心技术

- Shader 片段：
  ```glsl
  shader_type spatial;
  uniform float growth = 1.0;
  uniform float thickness = 0.25;
  
  void vertex() {
      float uneven_growth = (growth * 2.0 - 1.0) - COLOR.r;
      displacement = UV.x + min(0.0, uneven_growth);
      VERTEX += NORMAL * 0.1;
      VERTEX -= NORMAL * thickness * displacement;
  }
  
  void fragment() {
      if (displacement < 0.0) {
          discard;
      }
      vec2 offset_uv = UV + vec2(displacement, 0.0) * uv_scale;
      ALBEDO = texture(albedo_texture, offset_uv).rgb;
      NORMAL_MAP = texture(normal_texture, offset_uv).rgb;
      ROUGHNESS = roughness;
      SSS_STRENGTH = sss_strength;
  }
  ```
- 顶点颜色（红色通道）表示藤蔓生长先后顺序；`growth` 从 0~1 插值时，不同区段逐步显示。
- `thickness` 控制沿法线收缩量，可用于模拟藤蔓枯萎或变粗的过程。

## 关键参数

- `growth`（默认 0.999999）：决定当前生长进度，0=完全未生长，1=完全覆盖。
- `thickness`（默认 0.26）：藤蔓厚度。
- `uv_scale`：纹理重复次数，默认 `(4, 1)`。
- `roughness`：影响材质高光程度。

## 性能与常见陷阱

- Shader 使用 `discard`，对延迟渲染影响较小，但请确保 `growth` 渐变平滑，避免大量像素突然消失引发闪烁。
- 若模型缺乏顶点颜色，生长序列将失效；导出新模型时需在 DCC 中烘焙红色通道。
- 过高的 `uv_scale` 会重复纹理，注意与模型 UV 对齐。

## 复用流程

1. 导入 `vines_3d` 目录，实例化 `Vines`，根据场景摆放位置与旋转。
2. 在脚本或动画中插值 `growth`，例如通过 `Tween`：
   ```gdscript
   var mat := $Vines.material_override
   mat.set("shader_parameter/growth", 0.0)
   create_tween().tween_property(mat, "shader_parameter/growth", 1.0, 2.0)
   ```
3. 调整 `thickness` 可模拟植物枯萎、退场。
4. 若需多株藤蔓，可复制 Mesh 并改变 `uv_scale` 或 `growth` 起始值，避免完全同步。

## 资源关联

- Shader：`res://vines_3d/vines.gdshader`
- 纹理：`vines_albedo.png`、`vines_normal.png`
- Mesh：`vines_mesh.mesh`

## 预览

- TODO：添加 `vines_3d.gif` 预览。
