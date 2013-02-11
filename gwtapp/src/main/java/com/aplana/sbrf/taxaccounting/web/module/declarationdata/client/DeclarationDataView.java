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
		implements DeclarationDataPresenter.MyView, Editor<Declaration> {

	interface Binder extends UiBinder<Widget, DeclarationDataView> { }

	interface MyDriver extends SimpleBeanEditorDriver<Declaration, DeclarationDataView> {
	}

	private final Widget widget;
	private final MyDriver driver = GWT.create(MyDriver.class);

	@UiField
	@Ignore
	Label titleLabel;

	@UiField
	@Ignore
	Button acceptButton;

	@UiField
	@Ignore
	Button cancelButton;

	@UiField
	@Ignore
	Button downloadExcelButton;

	@UiField
	@Ignore
	Button downloadAsLegislatorButton;

	@UiField
	@Ignore
	TextBox taxType;

	@UiField
	IntegerBox reportPeriodId;

	@UiField
	IntegerBox departmentId;

	@UiField
	CheckBox accepted;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
		driver.initialize(this);
	}

	@Override
	public void setDeclarationData(Declaration declaration) {
		titleLabel.setText(declaration.getId().toString());
		driver.edit(declaration);

		if (declaration.isAccepted()) {
			acceptButton.setVisible(false);
			cancelButton.setVisible(true);
		}
		else {
			acceptButton.setVisible(true);
			cancelButton.setVisible(false);
		}
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
	public void setTaxType(TaxType taxType) {
		this.taxType.setValue(taxType.getName());
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