package com.tvplayer.webdav.data.storage;

import android.content.SharedPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MediaCache_Factory implements Factory<MediaCache> {
  private final Provider<SharedPreferences> prefsProvider;

  public MediaCache_Factory(Provider<SharedPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public MediaCache get() {
    return newInstance(prefsProvider.get());
  }

  public static MediaCache_Factory create(Provider<SharedPreferences> prefsProvider) {
    return new MediaCache_Factory(prefsProvider);
  }

  public static MediaCache newInstance(SharedPreferences prefs) {
    return new MediaCache(prefs);
  }
}
