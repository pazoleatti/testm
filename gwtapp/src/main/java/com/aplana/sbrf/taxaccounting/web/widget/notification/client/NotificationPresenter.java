package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class NotificationPresenter extends PresenterWidget<NotificationPresenter.MyView>{
	
	@Inject
	public NotificationPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
	}

	public static interface MyView extends View{
		
	}

}
