package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.client;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;

public interface PdfViewerView {
	
	void setPages(Pdf pdf);

    void setPage(int page);

    void setVisible(boolean visible);
}
