package com.tvplayer.webdav.di;

import android.content.SharedPreferences;
import com.tvplayer.webdav.data.storage.WebDAVServerStorage;
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
public final class StorageModule_ProvideWebDAVServerStorageFactory implements Factory<WebDAVServerStorage> {
  private final Provider<SharedPreferences> prefsProvider;

  public StorageModule_ProvideWebDAVServerStorageFactory(
      Provider<SharedPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public WebDAVServerStorage get() {
    return provideWebDAVServerStorage(prefsProvider.get());
  }

  public static StorageModule_ProvideWebDAVServerStorageFactory create(
      Provider<SharedPreferences> prefsProvider) {
    return new StorageModule_ProvideWebDAVServerStorageFactory(prefsProvider);
  }

  public static WebDAVServerStorage provideWebDAVServerStorage(SharedPreferences prefs) {
    return Preconditions.checkNotNullFromProvides(StorageModule.INSTANCE.provideWebDAVServerStorage(prefs));
  }
}
