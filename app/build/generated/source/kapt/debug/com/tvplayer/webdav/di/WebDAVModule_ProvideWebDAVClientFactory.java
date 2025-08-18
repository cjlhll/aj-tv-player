package com.tvplayer.webdav.di;

import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
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
public final class WebDAVModule_ProvideWebDAVClientFactory implements Factory<SimpleWebDAVClient> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public WebDAVModule_ProvideWebDAVClientFactory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public SimpleWebDAVClient get() {
    return provideWebDAVClient(okHttpClientProvider.get());
  }

  public static WebDAVModule_ProvideWebDAVClientFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new WebDAVModule_ProvideWebDAVClientFactory(okHttpClientProvider);
  }

  public static SimpleWebDAVClient provideWebDAVClient(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(WebDAVModule.INSTANCE.provideWebDAVClient(okHttpClient));
  }
}
