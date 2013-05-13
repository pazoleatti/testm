package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.PdfPage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

public class PdfViewerWidget extends Composite implements PdfViewerView {

	private static PdfViewerWidgetUiBinder uiBinder = GWT
			.create(PdfViewerWidgetUiBinder.class);

	interface PdfViewerWidgetUiBinder extends UiBinder<Widget, PdfViewerWidget> {
	}

	Pdf pdf;

	@UiField
	Image pdfPage;

	@UiField
	Tree pages;

	@UiField
	ListBox scale;

	@UiField
	ScrollPanel pdfPanel;

	public static final int DEFAULT_PAGE_WIDTH = 793;
	public static final int DEFAULT_PAGE_HEIGHT = 1123;

	public static final int DEFAULT_SCALE_POSITION = 5;

	public PdfViewerWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		setPages(null);
		pdfPage.setPixelSize(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT);

		for (int i=50; i<=200; i+=10) {
			scale.addItem(String.valueOf(i) + "%");
		}
		scale.setSelectedIndex(DEFAULT_SCALE_POSITION); //Select 100%

	}

	@Override
	public void setPages(Pdf pdf) {
		pages.clear();
		if (pdf != null) {
			this.pdf = pdf;
			TreeItem rootItem = new TreeItem();
			rootItem.setText(pdf.getTitle());
			rootItem.setState(true);
			for (PdfPage page : pdf.getPdfPages()) {
				TreeItem pageItem = new TreeItem();
				pageItem.setText(page.getTitle());
				pageItem.setUserObject(page);
				rootItem.addItem(pageItem);
			}
			pages.addItem(rootItem);
			rootItem.setState(true);
			pages.setSelectedItem(rootItem.getChild(0));
		}
	}

	@UiHandler("scale")
	public void onChange(ChangeEvent event) {
		int scaleValue = Integer.parseInt(scale.getItemText(scale.getSelectedIndex()).substring(0,
				scale.getItemText(scale.getSelectedIndex()).length() - 1));

		pdfPage.setPixelSize((int)(DEFAULT_PAGE_WIDTH *(scaleValue/100.)), (int)(DEFAULT_PAGE_HEIGHT *(scaleValue/100.)));
	}

	@UiHandler("pages")
	public void onSelection(SelectionEvent<TreeItem> event) {
		PdfPage page = (PdfPage)event.getSelectedItem().getUserObject();
		if (page != null) {
			pdfPage.setUrl(page.getSrc());
		}
	}

	@UiHandler("plusButton")
	public void plusButtonClick(ClickEvent event) {
		if (scale.getSelectedIndex() < scale.getItemCount()-1) {
			scale.setItemSelected(scale.getSelectedIndex()+1, true);
			DomEvent.fireNativeEvent(Document.get().createChangeEvent(), scale);
		}
	}

	@UiHandler("minusButton")
	public void minusButtonClick(ClickEvent event) {
		if (scale.getSelectedIndex() > 0) {
			scale.setItemSelected(scale.getSelectedIndex()-1, true);
			DomEvent.fireNativeEvent(Document.get().createChangeEvent(), scale);
		}
	}

	@UiHandler("fitToWidthButton")
	public void fitToWidthButtonClick(ClickEvent event) {
		double mul = (pdfPanel.getOffsetWidth() - pdfPanel.getAbsoluteLeft() - 10)/(double)pdfPage.getWidth();
		if (mul >= 0.9938 && mul <= 1.0062) {
			pdfPage.setPixelSize(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT);
			scale.setSelectedIndex(DEFAULT_SCALE_POSITION);
		}
		pdfPage.setPixelSize((int)(pdfPage.getWidth() * mul), (int)(pdfPage.getHeight() * mul));
	}

}
