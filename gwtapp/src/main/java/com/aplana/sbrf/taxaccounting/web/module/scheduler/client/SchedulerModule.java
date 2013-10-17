package com.aplana.sbrf.taxaccounting.web.module.scheduler.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы "Планировщик задач"
 *
 * @author dloshkarev
 *
 */
public class SchedulerModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(TaskListPresenter.class, TaskListPresenter.MyView.class,
                TaskListView.class, TaskListPresenter.MyProxy.class);
        bindPresenter(TaskPresenter.class, TaskPresenter.MyView.class,
                TaskView.class, TaskPresenter.MyProxy.class);
	}
}