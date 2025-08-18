package com.tvplayer.webdav.ui.home;

import com.tvplayer.webdav.data.scanner.MediaScanner;
import com.tvplayer.webdav.data.storage.MediaCache;
import com.tvplayer.webdav.data.storage.WebDAVServerStorage;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<SimpleWebDAVClient> webdavClientProvider;

  private final Provider<WebDAVServerStorage> serverStorageProvider;

  private final Provider<MediaScanner> mediaScannerProvider;

  private final Provider<MediaCache> mediaCacheProvider;

  public HomeViewModel_Factory(Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<WebDAVServerStorage> serverStorageProvider,
      Provider<MediaScanner> mediaScannerProvider, Provider<MediaCache> mediaCacheProvider) {
    this.webdavClientProvider = webdavClientProvider;
    this.serverStorageProvider = serverStorageProvider;
    this.mediaScannerProvider = mediaScannerProvider;
    this.mediaCacheProvider = mediaCacheProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(webdavClientProvider.get(), serverStorageProvider.get(), mediaScannerProvider.get(), mediaCacheProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<WebDAVServerStorage> serverStorageProvider,
      Provider<MediaScanner> mediaScannerProvider, Provider<MediaCache> mediaCacheProvider) {
    return new HomeViewModel_Factory(webdavClientProvider, serverStorageProvider, mediaScannerProvider, mediaCacheProvider);
  }

  public static HomeViewModel newInstance(SimpleWebDAVClient webdavClient,
      WebDAVServerStorage serverStorage, MediaScanner mediaScanner, MediaCache mediaCache) {
    return new HomeViewModel(webdavClient, serverStorage, mediaScanner, mediaCache);
  }
}
