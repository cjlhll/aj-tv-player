package com.tvplayer.webdav.di;

import com.tvplayer.webdav.data.tmdb.TmdbApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class WebDAVModule_ProvideTmdbApiServiceFactory implements Factory<TmdbApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public WebDAVModule_ProvideTmdbApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TmdbApiService get() {
    return provideTmdbApiService(retrofitProvider.get());
  }

  public static WebDAVModule_ProvideTmdbApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new WebDAVModule_ProvideTmdbApiServiceFactory(retrofitProvider);
  }

  public static TmdbApiService provideTmdbApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(WebDAVModule.INSTANCE.provideTmdbApiService(retrofit));
  }
}
