package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

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
	SimplePanel pdfPanel;

	@UiField
	Panel downloadXml;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@Override
	public void setShowAccept(boolean show) {
		if (show) {
			stateLabel.setText("Создана");
		}
		acceptButton.setVisible(show);
	}

	@Override
	public void setShowReject(boolean show) {
		if (show) {
			stateLabel.setText("Принята");
		}
		cancelButton.setVisible(show);
	}

	@Override
	public void setShowRefresh(boolean show) {
		refreshButton.setVisible(show);
	}

	@Override
	public void setShowDownloadXml(boolean show) {
		downloadXml.setVisible(show);
	}

	@Override
	public void setShowDelete(boolean show) {
		deleteButton.setVisible(show);
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
		clearPdfFile();
		Frame pdfContent = new Frame();
		pdfContent.setWidth("100%");
		pdfContent.setHeight("100%");
		pdfContent.setUrl(fileUrl);
		pdfPanel.add(pdfContent);
	}

	@Override
	public void setVisiblePdfFile(boolean visible) {
		if (pdfPanel != null) {
			pdfPanel.setVisible(visible);
		}
	}

	@Override
	public void clearPdfFile() {
		pdfPanel.clear();
	}


	@UiHandler("refreshButton")
	public void onRefresh(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().refreshDeclaration();
		}
	}

	@UiHandler("acceptButton")
	public void onAccept(ClickEvent event){
		getUiHandlers().accept(true);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		getUiHandlers().accept(false);
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