package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Загрузка ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 *
 */
public class UploadTransportDataModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(UploadTransportDataPresenter.class, UploadTransportDataPresenter.MyView.class,
                UploadTransportDataView.class, UploadTransportDataPresenter.MyProxy.class);
	}
}