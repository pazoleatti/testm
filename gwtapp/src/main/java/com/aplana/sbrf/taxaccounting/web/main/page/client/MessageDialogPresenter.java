package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class MessageDialogPresenter extends
		PresenterWidget<MessageDialogPresenter.MyView> {

	private MessageEvent messageEvent;

	public interface MyView extends PopupView {

		void setMeassege(String text);
		
		void setStackTrace(Throwable throwable);

		void setModal(boolean modal);
	}

	@Inject
	public MessageDialogPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
	}
	
	@Override
	protected void onReveal() {
		getView().setMeassege(messageEvent.getMessage());
		getView().setModal(true);
		getView().setStackTrace(messageEvent.getThrowable());
		super.onReveal();
	}

	public MessageEvent getMessageEvent() {
		return messageEvent;
	}

	public void setMessageEvent(MessageEvent messageEvent) {
		this.messageEvent = messageEvent;
	}



}
