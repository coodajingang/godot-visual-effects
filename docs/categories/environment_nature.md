# 自然与场景氛围类效果

用于营造环境氛围、植被生长或背景动态的效果，通常持续播放且对性能要求稳定。

| 效果 | 场景 | 技术亮点 | 应用建议 |
| --- | --- | --- | --- |
| [星空背景](../effects/star_field.md) | `res://star_field/star_field.tscn` | 大范围粒子，随机旋转 | 作为太空/夜空背景，建议配合 Parallax 层 |
| [藤蔓蔓延](../effects/vines_3d.md) | `res://vines_3d/vines.tscn` | 顶点位移模拟藤蔓生长 | 适合关卡演出、建筑侵蚀效果，可调 `growth` 实现时间控制 |

## 扩展方向

- 星空可与 `WorldEnvironment` 的雾效、光晕结合，形成更深的空间层次。
- 藤蔓 Shader 接受顶点颜色驱动，可通过外部 DCC 工具定制生长顺序，实现多分支扩散。
