package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Налоговая декларация.
 * 
 * Обращаю внимание, что сами данные декларации (XML-файл в формате законодателя) в модели не содержатся
 * Работа с ними должна вестись через отдельные методы на dao и сервисном слое
 * @author dsultanbekov
 */
public class DeclarationData extends IdentityObject<Long> implements SecuredEntity {
	private static final long serialVersionUID = 1L;

	private int declarationTemplateId;	
	private int reportPeriodId;
	private int departmentId;
    private Integer departmentReportPeriodId;

    private String taxOrganCode;
    private String kpp;
	private String oktmo;

    /**
     * Идентификатор АСНУ
     */
    private Long asnuId;

    /**
     * Комментарий к НФ
     */
    private String note;

    /**
     * Имя файла
     */
    private String fileName;

    /**
     * Статус налоговой формы
     */
    private State state;

    /**
     * Статус ЭД
     */
    private Long docState;

    /**
     * Права
     */
    private long permissions;

    /**
     * Создана в ручную
     */
    private Boolean manuallyCreated = false;

    /**
     * Дата последних изменений данных формы
     */
    private Date lastDataModifiedDate;

    /**
     * Признак, показывающий необходимость корректировки отрицательных значений
     */
    private boolean isAdjustNegativeValues;

    /**
	 * Получить идентификатор {@link DeclarationTemplate шаблона декларации}, по которому создана данная декларация
	 * @return идентификатор шаблона декларации
	 */
	public int getDeclarationTemplateId() {
		return declarationTemplateId;
	}

	/**
	 * Задать идентификатор {@link DeclarationTemplate шаблона декларации}, по которому создана данная декларация
	 * @param declarationTemplateId идентификатор шаблона декларации
	 */	
	public void setDeclarationTemplateId(int declarationTemplateId) {
		this.declarationTemplateId = declarationTemplateId;
	}
	
	/**
	 * Получить идентификатор отчётного периода
	 * @return идентификатор отчётного периода
	 */
	public int getReportPeriodId() {
		return reportPeriodId;
	}
	
	/**
	 * Задать идентификатор отчётного периода
	 * @param reportPeriodId идентификатор отчётного периода
	 */
	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}
	
	/**
	 * Получить идентификатор подразделения банка
	 * @return идентификатор подразделения банка
	 */
	public int getDepartmentId() {
		return departmentId;
	}
	
	/**
	 * Задать иденфтикатор подразделения банка
	 * @param departmentId идентфикатор подразделения банка
	 */
	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

    public Integer getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(Integer departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    /**
     * КПП
     */
    public String getKpp() {
        return kpp;
    }

    /**
     * КПП
     */
    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    /**
     * Налоговый орган
     */
    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    /**
     * Налоговый орган
     */
    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

	public String getOktmo() {
		return oktmo;
	}

	public void setOktmo(String oktmo) {
		this.oktmo = oktmo;
	}

    public Long getDocState() {
        return docState;
    }

    public void setDocState(Long docState) {
        this.docState = docState;
    }

    public long getPermissions() { return permissions; }
    public void setPermissions(long permissions) { this.permissions = permissions; }

    public Boolean getManuallyCreated() {
        return manuallyCreated;
    }

    public void setManuallyCreated(Boolean manuallyCreated) {
        this.manuallyCreated = manuallyCreated;
    }

    public Date getLastDataModifiedDate() {
        return lastDataModifiedDate;
    }

    public void setLastDataModifiedDate(Date lastDataModifiedDate) {
        this.lastDataModifiedDate = lastDataModifiedDate;
    }

    public boolean isAdjustNegativeValues() {
        return isAdjustNegativeValues;
    }

    public void setAdjustNegativeValues(boolean adjustNegativeValues) {
        isAdjustNegativeValues = adjustNegativeValues;
    }

    @Override
    public String toString() {
        return "DeclarationData{" +
                "declarationTemplateId=" + declarationTemplateId +
                ", reportPeriodId=" + reportPeriodId +
                ", departmentId=" + departmentId +
                ", departmentReportPeriodId=" + departmentReportPeriodId +
                ", taxOrganCode='" + taxOrganCode + '\'' +
                ", kpp='" + kpp + '\'' +
                ", oktmo='" + oktmo + '\'' +
                ", asnuId=" + asnuId +
                ", note='" + note + '\'' +
                ", fileName='" + fileName + '\'' +
                ", state=" + state +
                ", docState=" + docState +
                ", permissions=" + permissions +
                ", manuallyCreated=" + manuallyCreated +
                ", lastDataModifiedDate=" + lastDataModifiedDate +
                '}';
    }
}