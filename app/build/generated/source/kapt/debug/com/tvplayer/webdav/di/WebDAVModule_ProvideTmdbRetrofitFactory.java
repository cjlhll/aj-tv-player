package com.tvplayer.webdav.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class WebDAVModule_ProvideTmdbRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public WebDAVModule_ProvideTmdbRetrofitFactory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public Retrofit get() {
    return provideTmdbRetrofit(okHttpClientProvider.get());
  }

  public static WebDAVModule_ProvideTmdbRetrofitFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new WebDAVModule_ProvideTmdbRetrofitFactory(okHttpClientProvider);
  }

  public static Retrofit provideTmdbRetrofit(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(WebDAVModule.INSTANCE.provideTmdbRetrofit(okHttpClient));
  }
}
