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
	private boolean accepted;

    /**
     * Идентификаторы бинарных данных в таблице BLOB_DATA
     */
    private String xmlDataUuid;
    private String pdfDataUuid;
    private String xlsxDataUuid;

    public String getXmlDataUuid() {
        return xmlDataUuid;
    }

    public void setXmlDataUuid(String xmlDataUuid) {
        this.xmlDataUuid = xmlDataUuid;
    }

    public String getPdfDataUuid() {
        return pdfDataUuid;
    }

    public void setPdfDataUuid(String pdfDataUuid) {
        this.pdfDataUuid = pdfDataUuid;
    }

    public String getXlsxDataUuid() {
        return xlsxDataUuid;
    }

    public void setXlsxDataUuid(String xlsxDataUuid) {
        this.xlsxDataUuid = xlsxDataUuid;
    }

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
}
