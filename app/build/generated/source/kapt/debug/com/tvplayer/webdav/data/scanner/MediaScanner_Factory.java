package com.tvplayer.webdav.data.scanner;

import com.tvplayer.webdav.data.tmdb.TmdbClient;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
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
public final class MediaScanner_Factory implements Factory<MediaScanner> {
  private final Provider<SimpleWebDAVClient> webdavClientProvider;

  private final Provider<TmdbClient> tmdbClientProvider;

  public MediaScanner_Factory(Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<TmdbClient> tmdbClientProvider) {
    this.webdavClientProvider = webdavClientProvider;
    this.tmdbClientProvider = tmdbClientProvider;
  }

  @Override
  public MediaScanner get() {
    return newInstance(webdavClientProvider.get(), tmdbClientProvider.get());
  }

  public static MediaScanner_Factory create(Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<TmdbClient> tmdbClientProvider) {
    return new MediaScanner_Factory(webdavClientProvider, tmdbClientProvider);
  }

  public static MediaScanner newInstance(SimpleWebDAVClient webdavClient, TmdbClient tmdbClient) {
    return new MediaScanner(webdavClient, tmdbClient);
  }
}
