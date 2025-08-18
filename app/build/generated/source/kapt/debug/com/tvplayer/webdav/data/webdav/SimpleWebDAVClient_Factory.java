package com.tvplayer.webdav.data.webdav;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class SimpleWebDAVClient_Factory implements Factory<SimpleWebDAVClient> {
  private final Provider<OkHttpClient> baseHttpClientProvider;

  public SimpleWebDAVClient_Factory(Provider<OkHttpClient> baseHttpClientProvider) {
    this.baseHttpClientProvider = baseHttpClientProvider;
  }

  @Override
  public SimpleWebDAVClient get() {
    return newInstance(baseHttpClientProvider.get());
  }

  public static SimpleWebDAVClient_Factory create(Provider<OkHttpClient> baseHttpClientProvider) {
    return new SimpleWebDAVClient_Factory(baseHttpClientProvider);
  }

  public static SimpleWebDAVClient newInstance(OkHttpClient baseHttpClient) {
    return new SimpleWebDAVClient(baseHttpClient);
  }
}
