package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.List;

public class GetDeclarationDataResult implements Result {
	private TaxType taxType;
	private String department;
	private String reportPeriod;
    private Date correctionDate;
    private Integer reportPeriodYear;
	private String declarationType;
	private Date docDate;
    private String stateEDName;
    private String asnuName;
    private String fileName;
    private String uuid;
    private List<DeclarationSubreport> subreports;
    private boolean isVisiblePDF;
    private DeclarationData declarationData;
    private DeclarationFormKind declarationFormKind;
    private String creationUserName;
    private String creationDate;
    /**
     * Тип формы, пока не используем
     */
    private String declarationFormType;

	private boolean canAccept;
	private boolean canReject;
    private boolean canDelete;
    private boolean canRecalculate;
    private boolean canChangeStatusED;

    public boolean isCanRecalculate() {
        return canRecalculate;
    }

    public void setCanRecalculate(boolean canRecalculate) {
        this.canRecalculate = canRecalculate;
    }

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

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public Date getDocDate() {
		return docDate;
	}

	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}

    public boolean isCanChangeStatusED() {
        return canChangeStatusED;
    }

    public void setCanChangeStatusED(boolean canChangeStatusED) {
        this.canChangeStatusED = canChangeStatusED;
    }

    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }

    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
    }

    public String getStateEDName() {
        return stateEDName;
    }

    public void setStateEDName(String stateEDName) {
        this.stateEDName = stateEDName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAsnuName() {
        return asnuName;
    }

    public void setAsnuName(String asnuName) {
        this.asnuName = asnuName;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    public List<DeclarationSubreport> getSubreports() {
        return subreports;
    }

    public void setSubreports(List<DeclarationSubreport> subreports) {
        this.subreports = subreports;
    }

    public boolean isVisiblePDF() {
        return isVisiblePDF;
    }

    public void setVisiblePDF(boolean isVisiblePDF) {
        this.isVisiblePDF = isVisiblePDF;
    }

    public DeclarationData getDeclarationData() {
        return declarationData;
    }

    public void setDeclarationData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }

    public String getDeclarationFormType() {
        return declarationFormType;
    }

    public void setDeclarationFormType(String declarationFormType) {
        this.declarationFormType = declarationFormType;
    }

    public DeclarationFormKind getDeclarationFormKind() {
        return declarationFormKind;
    }

    public void setDeclarationFormKind(DeclarationFormKind declarationFormKind) {
        this.declarationFormKind = declarationFormKind;
    }

    public String getCreationUserName() {
        return creationUserName;
    }

    public void setCreationUserName(String creationUserName) {
        this.creationUserName = creationUserName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
