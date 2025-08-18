# 🎬 海报切换过渡动画增强

## 🎯 功能概述

为海报卡片添加丰富的过渡动画效果，包括多层次的焦点切换动画、播放按钮悬浮动画和评分标签滑入效果。

## ✨ 动画特性

### 1. 多层次焦点动画
```kotlin
// 主容器动画
- 缩放: 1.0f → 1.06f (350ms)
- 阴影: 8dp → 16dp
- 插值器: OvershootInterpolator(0.1f)

// 播放按钮动画 (延迟50ms)
- 透明度: 0.9f → 1.0f
- 缩放: 0.8f → 1.0f
- 插值器: OvershootInterpolator(0.2f)

// 评分标签动画 (延迟100ms)
- 滑入: translationX(20f → 0f)
- 透明度: 0.7f → 1.0f
- 缩放: 0.9f → 1.0f
```

### 2. 播放按钮悬浮动画
```kotlin
// 循环缩放动画
- 缩放范围: 1.0f ↔ 1.1f
- 动画时长: 1000ms
- 自动循环播放
- 仅在按钮可见时运行
```

### 3. 失去焦点恢复动画
```kotlin
// 平滑恢复到默认状态
- 主容器: 快速恢复 (350ms)
- 播放按钮: 轻微缩小 + 淡出
- 评分标签: 轻微淡出
- 插值器: AccelerateDecelerateInterpolator
```

## 🔧 技术实现

### 1. 新增 PosterFocusAnimator.kt
```kotlin
object PosterFocusAnimator {
    // 设置海报焦点动画
    fun setupPosterFocusAnimation(itemView, cardView, playButton, ratingBadge)
    
    // 播放按钮悬浮动画
    fun startPlayButtonFloatingAnimation(playButton)
    
    // 私有动画方法
    private fun animateFocusIn(...)
    private fun animateFocusOut(...)
}
```

### 2. 更新 MediaPosterAdapter.kt
```kotlin
// 在ViewHolder初始化时设置动画
val cardView = itemView.findViewById<CardView>(R.id.card_view)
PosterFocusAnimator.setupPosterFocusAnimation(
    itemView, cardView, ivPlayButton, tvRating
)

// 在bind方法中启动悬浮动画
PosterFocusAnimator.startPlayButtonFloatingAnimation(ivPlayButton)
```

### 3. 更新 item_media_poster.xml
```xml
<!-- 为CardView添加ID -->
<androidx.cardview.widget.CardView
    android:id="@+id/card_view"
    ... />
```

## 🎨 动画时序设计

### 获得焦点时序
```
0ms    ├─ 主容器开始缩放 + 阴影变化
50ms   ├─ 播放按钮淡入 + 缩放
100ms  ├─ 评分标签滑入 + 淡入
350ms  └─ 所有动画完成
```

### 失去焦点时序
```
0ms    ├─ 主容器开始恢复
0ms    ├─ 播放按钮开始淡出 (175ms)
0ms    ├─ 评分标签开始淡出 (175ms)
350ms  └─ 主容器恢复完成
```

### 悬浮动画循环
```
播放按钮持续悬浮:
0ms     ├─ 缩放到1.1f (500ms)
500ms   ├─ 缩放回1.0f (500ms)
1000ms  └─ 重新开始循环
```

## 📋 文件修改清单

### 新增文件
1. **PosterFocusAnimator.kt** - 海报专用动画系统
   - 多层次焦点动画
   - 错开时序控制
   - 播放按钮悬浮效果

### 修改文件
1. **MediaPosterAdapter.kt**
   - 添加CardView import
   - 集成PosterFocusAnimator
   - 启动播放按钮悬浮动画

2. **item_media_poster.xml**
   - 为CardView添加ID引用

## 🎯 动画效果对比

### 修改前
```
简单焦点效果:
- 基础缩放 (1.06f)
- 阴影变化
- 单一动画时长
```

### 修改后
```
丰富多层动画:
- 主容器: 缩放 + 阴影 + 弹性效果
- 播放按钮: 淡入 + 缩放 + 悬浮循环
- 评分标签: 滑入 + 淡入 + 缩放
- 错开时序: 50ms间隔，层次分明
```

## 🚀 用户体验提升

### 视觉层次
- ✅ **主次分明**: 主容器先动，细节后动
- ✅ **节奏感**: 50ms错开，不会同时开始
- ✅ **弹性效果**: OvershootInterpolator增加活力
- ✅ **持续吸引**: 播放按钮悬浮动画

### 交互反馈
- ✅ **即时响应**: 焦点变化立即开始动画
- ✅ **平滑过渡**: 350ms适中时长
- ✅ **状态清晰**: 焦点状态通过多种视觉元素体现
- ✅ **细节丰富**: 每个元素都有专门的动画

### TV适配
- ✅ **遥控器友好**: 动画时长适合遥控器操作节奏
- ✅ **大屏效果**: 在大屏幕上动画效果更明显
- ✅ **性能优化**: 使用硬件加速的属性动画
- ✅ **内存友好**: 动画结束后自动清理

## 🔍 动画参数调优

### 时长设置
```kotlin
ANIMATION_DURATION = 350L     // 主动画时长
STAGGER_DELAY = 50L          // 错开延迟
```

### 缩放比例
```kotlin
SCALE_FOCUSED = 1.06f        // 焦点缩放
SCALE_NORMAL = 1.0f          // 默认缩放
```

### 插值器选择
```kotlin
// 获得焦点: 弹性效果
OvershootInterpolator(0.1f)

// 失去焦点: 平滑过渡  
AccelerateDecelerateInterpolator()
```

## 📱 测试重点

### 动画流畅性
1. **焦点切换** - 确认动画平滑无卡顿
2. **快速切换** - 测试快速移动焦点的表现
3. **动画取消** - 确认动画能正确取消和重新开始

### 视觉效果
1. **层次感** - 确认错开时序效果明显
2. **弹性效果** - 确认OvershootInterpolator效果自然
3. **悬浮动画** - 确认播放按钮循环动画正常

### 性能表现
1. **内存使用** - 确认动画不会造成内存泄漏
2. **CPU占用** - 确认动画性能开销合理
3. **电池消耗** - 确认悬浮动画不会过度耗电

---
**增强完成时间**: 2025-08-15
**主要特性**: 多层次动画 + 错开时序 + 悬浮效果
**用户体验**: 丰富的视觉反馈 + 平滑的过渡效果
