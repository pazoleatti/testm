package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;


public class DeclarationDataView extends ViewWithUiHandlers<DeclarationDataUiHandlers>
		implements DeclarationDataPresenter.MyView{

	interface Binder extends UiBinder<Widget, DeclarationDataView> { }

	private final Widget widget;

	@UiField
	Button refreshButton;

	@UiField
	Button acceptButton;

	@UiField
	Button cancelButton;

	@UiField
	Anchor downloadExcelButton;

	@UiField
	Anchor downloadXmlButton;

	@UiField
	Button deleteButton;

	@UiField
	Label type;

	@UiField
	Label reportPeriod;

	@UiField
	Label department;

	@UiField
	Label stateLabel;

	@UiField
	Label title;

	@UiField
	Anchor returnAnchor;

	@UiField
	HTMLPanel pdfContent;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setDeclarationData(DeclarationData declaration) {
		if (declaration.isAccepted()) {
			stateLabel.setText("Принята");
			acceptButton.setVisible(false);
			cancelButton.setVisible(true);
		}
		else {
			stateLabel.setText("Создана");
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
	public void setCannotDownloadXml() {
		downloadXmlButton.setVisible(false);
	}

	@Override
	public void setCannotDelete() {
		deleteButton.setVisible(false);
	}

	@Override
	public void setType(String type) {
		this.type.setText(type);
	}

	@Override
	public void setTitle(String title) {
		this.title.setText(title);
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

	@Override
	public void setBackButton(String link){
		returnAnchor.setHref(link);
	}

	@Override
	public void setPdfFile(String fileUrl) {
		Frame pdf = new Frame(fileUrl);
		pdf.setWidth("100%");
		pdf.setHeight("100%");
		pdfContent.add(pdf);
	}


	@UiHandler("refreshButton")
	public void onRefresh(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().refreshDeclaration();
		}
	}

	@UiHandler("acceptButton")
	public void onAccept(ClickEvent event){
		getUiHandlers().setAccepted(true);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().setAccepted(false);
	}

	@UiHandler("deleteButton")
	public void onDelete(ClickEvent event){
		getUiHandlers().delete();
	}

	@UiHandler("downloadExcelButton")
	public void onDownloadExcelButton(ClickEvent event){
		getUiHandlers().downloadExcel();
	}

	@UiHandler("downloadXmlButton")
	public void onDownloadAsLegislatorButton(ClickEvent event){
		getUiHandlers().downloadXml();
	}

}