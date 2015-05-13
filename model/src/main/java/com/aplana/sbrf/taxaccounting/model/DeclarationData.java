package com.aplana.sbrf.taxaccounting.model;

/**
 * Налоговая декларация.
 * 
 * Обращаю внимание, что сами данные декларации (XML-файл в формате законодателя) в модели не содержатся
 * Работа с ними должна вестись через отдельные методы на dao и сервисном слое
 * @author dsultanbekov
 */
public class DeclarationData extends IdentityObject<Long> {
	private static final long serialVersionUID = 1L;

	private int declarationTemplateId;	
	private int reportPeriodId;
	private int departmentId;
    private Integer departmentReportPeriodId;

	private boolean accepted;
    private String taxOrganCode;
    private String kpp;

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
	 * Получить идентификатор отчётного перода
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
	 * Возвращает признак того, что декларация принята
	 * @return true - декларация принята, false - не принята
	 */
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * Задать признак того, что декларация принята
	 * @param accepted true - декларация принята, false - не принята
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
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
}
