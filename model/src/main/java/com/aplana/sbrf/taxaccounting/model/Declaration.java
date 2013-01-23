package com.aplana.sbrf.taxaccounting.model;

/**
 * Налоговая декларация
 * @author dsultanbekov
 */
public class Declaration extends IdentityObject<Long> {
	private static final long serialVersionUID = 1L;

	private int declarationTemplateId;	
	private int reportPeriodId;
	private int departmentId;
	private String xml;

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
	 * Получить представление декларации в формате законодателя
	 * @return данные декларации в виде XML-строки
	 */
	public String getXml() {
		return xml;
	}
	
	/**
	 * Задать представление декларации в формате законодателя
	 * @param xml данные декларации в виде XML-строки
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}
}
