package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client;

import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.PdfPage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;

public class PdfViewerWidget extends Composite implements PdfViewerView, HasRows {

	private static PdfViewerWidgetUiBinder uiBinder = GWT
			.create(PdfViewerWidgetUiBinder.class);

    interface PdfViewerWidgetUiBinder extends UiBinder<Widget, PdfViewerWidget> {
	}

	private Pdf pdf;

    private int page = 0;

	@UiField
	Image pdfPage;

    @UiField
    FlexiblePager pager;

	@UiField
	ListBox scale;

	@UiField
	ScrollPanel pdfPanel;

	public static final int DEFAULT_PAGE_WIDTH = 793;
	public static final int DEFAULT_PAGE_HEIGHT = 1123;

	public static final int DEFAULT_SCALE_POSITION = 5;

	public PdfViewerWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		pdfPage.setPixelSize(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT);

		for (int i=50; i<=200; i+=10) {
			scale.addItem(i + "%");
		}

		scale.setSelectedIndex(DEFAULT_SCALE_POSITION); //Select 100%
	}

    private void toPage(int pageNum) {
        if (pdf != null && pdf.getPdfPages() != null && !pdf.getPdfPages().isEmpty()) {
            if (pageNum >= pdf.getPdfPages().size() || pageNum < 0) {
                return;
            }
            PdfPage page = pdf.getPdfPages().get(pageNum);
            if (page != null) {
                pdfPage.setUrl(page.getSrc());
            }
            this.page = pageNum;
            pager.setPageNumber(pageNum + 1);
        }
    }

	@Override
	public void setPages(Pdf pdf) {
        this.pdf = pdf;
        toPage(0);
        pager.setDisplay(this);
        pager.setPageSize(1);
	}

    @Override
    public void setPage(int page) {
        toPage(page);
    }

    @UiHandler("scale")
	public void onChange(ChangeEvent event) {
		int scaleValue = Integer.parseInt(scale.getItemText(scale.getSelectedIndex()).substring(0,
				scale.getItemText(scale.getSelectedIndex()).length() - 1));

		pdfPage.setPixelSize((int)(DEFAULT_PAGE_WIDTH *(scaleValue/100.)), (int)(DEFAULT_PAGE_HEIGHT *(scaleValue/100.)));
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
		double mul = (pdfPanel.getOffsetWidth() - pdfPanel.getAbsoluteLeft() - 15)/(double)pdfPage.getWidth();
		if (mul >= 0.9938 && mul <= 1.0062) {
			pdfPage.setPixelSize(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT);
			scale.setSelectedIndex(DEFAULT_SCALE_POSITION);
		}
		pdfPage.setPixelSize((int)(pdfPage.getWidth() * mul), (int)(pdfPage.getHeight() * mul));
	}

    @Override
    public void setRowCount(int count) {}

    @Override
    public void setRowCount(int count, boolean isExact) {}

    @Override
    public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler) {
        return null;
    }

    @Override
    public int getRowCount() {
        if (pdf == null || pdf.getPdfPages() == null) {
            return 0;
        }
        return pdf.getPdfPages().size();
    }

    @Override
    public Range getVisibleRange() {
        return new Range(page, 1);
    }

    @Override
    public boolean isRowCountExact() {
        return true;
    }

    @Override
    public void setVisibleRange(int start, int length) {
        toPage(start);
    }

    @Override
    public void setVisibleRange(Range range) {
        toPage(range.getStart());
    }
}
