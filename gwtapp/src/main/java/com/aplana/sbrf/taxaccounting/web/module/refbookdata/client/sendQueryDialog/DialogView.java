package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendQueryDialog;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

public class DialogView extends PopupViewWithUiHandlers<DialogUiHandlers> implements DialogPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, DialogView> {
	}

	@UiField
	TextArea textArea;

	@UiField
	Button sendButton;

	@UiField
	Button cancelButton;

	private final PopupPanel widget;

	@Inject
	public DialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
		widget.setAnimationEnabled(true);
	}

    @Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clearInput(){
		textArea.setText("");
	}

	@UiHandler("sendButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().onConfirm();
		}
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}

	@Override
	public String getComment(){
		return textArea.getText();
	}
}
