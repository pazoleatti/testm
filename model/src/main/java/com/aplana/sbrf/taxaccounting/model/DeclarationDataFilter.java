package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DeclarationDataFilter implements Serializable {

	private static final long serialVersionUID = -4400641153082281834L;

	private TaxType taxType;

	private List<Integer> reportPeriodIds;

	private List<Integer> departmentIds;

	private List<Long> declarationTypeIds;

    private State formState;

    private Boolean correctionTag;

    private Date correctionDate;

    /*Стартовый индекс списка записей */
	private int startIndex;

	/*Количество записей, которые нужно вернуть*/
	private int countOfRecords;

    private Long declarationDataId;

    private DeclarationDataSearchOrdering searchOrdering;

    private String taxOrganCode;

    private String taxOrganKpp;

    private Long asnuId;

    private List<Long> formKindIds;

    private String fileName;

    /*true, если сортируем по возрастанию, false - по убыванию*/
	private boolean ascSorting;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public List<Integer> getReportPeriodIds() {
		return reportPeriodIds;
	}

	public void setReportPeriodIds(List<Integer> reportPeriodIds) {
		this.reportPeriodIds = reportPeriodIds;
	}

	public List<Integer> getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(List<Integer> departmentIds) {
		this.departmentIds = departmentIds;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getCountOfRecords() {
		return countOfRecords;
	}

	public void setCountOfRecords(int countOfRecords) {
		this.countOfRecords = countOfRecords;
	}

	public DeclarationDataSearchOrdering getSearchOrdering() {
		return searchOrdering;
	}

	public void setSearchOrdering(DeclarationDataSearchOrdering searchOrdering) {
		this.searchOrdering = searchOrdering;
	}

	public boolean isAscSorting() {
		return ascSorting;
	}

	public void setAscSorting(boolean ascSorting) {
		this.ascSorting = ascSorting;
	}

    public List<Long> getDeclarationTypeIds() {
        return declarationTypeIds;
    }

    public void setDeclarationTypeIds(List<Long> declarationTypeIds) {
        this.declarationTypeIds = declarationTypeIds;
    }

    public State getFormState() {
        return formState;
    }

    public void setFormState(State formState) {
        this.formState = formState;
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public String getTaxOrganKpp() {
        return taxOrganKpp;
    }

    public void setTaxOrganKpp(String taxOrganKpp) {
        this.taxOrganKpp = taxOrganKpp;
    }

    public Boolean getCorrectionTag() {
        return correctionTag;
    }

    public void setCorrectionTag(Boolean correctionTag) {
        this.correctionTag = correctionTag;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    /**
     * Устанавливает дату корректировки. Действительно только при установленом {@link #correctionTag}
     * @param correctionDate Дата корректировки
     */
    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public List<Long> getFormKindIds() {
        return formKindIds;
    }

    public void setFormKindIds(List<Long> formKindIds) {
        this.formKindIds = formKindIds;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
