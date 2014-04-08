package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerView;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;

import java.util.Date;


public class DeclarationDataView extends ViewWithUiHandlers<DeclarationDataUiHandlers>
		implements DeclarationDataPresenter.MyView{

	interface Binder extends UiBinder<Widget, DeclarationDataView> { }

    public static final String DATE_BOX_TITLE = "Дата формирования декларации";
    public static final String DATE_BOX_TITLE_D = "Дата формирования уведомления";

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
	Button checkButton;
	@UiField
	Anchor returnAnchor;
	@UiField
    LinkButton infoAnchor;

    @UiField
    Label typeLabel;

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
    Label dateBoxLabel;

	@UiField
    DateMaskBoxPicker dateBox;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showAccept(boolean show) {
		if (show) {
			stateLabel.setText(!getUiHandlers().getTaxType().equals(TaxType.DEAL) ? "Создана" : "Создано");
		}
		acceptButton.setVisible(show);
	}

	@Override
	public void showReject(boolean show) {
		if (show) {
			stateLabel.setText(!getUiHandlers().getTaxType().equals(TaxType.DEAL) ? "Принята" : "Принято");
		}
		cancelButton.setVisible(show);
	}

	@Override
	public void showRefresh(boolean show) {
		//dateBox.setVisible(show);
		refreshButton.setVisible(show);
		dateBox.setEnabled(show);
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
	public void setTitle(String title, boolean isTaxTypeDeal) {
		this.title.setText(title);
		this.title.setTitle(title);
        type.setVisible(!isTaxTypeDeal);
        typeLabel.setVisible(!isTaxTypeDeal);
        if (!isTaxTypeDeal) {
            dateBoxLabel.setText(DATE_BOX_TITLE + ":");
            dateBoxLabel.setTitle(DATE_BOX_TITLE);
        } else {
            dateBoxLabel.setText(DATE_BOX_TITLE_D + ":");
            dateBoxLabel.setTitle(DATE_BOX_TITLE_D);
        }
	}

	@Override
	public void setDepartment(String department) {
		this.department.setText(department);
		this.department.setTitle(department);
	}

	@Override
	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod.setText(reportPeriod);
	}

    @Override
    public void setBackButton(String link, String text) {
        returnAnchor.setHref(link);
        returnAnchor.setText(text);
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

	@UiHandler("checkButton")
	public void onCheck(ClickEvent event){
		getUiHandlers().check();
	}

	@UiHandler("downloadExcelButton")
	public void onDownloadExcelButton(ClickEvent event){
		getUiHandlers().downloadExcel();
	}

	@UiHandler("downloadXmlButton")
	public void onDownloadAsLegislatorButton(ClickEvent event){
		getUiHandlers().downloadXml();
	}

	@UiHandler("infoAnchor")
	void onInfoButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onInfoClicked();
		}
	}
}