package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client.PdfViewerView;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.datePicker.DatePickerWithYearSelector;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
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

	private final PopupPanel datePickerPanel = new PopupPanel(true, true);
	private final DatePickerWithYearSelector datePicker = new DatePickerWithYearSelector();
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
	Anchor returnAnchor;

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
	TextBox dateBox;
	@UiField
	Image dateImage;

	@Inject
	@UiConstructor
	public DeclarationDataView(final Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);

		datePickerPanel.setWidth("200");
		datePickerPanel.setHeight("200");
		datePickerPanel.add(datePicker);
		addDatePickerHandlers();
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
		dateImage.setVisible(show);
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
	public Widget asWidget() {
		return widget;
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
	public void setDocDate(String date) {
		dateBox.setValue(date);
	}

	@Override
	public void showPdfFile(boolean show) {
	}

	@Override
	public void clearPdfFile() {
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

	@UiHandler("dateImage")
	public void onDateImage(ClickEvent event){
		datePickerPanel.setPopupPosition(event.getClientX(), event.getClientY() + 10);
		datePickerPanel.show();
	}

	private void addDatePickerHandlers() {
		datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				dateBox.setValue(getFormattedDate(event.getValue()));
				datePickerPanel.hide();
			}
		});
	}

	private String getFormattedDate(Date date){
		final String DATE_SHORT_START = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)
				.format(date);

		int startDayIndex = DATE_SHORT_START.lastIndexOf('-');
		int startMonthIndex = DATE_SHORT_START.indexOf('-');

		String startDate =  DATE_SHORT_START.substring(startDayIndex + 1, DATE_SHORT_START.length()) + '.' +
				DATE_SHORT_START.substring(startMonthIndex + 1, startDayIndex) + '.' +
				DATE_SHORT_START.substring(0, startMonthIndex);

		return startDate;
	}
}