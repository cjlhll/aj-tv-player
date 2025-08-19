package com.tvplayer.webdav.ui.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.tvplayer.webdav.R;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * 媒体扫描Fragment
 * 允许用户扫描WebDAV目录并刮削媒体信息
 */
@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0007\u0018\u0000 .2\u00020\u0001:\u0001.B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0016H\u0002J\u0010\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u0019H\u0002J&\u0010\u001d\u001a\u0004\u0018\u00010\u001b2\u0006\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\u001a\u0010$\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\b\u0010%\u001a\u00020\u0019H\u0002J\u001c\u0010&\u001a\u00020\u00192\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00190(H\u0002J\b\u0010)\u001a\u00020\u0019H\u0002J\b\u0010*\u001a\u00020\u0019H\u0002J\b\u0010+\u001a\u00020\u0019H\u0002J\b\u0010,\u001a\u00020\u0019H\u0002J\b\u0010-\u001a\u00020\u0019H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000bX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006/"}, d2 = {"Lcom/tvplayer/webdav/ui/scanner/ScannerFragment;", "Landroidx/fragment/app/Fragment;", "()V", "btnPickMoviesDir", "Landroid/widget/Button;", "btnPickTvDir", "btnScanMovies", "btnScanTv", "progressBar", "Landroid/widget/ProgressBar;", "tvMoviesDir", "Landroid/widget/TextView;", "tvScanProgress", "tvScanStatus", "tvTvDir", "viewModel", "Lcom/tvplayer/webdav/ui/scanner/ScannerViewModel;", "getViewModel", "()Lcom/tvplayer/webdav/ui/scanner/ScannerViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "formatChinesePath", "", "path", "initViews", "", "view", "Landroid/view/View;", "observeViewModel", "onCreateView", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "setupListeners", "showPathSelectionDialog", "onChosen", "Lkotlin/Function1;", "startScan", "startScanMovies", "startScanTv", "updateMoviesDirDisplay", "updateTvDirDisplay", "Companion", "app_debug"})
public final class ScannerFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private android.widget.TextView tvMoviesDir;
    private android.widget.TextView tvTvDir;
    private android.widget.Button btnPickMoviesDir;
    private android.widget.Button btnPickTvDir;
    private android.widget.Button btnScanMovies;
    private android.widget.Button btnScanTv;
    private android.widget.TextView tvScanStatus;
    private android.widget.TextView tvScanProgress;
    private android.widget.ProgressBar progressBar;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.scanner.ScannerFragment.Companion Companion = null;
    
    public ScannerFragment() {
        super();
    }
    
    private final com.tvplayer.webdav.ui.scanner.ScannerViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initViews(android.view.View view) {
    }
    
    private final void updateMoviesDirDisplay() {
    }
    
    private final void updateTvDirDisplay() {
    }
    
    private final java.lang.String formatChinesePath(java.lang.String path) {
        return null;
    }
    
    private final void setupListeners() {
    }
    
    private final void observeViewModel() {
    }
    
    private final void showPathSelectionDialog(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onChosen) {
    }
    
    private final void startScanMovies() {
    }
    
    private final void startScanTv() {
    }
    
    private final void startScan() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/tvplayer/webdav/ui/scanner/ScannerFragment$Companion;", "", "()V", "newInstance", "Lcom/tvplayer/webdav/ui/scanner/ScannerFragment;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.tvplayer.webdav.ui.scanner.ScannerFragment newInstance() {
            return null;
        }
    }
}