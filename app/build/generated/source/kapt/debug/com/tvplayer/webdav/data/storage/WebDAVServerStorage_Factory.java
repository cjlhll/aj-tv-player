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
public final class WebDAVServerStorage_Factory implements Factory<WebDAVServerStorage> {
  private final Provider<SharedPreferences> prefsProvider;

  public WebDAVServerStorage_Factory(Provider<SharedPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public WebDAVServerStorage get() {
    return newInstance(prefsProvider.get());
  }

  public static WebDAVServerStorage_Factory create(Provider<SharedPreferences> prefsProvider) {
    return new WebDAVServerStorage_Factory(prefsProvider);
  }

  public static WebDAVServerStorage newInstance(SharedPreferences prefs) {
    return new WebDAVServerStorage(prefs);
  }
}
