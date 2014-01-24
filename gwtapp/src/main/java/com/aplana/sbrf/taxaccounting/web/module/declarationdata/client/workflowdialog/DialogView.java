package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

public class DialogView extends PopupViewWithUiHandlers<DialogUiHandlers> implements DialogPresenter.MyView {

    private static final int MAX_LENGTH = 255;

    public interface Binder extends UiBinder<PopupPanel, DialogView> {
	}

	@UiField
	TextArea data;

	@UiField
	Button okButton;

	@UiField
	Button cancelButton;

    @UiField
    Label textLengthLabel;

	private final PopupPanel widget;

    @Inject
    public DialogView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        data.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                setDataLength();
            }
        });
        data.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setDataLength();
            }
        });
    }

    private void setDataLength() {
        textLengthLabel.setText("Длина " + data.getText().length() + " (макс. " + MAX_LENGTH + ")");
    }

    @Override
    public Widget asWidget() {
		return widget;
	}

	@Override
	public void clearInput(){
		data.setText("");
	}

	@Override
	public String getComment(){
		return data.getText();
	}

	@UiHandler("okButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().onConfirm();
		}
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().hide();
		}
	}
}
