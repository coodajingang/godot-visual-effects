# 可编程拖尾（Trail2D）

- **场景路径**：`res://trail/trail_2d.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[运动轨迹与残影](../categories/motion_trails.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

基于 `Line2D` 的通用拖尾组件，通过脚本记录移动路径并按时间衰减点位。支持在编辑器中直接调节分辨率、寿命、最大点数以及目标节点，适用于角色、弹幕或 UI 光效的尾迹。

## 节点结构

- `Trail2D (Line2D)`：内置脚本 `trail_2d.gd`，继承 `Line2D` 并提供导出属性。
- 默认宽度由 `width_curve` 与 `gradient` 决定，可在 Inspector 中替换。

## 核心技术

- 通过 `_process` 每帧检查目标节点位置，若与上一个点距离大于 `resolution` 即新增点。
- 使用数组 `_points_creation_time` 记录每个点的生成时间，`remove_older()` 在 `lifetime` 到期后依次删除，实现拖尾逐段收缩。
- `set_as_top_level(true)` 使拖尾节点独立于父节点 transform，避免旋转缩放导致的形变。
- 若 `target_path` 未指定，则默认跟随父节点。

## 关键参数

- `is_emitting`：是否采样新点；关闭时仅保留/衰减已有点。
- `resolution`（默认 5 像素）：相邻点之间的最小距离。
- `lifetime`（默认 0.5 秒）：点存在时间。
- `max_points`（默认 100）：上限点数，防止无限增长。
- `width_curve`、`gradient`：控制线条宽度与颜色渐变。

## 性能与常见陷阱

- `max_points` 过高会增加 CPU 开销（管理点列表），需结合对象速度调整。
- `set_emitting(true)` 会清空历史点，如果需要平滑衔接应先调用 `set_emitting(false)` 停止，再稍后重新启动。
- 拖尾在世界空间铺展，如需 UI 坐标应确保父节点在 `CanvasLayer` 中。

## 复用流程

1. 将 `trail_2d.tscn` 作为任何 `Node2D` 子节点，或在脚本中 `new()` 一个 `Trail2D`。
2. 设置 `target_path` 指向要跟随的节点；若为空，默认跟随父节点。
3. 在移动逻辑中调用 `set_emitting(true/false)` 控制启停，例如加速时开启。
4. 自定义颜色时可直接改写 `gradient` 资源或在运行时调用 `gradient.set_color(...)`。

## 资源关联

- 脚本：`trail/trail_2d.gd`（注册为 `Trail2D` 类）。
- 贴图：默认无贴图，仅使用渐变颜色。

## 预览

- TODO：添加 `trail_2d.gif` 预览。
