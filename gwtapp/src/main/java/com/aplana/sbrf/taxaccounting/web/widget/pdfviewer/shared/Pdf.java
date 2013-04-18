package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared;

import java.io.Serializable;
import java.util.List;

public class Pdf implements Serializable{
	private static final long serialVersionUID = 7870486957237520599L;

	private String title;
	
	private List<PdfPage> pdfPages;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<PdfPage> getPdfPages() {
		return pdfPages;
	}

	public void setPdfPages(List<PdfPage> pdfPages) {
		this.pdfPages = pdfPages;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pdf [title=");
		builder.append(title);
		builder.append(", pdfPages=");
		builder.append(pdfPages);
		builder.append("]");
		return builder.toString();
	}

}
