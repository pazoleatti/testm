package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.gwt.client.mask.ui.YearMaskBox;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateInfoPresenter;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Arrays;
import java.util.Date;


public class FormTemplateInfoView extends ViewWithUiHandlers<FormTemplateInfoUiHandlers> implements FormTemplateInfoPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateInfoView> { }

    @UiField
    YearMaskBox versionDateBegin;

    @UiField
    YearMaskBox versionDateEnd;

	@UiField
	CheckBox fixedRowsCheckBox;

	@UiField
	CheckBox monthlyCheckBox;

    @UiField
    CheckBox comparativeCheckBox;

    @UiField
    CheckBox accruingCheckBox;

	@UiField
	TextBox nameBox;
	
	@UiField
	TextBox fullnameBox;

	@UiField
	TextBox headerBox;

	@Inject
	public FormTemplateInfoView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setViewData(TaxType taxType, Date version, Date versionEnd, boolean fixedRows, boolean monthlyForm, boolean comparative, boolean accruing, String name, String fullName, String header) {
        versionDateBegin.setValue(version);
        versionDateEnd.setValue(versionEnd);
		fixedRowsCheckBox.setValue(fixedRows);
		monthlyCheckBox.setValue(monthlyForm);
        comparativeCheckBox.setValue(comparative);
        accruingCheckBox.setValue(accruing);
		nameBox.setValue(name);
		fullnameBox.setValue(fullName);
        headerBox.setValue(header);
        if (Arrays.asList(TaxType.INCOME, TaxType.VAT, TaxType.TRANSPORT, TaxType.PROPERTY).contains(taxType)) {
            monthlyCheckBox.setEnabled(true);
        } else {
            monthlyCheckBox.setEnabled(false);
        }
        if (Arrays.asList(TaxType.ETR).contains(taxType)) {
            comparativeCheckBox.setEnabled(true);
            accruingCheckBox.setEnabled(true);
        } else {
            comparativeCheckBox.setEnabled(false);
            accruingCheckBox.setEnabled(false);
        }
	}

	@Override
	public void onFlush() {
		getUiHandlers().setRangeRelevanceVersion(versionDateBegin.getValue(), versionDateEnd.getValue());
		getUiHandlers().setFixedRows(fixedRowsCheckBox.getValue());
		getUiHandlers().setMonthlyForm(monthlyCheckBox.getValue());
        getUiHandlers().setComparative(comparativeCheckBox.getValue());
        getUiHandlers().setAccruing(accruingCheckBox.getValue());
        getUiHandlers().setName(nameBox.getValue());
		getUiHandlers().setFullname(fullnameBox.getValue());
		getUiHandlers().setHeader(headerBox.getValue());
	}

}