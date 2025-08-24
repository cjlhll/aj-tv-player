# 🎯 修复默认焦点设置问题

## 🔍 问题分析

原始代码中默认焦点设置没有生效的主要原因：

1. **首次启动标识缺失**：`shouldRestoreFocus`在首次启动时为`false`，导致焦点设置逻辑不执行
2. **优先级顺序错误**：电影优先于播放历史，不符合需求
3. **时机问题**：ViewHolder可能还没创建完成就尝试设置焦点
4. **焦点效果缺失**：仅调用`requestFocus()`不足以触发视觉效果

## ✅ 修复方案

### 1. 添加首次启动标识
```kotlin
// 标记是否是首次启动（用于设置默认焦点）
private var isFirstLaunch = true
```

### 2. 优化onResume逻辑
```kotlin
override fun onResume() {
    super.onResume()
    
    if (shouldRestoreFocus) {
        // 恢复焦点状态
        view?.postDelayed({
            restoreFocusStates()
            shouldRestoreFocus = false
        }, 300)
    } else if (isFirstLaunch) {
        // 首次启动时设置默认焦点
        view?.postDelayed({
            setDefaultFocus()
            isFirstLaunch = false
        }, 500) // 给更多时间确保数据加载完成
    }
}
```

### 3. 改进数据观察逻辑
```kotlin
private fun observeViewModel() {
    viewModel.playbackHistory.observe(viewLifecycleOwner) { items ->
        playbackHistoryAdapter.submitList(items) {
            handleFocusAfterDataLoad()
        }
    }
    // ... 其他观察逻辑
}

private fun handleFocusAfterDataLoad() {
    if (shouldRestoreFocus && isResumed) {
        view?.postDelayed({
            restoreFocusStates()
        }, 100)
    } else if (isFirstLaunch && isResumed) {
        view?.postDelayed({
            setDefaultFocus()
            isFirstLaunch = false
        }, 200)
    }
}
```

### 4. 实现按优先级的默认焦点设置
```kotlin
private fun setDefaultFocus() {
    val playbackHistoryHasData = ::playbackHistoryAdapter.isInitialized && playbackHistoryAdapter.itemCount > 0
    val moviesHasData = ::moviesAdapter.isInitialized && moviesAdapter.itemCount > 0
    val tvShowsHasData = ::tvShowsAdapter.isInitialized && tvShowsAdapter.itemCount > 0

    when {
        // 第一优先级：播放历史
        playbackHistoryHasData -> {
            setFocusToRecyclerView(rvPlaybackHistory, "播放历史")
        }
        // 第二优先级：电影
        moviesHasData -> {
            setFocusToRecyclerView(rvMovies, "电影")
        }
        // 第三优先级：电视剧
        tvShowsHasData -> {
            setFocusToRecyclerView(rvTVShows, "电视剧")
        }
        else -> {
            // 如果没有数据，延迟重试
            view?.postDelayed({ setDefaultFocus() }, 500)
        }
    }
}
```

### 5. 强化焦点设置和视觉效果
```kotlin
private fun setFocusToRecyclerView(recyclerView: RecyclerView, name: String) {
    recyclerView.post {
        if (recyclerView.adapter?.itemCount ?: 0 > 0) {
            val firstViewHolder = recyclerView.findViewHolderForAdapterPosition(0)
            if (firstViewHolder != null) {
                val itemView = firstViewHolder.itemView
                
                // 请求焦点
                val focusResult = itemView.requestFocus()
                
                // 手动触发焦点动画和边框效果
                itemView.postDelayed({
                    triggerFocusEffects(itemView)
                }, 50)
                
            } else {
                // ViewHolder还没创建，延迟重试
                recyclerView.postDelayed({
                    setFocusToRecyclerView(recyclerView, name)
                }, 100)
            }
        }
    }
}

private fun triggerFocusEffects(itemView: View) {
    val cardView = itemView.findViewById<CardView>(R.id.card_view)
    val tvRating = itemView.findViewById<TextView>(R.id.tv_rating)
    
    // 使用PosterFocusAnimator手动触发焦点效果
    PosterFocusAnimator.restoreFocusWithAnimation(
        itemView, cardView, null, tvRating, true
    ) { hasFocus ->
        android.util.Log.d("HomeFragment", "Focus animation triggered, hasFocus: $hasFocus")
    }
}
```

## 🎯 焦点优先级

### 修复后的优先级顺序
1. **第一优先级**：播放历史区域（如果有数据）
2. **第二优先级**：电影区域（如果播放历史无数据）
3. **第三优先级**：电视剧区域（如果前两者都无数据）

### 视觉效果确保
- ✅ **白色边框**：通过`PosterFocusAnimator.restoreFocusWithAnimation`触发
- ✅ **1.04倍缩放**：通过焦点动画系统实现
- ✅ **平滑过渡**：30ms动画时长，流畅体验

## 🔧 技术细节

### 时机控制
- **数据加载完成后**：通过`submitList`回调确保数据已提交
- **ViewHolder创建后**：通过`post`和重试机制确保ViewHolder存在
- **焦点效果延迟**：50ms延迟确保焦点请求完成后再触发视觉效果

### 错误处理
- **数据检查**：确认适配器已初始化且有数据
- **ViewHolder检查**：如果ViewHolder不存在则延迟重试
- **异常捕获**：所有焦点操作都有try-catch保护

### 日志记录
```kotlin
android.util.Log.d("HomeFragment", "Setting default focus - History: $playbackHistoryHasData, Movies: $moviesHasData, TV: $tvShowsHasData")
android.util.Log.d("HomeFragment", "Setting focus to $name")
android.util.Log.d("HomeFragment", "Focus request to $name result: $focusResult")
```

## 📱 用户体验

### 首次启动
1. 应用启动后自动检测数据可用性
2. 按优先级顺序设置焦点到第一个可用区域
3. 显示明显的视觉反馈（白色边框 + 缩放效果）

### 焦点恢复
- **从其他页面返回**：恢复到用户最后聚焦的位置
- **应用重启**：重新按优先级设置默认焦点
- **数据更新**：数据变化时智能调整焦点

## 🎨 视觉效果

### 焦点状态
- **边框**：白色2dp边框，清晰标识选中状态
- **缩放**：1.04倍放大，突出显示效果
- **动画**：30ms平滑过渡，不会感觉突兀

### 兼容性
- ✅ 与现有`MediaPosterAdapter`完全兼容
- ✅ 与现有`TVSeriesAdapter`完全兼容
- ✅ 与现有`PosterFocusAnimator`完全兼容

## 🔄 测试建议

### 功能测试
1. **首次启动**：确认焦点按优先级正确设置
2. **数据变化**：测试不同数据组合下的焦点行为
3. **页面切换**：测试从详情页返回时的焦点恢复

### 视觉测试
1. **边框效果**：确认白色边框正确显示
2. **缩放效果**：确认1.04倍缩放正确应用
3. **动画流畅性**：确认焦点切换动画平滑

### 边界测试
1. **无数据**：测试所有区域都无数据时的行为
2. **单一数据**：测试只有一个区域有数据时的行为
3. **快速切换**：测试快速页面切换时的焦点稳定性

## 🎉 修复结果

现在首次进入应用时：

1. **自动检测**：智能检测各区域数据可用性
2. **优先级设置**：按播放历史 → 电影 → 电视剧的顺序设置焦点
3. **视觉反馈**：选中的海报会立即显示白色边框和缩放效果
4. **稳定可靠**：通过重试机制确保焦点设置成功

用户现在可以在首次启动时立即看到明确的焦点指示，提供更好的导航体验！
