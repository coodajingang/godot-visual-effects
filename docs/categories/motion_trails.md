# 运动轨迹与残影类效果

本类别处理角色或武器快速移动时的拖尾、残影，强调持续性、方向感与渐隐控制。

| 效果 | 技术路径 | 典型用途 | 调整建议 |
| --- | --- | --- | --- |
| [鬼影拖尾](../effects/ghost_trail.md) | GPUParticles2D + Sprite 残影复制 | 动作游戏角色残影 | `spawn_rate` 决定残影密度，注意开启 `is_emitting` |
| [可编程拖尾](../effects/trail_2d.md) | Line2D 自定义脚本缓存轨迹点 | 飞行器尾迹、UI 手势轨迹 | `resolution`、`lifetime` 控制光带平滑度 |
| [剑气斩击](../effects/slash_3d.md) | 武器模型动画 + Slash Shader + 粒子火花 | ARPG 近战斩击 | `alpha_offset` 与动画曲线决定斩击消隐 |

## 设计建议

- 轨迹效果通常与角色移动速度直接相关，建议在脚本中读写速度/输入以控制发射率。
- 若需要溶解效果，可在拖尾 Shader 中引入噪声纹理替换线性渐隐。
