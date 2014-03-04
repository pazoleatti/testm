package com.aplana.sbrf.taxaccounting.web.module.audit.client.archive;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Date;

/**
 * User: avanteev
 */
public class AuditArchiveDialogView extends PopupViewWithUiHandlers<AuditArchiveUiHandlers> implements AuditArchiveDialogPresenter.MyView, Editor<LogSystemFilter> {

    @UiField
    DateMaskBoxPicker toSearchDate;

    @Inject
    protected AuditArchiveDialogView(AuditArchiveViewUiBinder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));
        toSearchDate.setValue(new Date());
    }

    @Override
    public Date getToArchiveDate() {
        return toSearchDate.getValue();
    }

    interface AuditArchiveViewUiBinder extends UiBinder<PopupPanel, AuditArchiveDialogView> {
    }

    @UiHandler("cancelButton")
    public void onCancelButton(ClickEvent event){
        hide();
    }

    @UiHandler("archiveButton")
    public void onArchiveButton(ClickEvent event){
        Dialog.confirmMessage("Подтверждение", "При архиварации, данные будут выгружены в файл и удалены из Системы. Продолжить выполнение операции?", new DialogHandler() {
            @Override
            public void yes() {
                super.yes();
                hide();
                if( getUiHandlers()!= null){
                    getUiHandlers().onArchiveButtonClick();
                }
            }
        });

    }

}
