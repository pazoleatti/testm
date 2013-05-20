package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;

public class GetDeclarationDataResult implements Result {
	private TaxType taxType;
	private String department;
	private String reportPeriod;
	private String declarationType;
	private Date docDate;

	private boolean canAccept;
	private boolean canReject;
	private boolean canDownload;
	private boolean canDelete;
	
	/**
	 * Данные для инициализации компонента PDFView
	 */
	private Pdf pdf;

	public boolean isCanAccept() {
		return canAccept;
	}

	public void setCanAccept(boolean canAccept) {
		this.canAccept = canAccept;
	}

	public boolean isCanReject() {
		return canReject;
	}

	public void setCanReject(boolean canReject) {
		this.canReject = canReject;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	public String getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(String declarationType) {
		this.declarationType = declarationType;
	}

	public boolean isCanDownload() {
		return canDownload;
	}

	public void setCanDownload(boolean canDownload) {
		this.canDownload = canDownload;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public Pdf getPdf() {
		return pdf;
	}

	public void setPdf(Pdf pdf) {
		this.pdf = pdf;
	}

	public Date getDocDate() {
		return docDate;
	}

	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}
}
