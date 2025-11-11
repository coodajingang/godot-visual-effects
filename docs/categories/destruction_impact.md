# 破坏与冲击类效果

聚焦爆炸、冲击波、地表破坏与武器命中的瞬时反馈，这些效果强调节奏、震感与视觉冲击力。

| 效果 | 冲击元素 | 触发方式 | 组合建议 |
| --- | --- | --- | --- |
| [爆炸](../effects/explosion.md) | 火焰、火花、烟雾 | 调用 `AnimationPlayer` 播放 `Explode` | 搭配 [屏幕冲击波](../effects/shockwave.md) 增强反馈 |
| [风格化爆炸](../effects/stylized_explosion_3d.md) | 冲击波、地面 Decal、OmniLight | 3D 动画驱动全流程 | 与 `Camera` 震动、碎片粒子组合打造大爆炸 |
| [地表破碎](../effects/ground_destruction_3d.md) | 地面抬升、裂纹、碎块 | `AnimationPlayer` 推动 Shader 参数 | 可与实际地形 Mesh 混合，形成动态战斗场景 |
| [屏幕冲击波](../effects/shockwave.md) | 环形波纹、摄像机抖动 | 手动启停粒子或调用 Demo 动画 | 可在命中事件中共用摄像机，实现通用冲击效果 |
| [枪口焰](../effects/muzzle_flash_3d.md) | 高亮枪火、火花粒子 | 不断循环或与开火事件同步 | 与 [激光束](../effects/laser_beam.md) 等投射物组合构建完整射击反馈 |

## 强化冲击感的手段

- 结合 `AnimationPlayer` 控制 `Camera` 的震动强度，保证爆发时画面动感。
- 使用音效与屏幕 UI 闪烁配合视觉效果，提升玩家体感。
