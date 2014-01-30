package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.gwt.client.Spinner;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateInfoPresenter;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;


public class FormTemplateInfoView extends ViewWithUiHandlers<FormTemplateInfoUiHandlers> implements FormTemplateInfoPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateInfoView> { }

    private static DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.YEAR);

	@UiField
    Spinner versionDateBegin;

    @UiField
    Spinner versionDateEnd;

	@UiField
	CheckBox numberedColumnsBox;

	@UiField
	CheckBox fixedRowsCheckBox;
	
	@UiField
	TextBox nameBox;
	
	@UiField
	TextBox fullnameBox;
	
	@UiField
	TextBox codeBox;

	@Inject
	public FormTemplateInfoView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setViewData(Date version, Date versionEnd, boolean numberedColumns, boolean fixedRows, String name, String fullName, String code) {
        versionDateBegin.setValue(convertFromDate(version));
        versionDateEnd.setValue(convertFromDate(versionEnd));
		numberedColumnsBox.setValue(numberedColumns);
		fixedRowsCheckBox.setValue(fixedRows);
        Window.confirm(name + " " + fullName + " " + code);
		nameBox.setValue(name);
		fullnameBox.setValue(fullName);
		codeBox.setValue(code);
	}

	@Override
	public void onFlush() {
		getUiHandlers().setRangeRelevanceVersion(convertToDate(versionDateBegin.getValue()), convertToDate(versionDateEnd.getValue()));
		getUiHandlers().setNumberedColumns(numberedColumnsBox.getValue());
		getUiHandlers().setFixedRows(fixedRowsCheckBox.getValue());
		getUiHandlers().setName(nameBox.getValue());
		getUiHandlers().setFullname(fullnameBox.getValue());
		getUiHandlers().setCode(codeBox.getValue());
	}

    private Date convertToDate(Integer year){
        if (year == null)
            return null;
        return dateTimeFormat.parse(String.valueOf(year));
    }

    private int convertFromDate(Date date){
        return Integer.parseInt(dateTimeFormat.format(date));
    }

}