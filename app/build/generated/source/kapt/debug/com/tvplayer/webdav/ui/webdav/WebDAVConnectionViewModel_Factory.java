package com.tvplayer.webdav.ui.webdav;

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
public final class WebDAVConnectionViewModel_Factory implements Factory<WebDAVConnectionViewModel> {
  private final Provider<SimpleWebDAVClient> webdavClientProvider;

  private final Provider<WebDAVServerStorage> serverStorageProvider;

  public WebDAVConnectionViewModel_Factory(Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<WebDAVServerStorage> serverStorageProvider) {
    this.webdavClientProvider = webdavClientProvider;
    this.serverStorageProvider = serverStorageProvider;
  }

  @Override
  public WebDAVConnectionViewModel get() {
    return newInstance(webdavClientProvider.get(), serverStorageProvider.get());
  }

  public static WebDAVConnectionViewModel_Factory create(
      Provider<SimpleWebDAVClient> webdavClientProvider,
      Provider<WebDAVServerStorage> serverStorageProvider) {
    return new WebDAVConnectionViewModel_Factory(webdavClientProvider, serverStorageProvider);
  }

  public static WebDAVConnectionViewModel newInstance(SimpleWebDAVClient webdavClient,
      WebDAVServerStorage serverStorage) {
    return new WebDAVConnectionViewModel(webdavClient, serverStorage);
  }
}
