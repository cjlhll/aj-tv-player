package com.tvplayer.webdav.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.tvplayer.webdav.di.WebDAVClient")
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
public final class WebDAVModule_ProvideWebDAVOkHttpClientFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return provideWebDAVOkHttpClient();
  }

  public static WebDAVModule_ProvideWebDAVOkHttpClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient provideWebDAVOkHttpClient() {
    return Preconditions.checkNotNullFromProvides(WebDAVModule.INSTANCE.provideWebDAVOkHttpClient());
  }

  private static final class InstanceHolder {
    private static final WebDAVModule_ProvideWebDAVOkHttpClientFactory INSTANCE = new WebDAVModule_ProvideWebDAVOkHttpClientFactory();
  }
}
