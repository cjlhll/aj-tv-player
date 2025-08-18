package com.tvplayer.webdav.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.tvplayer.webdav.di.TmdbClient")
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
public final class WebDAVModule_ProvideTmdbOkHttpClientFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return provideTmdbOkHttpClient();
  }

  public static WebDAVModule_ProvideTmdbOkHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient provideTmdbOkHttpClient() {
    return Preconditions.checkNotNullFromProvides(WebDAVModule.INSTANCE.provideTmdbOkHttpClient());
  }

  private static final class InstanceHolder {
    private static final WebDAVModule_ProvideTmdbOkHttpClientFactory INSTANCE = new WebDAVModule_ProvideTmdbOkHttpClientFactory();
  }
}
