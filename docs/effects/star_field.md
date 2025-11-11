# 星空背景（Star Field）

- **场景路径**：`res://star_field/star_field.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [自然与场景氛围](../categories/environment_nature.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

用于营造动态星空的背景粒子系统，覆盖 1920×1080 视口。粒子随机旋转并以曲线控制大小，保持缓慢闪烁的视觉层次感。

## 节点结构

- `StarField (GPUParticles2D)`：唯一节点，位置位于屏幕中心 `(960, 540)`，方便对齐全屏。
- 粒子材质（`ParticleProcessMaterial`）：
  - `emission_shape = BOX`，`emission_box_extents = (960, 540, 0)`，完整覆盖视口。
  - `angular_velocity` 在 -445.5 ~ 50 之间，制造缓慢旋转。
  - `scale_curve` 利用 `CurveTexture` 赋予不同大小的星点。

## 关键参数

- `lifetime = 6.0`、`preprocess = 6.0`：即刻填满屏幕并保持缓慢更新。
- `texture = star.svg`：可替换为自定义星点贴图。
- `amount` 默认值（未在场景中显式设置，Godot 默认 32），可根据需求提高以增加密度。

## 性能与常见陷阱

- 若希望持续滚动星空，可在父节点中对 `StarField` 缓慢平移或旋转，或调节 `initial_velocity`。
- `visibility_rect` 设为 `Rect2(-960, -540, 1920, 1080)`，确保粒子在屏幕边缘不会被裁剪，必要时按实际分辨率修改。
- 在 UI 层使用时，可将节点放入 `CanvasLayer` 并关闭输入处理。

## 复用流程

1. 将 `star_field` 目录复制到项目，实例化 `StarField`。
2. 根据画面比例调整 `emission_box_extents` 与 `position`，可通过脚本读取 `get_viewport_rect()` 自动适配。
3. 若需层次深度，可创建多个 `StarField` 层，分别设定不同 `lifetime` 与 `scale`，并搭配视差移动。

## 资源关联

- 纹理：`res://star_field/star.svg`、`star_field_background.tscn` 可作为额外背景层。

## 预览

- TODO：添加 `star_field.gif` 预览。
