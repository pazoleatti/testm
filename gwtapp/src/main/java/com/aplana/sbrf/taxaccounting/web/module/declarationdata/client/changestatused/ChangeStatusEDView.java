package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Date;

public class ChangeStatusEDView extends PopupViewWithUiHandlers<ChangeStatusEDUiHandlers> implements ChangeStatusEDPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, ChangeStatusEDView> {
	}

	@UiField
	Button okButton;

	@UiField
	Button cancelButton;

    @UiField
    Label docStateLabel;

    @UiField
    RefBookPickerWidget docStatePicker;

    @Inject
    public ChangeStatusEDView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));

        docStatePicker.setPeriodDates(new Date(), new Date());
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
            Dialog.confirmMessage("Отмена", "Отменить изменения?", new DialogHandler() {
                @Override
                public void yes() {
                    Dialog.hideMessage();
                    hide();
                }
            });
		}
	}


    @Override
    public Long getDocStateId() {
        return docStatePicker.getSingleValue();
    }

    @Override
    public void setDocStateId(Long docStateId) {
        docStatePicker.setSingleValue(docStateId);
    }
}
