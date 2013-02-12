package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class DeclarationDataView extends ViewWithUiHandlers<DeclarationDataUiHandlers>
		implements DeclarationDataPresenter.MyView{

	interface Binder extends UiBinder<Widget, DeclarationDataView> { }

	private final Widget widget;

	@UiField
	Button acceptButton;

	@UiField
	Button cancelButton;

	@UiField
	Button downloadExcelButton;

	@UiField
	Button downloadAsLegislatorButton;

	@UiField
	Label taxType;

	@UiField
	Label reportPeriod;

	@UiField
	Label department;

	@UiField
	CheckBox accepted;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setDeclarationData(Declaration declaration) {
		if (declaration.isAccepted()) {
			acceptButton.setVisible(false);
			cancelButton.setVisible(true);
		}
		else {
			acceptButton.setVisible(true);
			cancelButton.setVisible(false);
		}
		accepted.setValue(declaration.isAccepted());
	}

	@Override
	public void setCannotAccept() {
		acceptButton.setVisible(false);
	}

	@Override
	public void setCannotReject() {
		cancelButton.setVisible(false);
	}

	@Override
	public void setTaxType(String taxType) {
		this.taxType.setText(taxType);
	}

	@Override
	public void setDepartment(String department) {
		this.department.setText(department);
	}

	@Override
	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod.setText(reportPeriod);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiHandler("acceptButton")
	public void onAccept(ClickEvent event){
		getUiHandlers().setAccepted(true);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().setAccepted(false);
	}

	@UiHandler("downloadExcelButton")
	public void onDownloadExcelButton(ClickEvent event){
		getUiHandlers().downloadExcel();
	}

	@UiHandler("downloadAsLegislatorButton")
	public void onDownloadAsLegislatorButton(ClickEvent event){
		getUiHandlers().downloadAsLegislator();
	}

}