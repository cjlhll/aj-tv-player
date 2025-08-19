package com.tvplayer.webdav.ui.details;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class VideoDetailsViewModel_Factory implements Factory<VideoDetailsViewModel> {
  @Override
  public VideoDetailsViewModel get() {
    return newInstance();
  }

  public static VideoDetailsViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static VideoDetailsViewModel newInstance() {
    return new VideoDetailsViewModel();
  }

  private static final class InstanceHolder {
    private static final VideoDetailsViewModel_Factory INSTANCE = new VideoDetailsViewModel_Factory();
  }
}
