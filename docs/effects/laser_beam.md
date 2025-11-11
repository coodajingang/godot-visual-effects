# 激光束（Laser Beam 2D）

- **场景路径**：`res://laser_beam/laser_beam_2d.tscn`
- **适用 Godot 版本**：4.3+
- **分类**：[粒子系统](../categories/particles.md) · [能量束与投射物](../categories/energy_and_projectile.md) · [屏幕空间 / 后处理](../categories/screen_space.md) · [2D 效果](../categories/dimension_2d.md)

## 效果简介

可命中检测的 2D 激光束，利用 `RayCast2D` 计算碰撞点，同时以 Line2D 和多组粒子渲染射线主体、起点光晕和命中火花。适合科幻武器、采集激光等持续束状效果。

## 节点结构

- `LaserBeam2D (RayCast2D)`：根节点与脚本宿主，负责碰撞检测与控制发射长度。
- `FillLine2D (Line2D)`：可调宽度的主束体。
- `CastingParticles2D (GPUParticles2D)`：武器端溢出的能量粒子。
- `BeamParticles2D (GPUParticles2D)`：沿束体分布的填充粒子，通过 `emission_box_extents` 拉伸长度。
- `CollisionParticles2D (GPUParticles2D)`：命中点火花。

## 核心技术

- 脚本 `laser_beam_2d.gd` 控制束体：
  ```gdscript
  func _physics_process(delta: float) -> void:
      target_position = (target_position + Vector2.RIGHT * cast_speed * delta).limit_length(max_length)
      cast_beam()
  
  func cast_beam() -> void:
      force_raycast_update()
      collision_particles.emitting = is_colliding()
      if is_colliding():
          var cast_point = to_local(get_collision_point())
          collision_particles.global_rotation = get_collision_normal().angle()
          collision_particles.position = cast_point
      fill.points[1] = cast_point
      beam_particles.position = cast_point * 0.5
      beam_particles.process_material.emission_box_extents.x = cast_point.length() * 0.5
  ```
- `appear()` / `disappear()` 利用 Tween 插值 Line2D 的 `width` 实现柔和展开/收束。
- `BeamParticles2D` 设置 `particle_flag_align_y = true`，确保粒子朝束体方向移动。

## 关键参数

- `cast_speed`：束体伸展速度（默认 7000）。
- `max_length`：最大射程（默认 1400）。
- `growth_time`：宽度 Tween 动画时间。
- `BeamParticles2D.amount = 50`，可按性能需求调整。

## 性能与常见陷阱

- 频繁开启/关闭激光时，Tween 的创建开销可能累积，脚本已在新 Tween 前调用 `tween.kill()`，扩展时请保留该逻辑。
- `BeamParticles2D` 的 `visibility_rect` 较大，如在大型场景中使用应根据实际摄像机尺寸调整。
- 若多个激光共享同一材质，可将 `process_material` 提升为资源并启用 `local_to_scene`，避免相互覆盖。

## 复用流程

1. 实例化 `LaserBeam2D`，将其作为武器子节点，设置本地朝向。
2. 调用 `set_is_casting(true/false)` 控制开火状态；脚本公开属性便于外部脚本绑定输入。
3. 若需对不同目标射击，可在 `_physics_process` 里更新 `RayCast2D` 的 `target_position` 或旋转父节点。
4. 如需改变颜色，调整 `FillLine2D.default_color` 与各粒子 `self_modulate`。

## 资源关联

- 纹理：`res://assets/glowing_circle.png`。
- 脚本：`laser_beam_2d.gd`。

## 预览

- TODO：添加 `laser_beam.gif` 预览。
