# 电视剧海报焦点效果修复说明

## 问题描述
电视剧海报没有遥控器焦点效果，与电影海报的焦点效果不一致，用户体验不统一。

## 问题分析
通过代码分析发现：
1. **MediaPosterAdapter**（电影海报）使用了`PosterFocusAnimator.setupPosterFocusAnimation()`来设置丰富的焦点动画效果
2. **TVSeriesAdapter**（电视剧海报）只使用了简单的`setOnFocusChangeListener`，没有焦点动画效果
3. 两个适配器使用了相同的布局文件`item_media_poster.xml`，但焦点处理方式不同

## 解决方案
修改`TVSeriesAdapter`，使其使用与`MediaPosterAdapter`相同的焦点动画效果。

### 主要修改内容

#### 1. 添加必要的导入
```kotlin
import com.tvplayer.webdav.ui.home.PosterFocusAnimator
import com.tvplayer.webdav.ui.home.FocusHighlightHelper
```

#### 2. 修改焦点效果设置
**修改前：**
```kotlin
// 设置CardView的点击和焦点事件
val cardView = itemView.findViewById<CardView>(R.id.card_view)
if (cardView != null) {
    cardView.setOnClickListener {
        onSeriesClick(series)
    }

    cardView.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            onItemFocused?.invoke(series)
        }
    }
}
```

**修改后：**
```kotlin
// 设置焦点效果（使用与电影海报相同的焦点动画）
val cardView = itemView.findViewById<CardView>(R.id.card_view)
if (cardView != null) {
    // 设置点击事件在CardView上
    cardView.setOnClickListener {
        onSeriesClick(series)
    }

    // 使用PosterFocusAnimator设置焦点动画，与MediaPosterAdapter保持一致
    PosterFocusAnimator.setupPosterFocusAnimation(
        cardView, cardView, null, tvRating
    ) { hasFocus ->
        if (hasFocus) {
            onItemFocused?.invoke(series)
        }
    }
} else {
    // 备用方案
    itemView.setOnClickListener {
        onSeriesClick(series)
    }
    
    // 使用FocusHighlightHelper作为备用焦点效果
    FocusHighlightHelper.setupFocusHighlight(itemView)
    
    itemView.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            onItemFocused?.invoke(series)
        }
    }
}
```

## 修复效果

### 焦点动画效果
现在电视剧海报将具有与电影海报完全一致的焦点动画效果：
- **缩放动画**：获得焦点时轻微放大（1.04倍），失去焦点时恢复原始大小
- **边框效果**：焦点状态下的边框高亮
- **评分标签动画**：评分标签的滑入滑出效果
- **平滑过渡**：使用OvershootInterpolator和AccelerateDecelerateInterpolator实现自然的动画效果

### 用户体验提升
1. **视觉一致性**：电影和电视剧海报的焦点效果完全统一
2. **交互反馈**：遥控器导航时提供清晰的视觉反馈
3. **动画流畅性**：400ms的动画时长，错开动画延迟，提供层次感

## 技术细节

### PosterFocusAnimator功能
- **主容器动画**：缩放、透明度变化
- **播放按钮动画**：淡入淡出、缩放效果
- **评分标签动画**：滑入滑出、淡入淡出、缩放效果
- **动画插值器**：OvershootInterpolator（弹性效果）、AccelerateDecelerateInterpolator（平滑过渡）

### 备用方案
当CardView不可用时，使用`FocusHighlightHelper.setupFocusHighlight()`作为备用焦点效果，确保在各种情况下都能提供焦点反馈。

## 兼容性说明
- 所有现有功能保持不变
- 焦点导航逻辑完全兼容
- 支持不同屏幕尺寸和分辨率
- 动画性能优化，不影响滚动流畅性

## 测试建议
1. 使用遥控器导航到电视剧海报
2. 验证焦点动画效果是否与电影海报一致
3. 检查焦点切换的流畅性
4. 确认评分标签等UI元素的动画效果

## 后续优化建议
1. 可以考虑为不同类型的媒体添加独特的焦点效果
2. 优化动画性能，减少内存占用
3. 添加用户自定义焦点效果的选项
4. 考虑添加触觉反馈（如果设备支持） 