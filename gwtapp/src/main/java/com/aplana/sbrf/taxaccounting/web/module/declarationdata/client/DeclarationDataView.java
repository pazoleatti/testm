package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerView;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;

import java.util.Date;


public class DeclarationDataView extends ViewWithUiHandlers<DeclarationDataUiHandlers>
		implements DeclarationDataPresenter.MyView{

	interface Binder extends UiBinder<Widget, DeclarationDataView> { }

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
	Anchor returnAnchor;
	@UiField
	Anchor historyAnchor;

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
	PdfViewerView pdfViewer;
	@UiField
	Panel downloadXml;

	@UiField
	CustomDateBox dateBox;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showAccept(boolean show) {
		if (show) {
			stateLabel.setText("Создана");
		}
		acceptButton.setVisible(show);
	}

	@Override
	public void showReject(boolean show) {
		if (show) {
			stateLabel.setText("Принята");
		}
		cancelButton.setVisible(show);
	}

	@Override
	public void showRefresh(boolean show) {
		dateBox.setVisible(show);
		refreshButton.setVisible(show);
		dateBox.setEnabled(show);
	}

	@Override
	public void showDownloadXml(boolean show) {
		downloadXml.setVisible(show);
	}

	@Override
	public void showDelete(boolean show) {
		deleteButton.setVisible(show);
	}

	@Override
	public void setType(String type) {
		this.type.setText(type);
	}

	@Override
	public void setTitle(String title) {
		this.title.setText(title);
		this.title.setTitle(title);
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
	public void setBackButton(String link){
		returnAnchor.setHref(link);
	}

	@Override
	public void setPdf(Pdf pdf) {
		pdfViewer.setPages(pdf);
	}

	@Override
	public void setDocDate(Date date) {
		dateBox.setValue(date);
	}

	@UiHandler("refreshButton")
	public void onRefresh(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().refreshDeclaration(dateBox.getValue());
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