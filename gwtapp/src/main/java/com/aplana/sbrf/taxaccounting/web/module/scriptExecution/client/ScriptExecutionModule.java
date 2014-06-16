package com.aplana.sbrf.taxaccounting.web.module.scriptExecution.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль выполнения скриптов из конфигуратора
 */
public class ScriptExecutionModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(ScriptExecutionPresenter.class, ScriptExecutionPresenter.MyView.class,
                ScriptExecutionView.class, ScriptExecutionPresenter.MyProxy.class);
    }
}
