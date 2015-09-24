package com.aplana.sbrf.taxaccounting.web.module.scriptsimport.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль выполнения скриптов из конфигуратора
 */
public class ScriptsImportModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(ScriptsImportPresenter.class, ScriptsImportPresenter.MyView.class,
                ScriptsImportView.class, ScriptsImportPresenter.MyProxy.class);
    }
}
