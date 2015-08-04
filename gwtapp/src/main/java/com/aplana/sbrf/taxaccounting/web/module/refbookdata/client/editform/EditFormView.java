package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookRecordVersionData;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

import java.util.Date;
import java.util.Map;

public class EditFormView extends AbstractEditView implements EditFormPresenter.MyView{

    interface Binder extends UiBinder<Widget, EditFormView> { }

	@UiField
	VerticalPanel editPanel;
	@UiField
	Button save;
	@UiField
	Button cancel;

    @UiField
    DateMaskBoxPicker versionStart;
    @UiField
    DateMaskBoxPicker versionEnd;
    @UiField
    LinkButton allVersion;
    @UiField
    HorizontalPanel buttonBlock;
    @UiField
    Label startVersionDateLabel;
    @UiField
    Label endVersionDateLabel;
    @UiField
    Label separator;

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

        versionStart.setStartLimitDate(new Date(0));//01.01.1970
        versionStart.setEndLimitDate(new Date(4133894400000L));//31.12.2100
        versionStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                if (versionEnd.getValue() != null && event.getValue().after(versionEnd.getValue())) {
                    Dialog.errorMessage("Неправильно указан диапазон дат!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                }else if (event.getValue() == null){
                    Dialog.errorMessage("Введите дату начала!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    save.setEnabled(true);
                    cancel.setEnabled(true);
                }
            }
        });
        versionEnd.setStartLimitDate(new Date(0));//01.01.1970
        versionEnd.setEndLimitDate(new Date(4133894400000L));//31.12.2100
        versionEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                if (versionStart.getValue() != null && event.getValue() != null && event.getValue().before(versionStart.getValue())) {
                    Dialog.errorMessage("Неправильно указан диапазон дат!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                } else if (versionStart.getValue() == null){
                    Dialog.errorMessage("Введите дату начала!");
                    save.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    save.setEnabled(true);
                    cancel.setEnabled(true);
                }
            }
        });
        versionStart.setCanBeEmpty(true);
        versionStart.setCanBeEmpty(true);
	}

    @Override
    public void updateRefBookPickerPeriod() {
        Date start = versionStart.getValue();
        if (start == null) {
            start = new Date();
        }

        for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
            if (w.getValue() instanceof RefBookPickerWidget) {
                RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                rbw.setPeriodDates(start, versionEnd.getValue());
            }
        }
    }

    @Override
    Panel getRootFieldsPanel() {
        return editPanel;
    }

    @Override
    public HasClickHandlers getClickAllVersion() {
        return allVersion;
    }

    @Override
    public void fillVersionData(RefBookRecordVersionData versionData) {
        versionStart.setValue(versionData.getVersionStart());
        versionEnd.setValue(versionData.getVersionEnd());
        allVersion.setVisible(!isVersionMode && versioned);
        allVersion.setText("Все версии ("+versionData.getVersionCount()+")");
        /*allVersion.setHref("#"
                + RefBookDataTokens.refBookVersion
                + ";" + RefBookDataTokens.REFBOOK_DATA_ID  + "=" + refBookId
                + ";" + RefBookDataTokens.REFBOOK_RECORD_ID + "=" + refBookRecordId);*/
    }

    @Override
    public void setVersionMode(boolean versionMode) {
        super.setVersionMode(versionMode);
        allVersion.setVisible(!versionMode && versioned);
    }

    @Override
    public void showVersioned(boolean versioned) {
        this.versioned = versioned;
        versionStart.setVisible(versioned);
        versionEnd.setVisible(versioned);
        allVersion.setVisible(versioned);
        startVersionDateLabel.setVisible(versioned);
        endVersionDateLabel.setVisible(versioned);
        separator.setVisible(versioned);
    }

    @Override
    public Date getVersionFrom() {
        return versionStart.getValue();
    }

    @Override
    public Date getVersionTo() {
        return versionEnd.getValue();
    }

    @Override
    public void setVersionFrom(Date value) {
        versionStart.setValue(value);
    }

    @Override
    public void setVersionTo(Date value) {
        versionEnd.setValue(value);
    }

    @Override
    public void updateMode(FormMode mode) {
        switch (mode){
            case CREATE:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                versionStart.setEnabled(true);
                versionEnd.setEnabled(true);
                allVersion.setVisible(false);
                break;
            case EDIT:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                versionStart.setEnabled(true);
                versionEnd.setEnabled(true);
                allVersion.setVisible(true);
                break;
            case READ:
            case VIEW:
                save.setEnabled(false);
                cancel.setEnabled(false);
                versionStart.setEnabled(false);
                versionEnd.setEnabled(false);
                updateWidgetsVisibility(false);
                allVersion.setVisible(versioned);
                break;
        }
    }

    @UiHandler("save")
	void saveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
            if (versionStart.getValue() == null) {
                Dialog.warningMessage(getUiHandlers().getTitle(), "Не указана дата начала актуальности");
                return;
            }
			getUiHandlers().onSaveClicked(false);
		}
	}

	@UiHandler("cancel")
	void cancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
    }
}
