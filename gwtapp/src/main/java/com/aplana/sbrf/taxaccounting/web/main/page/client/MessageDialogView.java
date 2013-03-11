/**
 * Copyright 2011 ArcBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.aplana.sbrf.taxaccounting.web.main.page.client;

import com.aplana.sbrf.taxaccounting.web.widget.log.ThrowableWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 * The view implementation for
 * {@link com.gwtplatform.samples.tab.client.presenter.GlobalDialogPresenterWidget}
 * .
 * 
 * @author Philippe Beaudoin
 */
public class MessageDialogView extends PopupViewImpl implements
		MessageDialogPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, MessageDialogView> {
	}
	
	@UiField
	DialogBox dialogBox;

	@UiField
	HasText message;

	@UiField
	Button okButton;
	
	@UiField
	ThrowableWidget throwableView;

	private final PopupPanel widget;

	@Inject
	public MessageDialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("okButton")
	void okButtonClicked(ClickEvent event) {
		widget.hide();
	}

	@Override
	public void setMeassege(String text) {
		message.setText(text);
	}

	@Override
	public void setModal(boolean modal) {
		asPopupPanel().setModal(true);
	}

	@Override
	public void setStackTrace(Throwable throwable) {
		throwableView.setThrowable(throwable);
		
	}


}
