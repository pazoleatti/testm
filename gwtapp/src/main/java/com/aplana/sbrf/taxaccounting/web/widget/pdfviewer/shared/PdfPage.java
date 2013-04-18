package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared;

import java.io.Serializable;

public class PdfPage implements Serializable{
	private static final long serialVersionUID = -6687699610652925750L;

	private String title;
	
	private String src;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PdfPage [title=");
		builder.append(title);
		builder.append(", src=");
		builder.append(src);
		builder.append("]");
		return builder.toString();
	}

}
