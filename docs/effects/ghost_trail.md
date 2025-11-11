# 鬼影拖尾（Ghost Trail）

- **场景路径**：`res://ghost_trail/ghost_trail.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [运动轨迹与残影](../categories/motion_trails.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

通过粒子与 Sprite 复制实现的角色残影效果，适合动作游戏中的快速移动或闪避表现。默认纹理为俯视角色，可在脚本中替换为自定义角色贴图。

## 节点结构

- `GhostTrail (GPUParticles2D)`：根节点，负责在轨迹后方投射半透明残影。
- 粒子材质 `ParticleProcessMaterial_kcy0o`：
  - 限制角度 `angle_min/max = -53.2°`，与角色朝向保持一致。
  - `color_ramp` 让残影从半透明到消失。
- 可选子场景：
  - `animated_ghost_trail.tscn`：`Sprite2D` 附带脚本和定时器，自动生成 `fading_sprite.tscn`。

## 核心技术

- `fading_sprite.gd` 负责残影淡出：
  ```gdscript
  func fade(duration := lifetime) -> void:
      var transparent := self_modulate
      transparent.a = 0.0
      var tween = create_tween()
      tween.tween_property(self, "self_modulate", transparent, duration).from_current()
      await tween.finished
      queue_free()
  ```
- `animated_ghost_trail.gd` 的 `set_spawn_rate()` 根据导出参数改变 `Timer.wait_time`，允许在编辑器中快速预览残影密度。
- `GhostTrail` 节点启用 `show_behind_parent`，确保残影绘制在主体下方。

## 关键参数

- `spawn_rate`（脚本导出，默认 0.1）：生成残影的频率（每秒次数）。
- `is_emitting`：开启或关闭残影；默认关闭，需要脚本激活。
- 粒子 `lifetime = 0.5`：残影持续时间。
- `amount = 16`：同时存在的残影数量。

## 性能与常见陷阱

- 残影本质为 Sprite 副本，纹理越大对填充率影响越高，必要时可以降低分辨率或裁剪透明区域。
- 若角色会翻转，记得同步设置 `flip_h`/`flip_v`，脚本已自动处理。
- 轨迹过长时可能出现“方块”式消失，可适当延长 `lifetime` 并增加 `amount`。

## 复用流程

1. 若只需粒子拖尾，将 `ghost_trail.tscn` 作为角色子节点，调整 `emitting` 与 `texture`。
2. 若需自定义残影图像，可将 `texture` 指向角色动画帧图集，或在播放动画时手动复制 `Sprite`。
3. 使用 `animated_ghost_trail.tscn` 时，调用 `set_is_emitting(true)` 开启，`set_spawn_rate()` 调整频率。
4. 可结合 `Tween` 或动画控制残影颜色，营造多彩轨迹。

## 资源关联

- 纹理：`res://assets/topdown-player.svg`（示例角色）。
- 脚本：`animated_ghost_trail.gd`、`fading_sprite.gd`。
- 子场景：`animated_ghost_trail.tscn`、`fading_sprite.tscn`。

## 预览

- TODO：添加 `ghost_trail.gif` 预览。
