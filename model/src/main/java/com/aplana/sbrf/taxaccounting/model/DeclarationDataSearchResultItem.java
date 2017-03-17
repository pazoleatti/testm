package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO-Класс, содержащий информацию о параметрах декларации и связанных с ним объектов в "плоском" виде
 * Используется для того, чтобы отображать результаты поисковых запросов по декларациям в таблицах, без необходимости
 * запрашивать из БД сведения по связанным объектам (название подразделения, вид налога и т.п.)
 */
public class DeclarationDataSearchResultItem implements Serializable {

	private static final long serialVersionUID = -43124124124124L;

	public DeclarationDataSearchResultItem() {
	}

	// Идентификатор записи с данными декларации
	private Long declarationDataId;
	// Идентификатор шаблона декларации
	private Integer declarationTemplateId;
	// Статус налоговой формы
	private State state;
	// Вид налога
	private TaxType taxType;
	// Идентификатор подразделения
	private Integer departmentId;
	// Название подразделения
	private String departmentName;
	// Тип подразделения
	private DepartmentType departmentType;
	// Идентификатор отчётного периода
	private Integer reportPeriodId;
	// Название отчётного периода
	private String reportPeriodName;
    // Срок сдачи корректировки
    private Date correctionDate;
    // Год отчетного периода
    private Integer reportPeriodYear;
	// Идентификатор вида декларации
	private String declarationType;
    // Налоговый орган
    private String taxOrganCode;
    // КПП
    private String taxOrganKpp;
    /**
     * Идентификатор АСНУ
     */
    private Long asnuId;

    /**
     * Дата формирования файла
     */
    private Date createDate;

    /**
     * Файл
     */
    private String fileName;
    /**
     * Тип формы
     */
    private DeclarationFormKind declarationFormKind;

    /**
     * Примечание
     */
    private String oktmo;

    /**
     * Статус документа
     */
    private Long docStateId;
    /**
     * Статус документа
     */
    private String docState;

    /**
     * Примечание
     */
    private String note;

    /**
     * Дата и время создания формы
     */
    private Date declarationDataCreationDate;

    /**
     * Имя пользователя, загрузившего ТФ
     */
    private String importDeclarationDataUserName;

	public Long getDeclarationDataId() {
		return declarationDataId;
	}

	public void setDeclarationDataId(Long declarationDataId) {
		this.declarationDataId = declarationDataId;
	}

	public Integer getDeclarationTemplateId() {
		return declarationTemplateId;
	}

	public void setDeclarationTemplateId(Integer declarationTemplateId) {
		this.declarationTemplateId = declarationTemplateId;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public DepartmentType getDepartmentType() {
		return departmentType;
	}

	public void setDepartmentType(DepartmentType departmentType) {
		this.departmentType = departmentType;
	}

	public Integer getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public String getReportPeriodName() {
		return reportPeriodName;
	}

	public void setReportPeriodName(String reportPeriodName) {
		this.reportPeriodName = reportPeriodName;
	}

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(String declarationType) {
		this.declarationType = declarationType;
	}

    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }

    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
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

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DeclarationFormKind getDeclarationFormKind() {
        return declarationFormKind;
    }

    public void setDeclarationFormKind(DeclarationFormKind declarationFormKind) {
        this.declarationFormKind = declarationFormKind;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public Long getDocStateId() {
        return docStateId;
    }

    public void setDocStateId(Long docStateId) {
        this.docStateId = docStateId;
    }

    public String getDocState() {
        return docState;
    }

    public void setDocState(String docState) {
        this.docState = docState;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDeclarationDataCreationDate() {
        return declarationDataCreationDate;
    }

    public void setDeclarationDataCreationDate(Date declarationDataCreationDate) {
        this.declarationDataCreationDate = declarationDataCreationDate;
    }

    public String getImportDeclarationDataUserName() {
        return importDeclarationDataUserName;
    }

    public void setImportDeclarationDataUserName(String importDeclarationDataUserName) {
        this.importDeclarationDataUserName = importDeclarationDataUserName;
    }
}
