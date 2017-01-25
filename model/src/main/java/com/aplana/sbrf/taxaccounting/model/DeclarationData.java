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

    private String taxOrganCode;
    private String kpp;
    /**
     * Идентификатор АСНУ
     */
    private Long asnuId;
    /**
     * Имя файла
     */
    private String fileName;

    /**
     * Статус налоговой формы
     */
    private State state;

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
}
