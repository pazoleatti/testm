package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.renameDialog;

import com.aplana.gwt.client.ModalWindow;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.inject.Inject;

import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;

/**
 * Вью диалогового окна "Период применения изменений в печатных формах"
 *
 * @author aivanov
 */
public class RenameDialogView extends ViewWithUiHandlers<RenameDialogUiHandlers>
        implements RenameDialogPresenter.MyView {

    public interface Binder extends UiBinder<ModalWindow, RenameDialogView> {
    }

    @UiField
    ModalWindow deadlineDialog;
    @UiField
    DateMaskBoxPicker dateFrom;
    @UiField
    DateMaskBoxPicker dateTo;
    @UiField
    Button okButton;

    ConfirmButtonClickHandler buttonClickHandler;

    @Inject
    public RenameDialogView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("okButton")
    public void handleClick(ClickEvent event) {
        if (dateFrom.getValue() == null) {
            Dialog.warningMessage("Поле \"С\" должно быть заполнено");
        } else {
            if (dateTo.getValue() != null && dateFrom.getValue().compareTo(dateTo.getValue()) == 1) {
                Dialog.warningMessage("Значение поля \"С\" должно быть меньше значения поля \"по\"");
            } else {
                buttonClickHandler.onClick(dateFrom.getValue(), dateTo.getValue());
                deadlineDialog.hide();
            }
        }
    }

    @Override
    public void open(ConfirmButtonClickHandler buttonClickHandler) {
        this.buttonClickHandler = buttonClickHandler;
        deadlineDialog.center();
    }

    @Override
    public Date getDateFrom() {
        return dateFrom.getValue();
    }

    @Override
    public Date getDateTo() {
        return dateTo.getValue();
    }


}
