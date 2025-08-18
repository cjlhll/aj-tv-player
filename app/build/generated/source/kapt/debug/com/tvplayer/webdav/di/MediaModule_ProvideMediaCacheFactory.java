package com.tvplayer.webdav.di;

import android.content.SharedPreferences;
import com.tvplayer.webdav.data.storage.MediaCache;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class MediaModule_ProvideMediaCacheFactory implements Factory<MediaCache> {
  private final Provider<SharedPreferences> prefsProvider;

  public MediaModule_ProvideMediaCacheFactory(Provider<SharedPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public MediaCache get() {
    return provideMediaCache(prefsProvider.get());
  }

  public static MediaModule_ProvideMediaCacheFactory create(
      Provider<SharedPreferences> prefsProvider) {
    return new MediaModule_ProvideMediaCacheFactory(prefsProvider);
  }

  public static MediaCache provideMediaCache(SharedPreferences prefs) {
    return Preconditions.checkNotNullFromProvides(MediaModule.INSTANCE.provideMediaCache(prefs));
  }
}
