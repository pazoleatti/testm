package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.AuditFieldList;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.LogSystemAuditFilter;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPicker;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Arrays;
import java.util.Date;


/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFilterView extends ViewWithUiHandlers<AuditFilterUIHandlers>
        implements AuditFilterPresenter.MyView, Editor<LogSystemAuditFilter> {

    interface Binder extends UiBinder<Widget, AuditFilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<LogSystemAuditFilter, AuditFilterView>{}

    private MyDriver driver;

    @UiField
    DateMaskBoxPicker fromSearchDate;

    @UiField
    DateMaskBoxPicker toSearchDate;

    @UiField
    TextBox filter;

    @UiField
    RefBookPicker auditFieldList;

    @Editor.Ignore
    @UiField
    Button search;

    @Editor.Ignore
    @UiField
    CheckBox searchResults;

    @Editor.Ignore
    @UiField
    Label searchCriteria;

    @Override
    public boolean isSearchResults() {
        return searchResults.getValue();
    }

    @Override
    public LogSystemAuditFilter getFilterData() {
        return driver.flush();
    }

    @Override
    public boolean isChangeFilter() {
        return driver.isDirty();
    }

    @Override
    public void edit(LogSystemAuditFilter auditFilter) {
        driver.edit(auditFilter);
    }

    @Override
    public void clear() {
    }

    @Override
    public void setSearchCriteria(String str) {
        searchCriteria.setText(str);
    }

    @Override
    public void init() {
        searchResults.setValue(false);
        searchCriteria.setText("");
        LogSystemAuditFilter logSystemAuditFilter = new LogSystemAuditFilter();
        logSystemAuditFilter.setAuditFieldList(Arrays.asList(AuditFieldList.ALL.getId()));
        driver.edit(logSystemAuditFilter);
    }

    @Inject
    @UiConstructor
    public AuditFilterView(final Binder uiBinder, MyDriver driver) {
        initWidget(uiBinder.createAndBindUi(this));
        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
        auditFieldList.setPeriodDates(new Date(), new Date());

        KeyPressHandler keyPressHandler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getUnicodeCharCode() == KeyCodes.KEY_ENTER) {
                    search.click();
                }
            }
        };

        fromSearchDate.addKeyPressHandler(keyPressHandler);
        toSearchDate.addKeyPressHandler(keyPressHandler);
        filter.addKeyPressHandler(keyPressHandler);

        this.driver = driver;
        this.driver.initialize(this);
    }

    @UiHandler("search")
    void onSearchButtonClicked(ClickEvent event) {
        if (fromSearchDate.getValue() == null || toSearchDate.getValue() == null || auditFieldList.getValue().isEmpty()) {
            StringBuffer errorStr = new StringBuffer();
            boolean sepFlag = false;
            if (fromSearchDate.getValue() == null) {
                errorStr.append("\"От даты\"");
                sepFlag = true;
            }
            if (toSearchDate.getValue() == null) {
                if (sepFlag) errorStr.append(", ");
                errorStr.append("\"До даты\"");
                sepFlag = true;
            }
            if (auditFieldList.getValue().isEmpty()) {
                if (sepFlag) errorStr.append(", ");
                errorStr.append("\"Искать по полям\"");
            }
            Dialog.errorMessage("Не заполнены обязательные поля", "Для выполнения поиска должно быть заполнено поле " + errorStr.toString());
            return;
        }

        if (fromSearchDate.getValue().compareTo(toSearchDate.getValue()) > 0) {
            Dialog.errorMessage("Операция \"Получение списка журнала аудита\" не выполнена. Дата \"От\" должна быть меньше или равна дате \"До\"");
            return;
        }

        if (getUiHandlers() != null)
            getUiHandlers().onSearchButtonClicked();
    }
}
