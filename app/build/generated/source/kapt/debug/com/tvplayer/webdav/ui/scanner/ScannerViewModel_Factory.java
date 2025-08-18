package com.tvplayer.webdav.ui.scanner;

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
public final class ScannerViewModel_Factory implements Factory<ScannerViewModel> {
  private final Provider<MediaScanner> mediaScannerProvider;

  private final Provider<SimpleWebDAVClient> webdavClientProvider;

  private final Provider<WebDAVServerStorage> serverStorageProvider;

  private final Provider<MediaCache> mediaCacheProvider;

  public ScannerViewModel_Factory(Provider<MediaScanner> mediaScannerProvider,
      Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<WebDAVServerStorage> serverStorageProvider,
      Provider<MediaCache> mediaCacheProvider) {
    this.mediaScannerProvider = mediaScannerProvider;
    this.webdavClientProvider = webdavClientProvider;
    this.serverStorageProvider = serverStorageProvider;
    this.mediaCacheProvider = mediaCacheProvider;
  }

  @Override
  public ScannerViewModel get() {
    return newInstance(mediaScannerProvider.get(), webdavClientProvider.get(), serverStorageProvider.get(), mediaCacheProvider.get());
  }

  public static ScannerViewModel_Factory create(Provider<MediaScanner> mediaScannerProvider,
      Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<WebDAVServerStorage> serverStorageProvider,
      Provider<MediaCache> mediaCacheProvider) {
    return new ScannerViewModel_Factory(mediaScannerProvider, webdavClientProvider, serverStorageProvider, mediaCacheProvider);
  }

  public static ScannerViewModel newInstance(MediaScanner mediaScanner,
      SimpleWebDAVClient webdavClient, WebDAVServerStorage serverStorage, MediaCache mediaCache) {
    return new ScannerViewModel(mediaScanner, webdavClient, serverStorage, mediaCache);
  }
}
