package com.aplana.sbrf.taxaccounting.model;

/**
 * Шаблон декларации
 * @author dsultanbekov
 */
public class DeclarationTemplate extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;
	
	private TaxType taxType;
	private String version;
	private boolean active;
	private String createScript;	
	private byte[] jrxml;

	/**
	 * Получить вид налога
	 * @return вид налога
	 */
	public TaxType getTaxType() {
		return taxType;
	}

	/**
	 * Задать вид налога
	 * @param taxType вид налога
	 */	
	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	/**
	 * Получить название версии шаблона
	 * @return версия шаблона
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Задать версию шаблона
	 * @param version версия шаблона
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * Признак активности шаблона декларации
	 * @return true - шаблон активен, false - шаблон неактивен
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Задать признак активности декларации
	 * @param active true - шаблон активен, false - шаблон неактивен
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Получить скрипт (groovy), использующийся для формирования декларации
	 * @return тело скрипта на groovy
	 */
	public String getCreateScript() {
		return createScript;
	}

	/**
	 * Задать скрипт, использующийся для формирования декларации
	 * @param createScript тело скрипта на groovy
	 */
	public void setCreateScript(String createScript) {
		this.createScript = createScript;
	}

	/**
	 * Получить описание Jrxml-шаблона для формирования декларации
	 * @return jrxml-файл в виде байтового массива
	 */
	public byte[] getJrxml() {
		return jrxml;
	}

	/**
	 * Задать описание Jrxml-шаблона для формирования декларации
	 * @param jrxml jrxml-файл в виде байтового массива
	 */
	public void setJrxml(byte[] jrxml) {
		this.jrxml = jrxml;
	}
}
