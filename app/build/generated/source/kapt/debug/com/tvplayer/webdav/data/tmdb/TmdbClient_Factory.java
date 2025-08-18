package com.tvplayer.webdav.data.tmdb;

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
public final class TmdbClient_Factory implements Factory<TmdbClient> {
  private final Provider<TmdbApiService> apiServiceProvider;

  public TmdbClient_Factory(Provider<TmdbApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public TmdbClient get() {
    return newInstance(apiServiceProvider.get());
  }

  public static TmdbClient_Factory create(Provider<TmdbApiService> apiServiceProvider) {
    return new TmdbClient_Factory(apiServiceProvider);
  }

  public static TmdbClient newInstance(TmdbApiService apiService) {
    return new TmdbClient(apiService);
  }
}
