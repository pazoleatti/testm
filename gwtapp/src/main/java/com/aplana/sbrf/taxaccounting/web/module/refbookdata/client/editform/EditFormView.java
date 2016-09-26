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
	Button save, cancel;

    @UiField
    DateMaskBoxPicker versionStart, versionEnd;
    @UiField
    LinkButton allVersion;
    @UiField
    HorizontalPanel buttonBlock;
    @UiField
    Label startVersionDateLabel, endVersionDateLabel, separator;

    protected Date versionStartDate;
    protected Date versionEndDate;

	@Inject
	@UiConstructor
	public EditFormView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

        versionStart.setStartLimitDate(new Date(0));//01.01.1970
        versionStart.setEndLimitDate(new Date(4133894400000L));//31.12.2100
        versionStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                getUiHandlers().setIsFormModified(isVersionDatesChanged());
                if (versionEnd.getValue() != null && event.getValue() != null && event.getValue().after(versionEnd.getValue())) {
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
                    updateRefBookPeriod(event.getValue(), versionEnd.getValue());
                }
            }
        });
        versionEnd.setStartLimitDate(new Date(0));//01.01.1970
        versionEnd.setEndLimitDate(new Date(4133894400000L));//31.12.2100
        versionEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                getUiHandlers().setIsFormModified(isVersionDatesChanged());
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
                    updateRefBookPeriod(versionStart.getValue(), event.getValue());
                }
            }
        });
        versionStart.setCanBeEmpty(false);
        versionEnd.setCanBeEmpty(true);
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
        setVersionFrom(versionData.getVersionStart());
        setVersionTo(versionData.getVersionEnd());
        allVersion.setVisible(!isVersionMode && getUiHandlers().isVersioned());
        allVersion.setText("Все версии ("+versionData.getVersionCount()+")");
        /*allVersion.setHref("#"
                + RefBookDataTokens.REFBOOK_VERSION
                + ";" + RefBookDataTokens.REFBOOK_DATA_ID  + "=" + refBookId
                + ";" + RefBookDataTokens.REFBOOK_RECORD_ID + "=" + refBookRecordId);*/
    }

    @Override
    public void setVersionMode(boolean versionMode) {
        super.setVersionMode(versionMode);
        allVersion.setVisible(!versionMode && getUiHandlers().isVersioned());
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
    public void setAllVersionVisible(boolean isVisible) {
        if (!isVersionMode()) {
            allVersion.setVisible(isVisible);
        }
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
        versionStartDate = value;
    }

    @Override
    public void setVersionTo(Date value) {
        versionEnd.setValue(value);
        versionEndDate = value;
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
                //allVersion.setVisible(true && !isVersionMode);
                break;
            case READ:
                save.setVisible(false);
                cancel.setVisible(false);
                versionStart.setEnabled(false);
                versionEnd.setEnabled(false);
                updateWidgetsVisibility(false);
                break;
            case VIEW:
                save.setVisible(true);
                cancel.setVisible(true);
                save.setEnabled(false);
                cancel.setEnabled(false);
                versionStart.setEnabled(false);
                versionEnd.setEnabled(false);
                updateWidgetsVisibility(false);
                //allVersion.setVisible(versioned);
                break;
        }
    }

    @Override
    public boolean checkCorrectnessForSave() {
        if (versioned && versionStart.getValue() == null) {
            Dialog.warningMessage(getUiHandlers().getTitle(), "Не указана дата начала актуальности");
            return false;
        }
        return true;
    }

    @UiHandler("save")
	void saveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSaveClicked(false);
		}
	}

	@UiHandler("cancel")
	void cancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
    }

    @Override
    public void lock(boolean isLock) {
        save.setEnabled(!isLock);
        cancel.setEnabled(!isLock);
    }

    private boolean isStartVersionChanged() {
        if (versionStart.getValue() == null) {
            return versionStartDate != null;
        }
        return !versionStart.getValue().equals(versionStartDate);
    }

    private boolean isEndVersionChanged() {
        if (versionEnd.getValue() == null) {
            return versionEndDate != null;
        }
        return !versionEnd.getValue().equals(versionEndDate);
    }

    @Override
    protected boolean isVersionDatesChanged() {
        return isStartVersionChanged() || isEndVersionChanged();
    }
}
