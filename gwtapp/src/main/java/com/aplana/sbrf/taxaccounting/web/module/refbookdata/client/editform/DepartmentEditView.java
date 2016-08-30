package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

import java.util.Map;

/**
 * User: avanteev
 */
public class DepartmentEditView extends AbstractEditView implements DepartmentEditPresenter.MyView {
    @UiField
    VerticalPanel editPanel;
    @UiField
    Button save;
    @UiField
    Button cancel;
    @UiField
    HorizontalPanel buttonBlock;

    @Override
    public void updateRefBookPickerPeriod() {
        for (Map.Entry<RefBookColumn, HasValue> w : widgets.entrySet()) {
            if (w.getValue() instanceof RefBookPickerWidget) {
                RefBookPickerWidget rbw = (RefBookPickerWidget) w.getValue();
                rbw.setPeriodDates(null, null);
            }
        }
    }

    @Override
    public void showVersioned(boolean versioned) {

    }

    @Override
    Panel getRootFieldsPanel() {
        return editPanel;
    }

    @Override
    public void updateMode(FormMode mode) {
        switch (mode){
            case CREATE:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                break;
            case EDIT:
                save.setEnabled(true);
                cancel.setEnabled(true);
                updateWidgetsVisibility(true);
                break;
            case READ:
            case VIEW:
                save.setEnabled(false);
                cancel.setEnabled(false);
                updateWidgetsVisibility(false);
                break;
        }
    }

    interface DepartmentEditViewUiBinder extends UiBinder<HTMLPanel, DepartmentEditView> {
    }

    private static DepartmentEditViewUiBinder uiBinder = GWT.create(DepartmentEditViewUiBinder.class);

    @Inject
    @UiConstructor
    public DepartmentEditView() {
        initWidget(uiBinder.createAndBindUi(this));
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
}