package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Информация об исполнителе налоговой формы.
 * У каждой налоговой формы может быть только один исполнитель.
 * @author dsultanbekov
 */
public class FormDataPerformer implements Serializable {
	private static final long serialVersionUID = 1L;
	private long formDataId;
	private String name;
	private String phone;
	
	/**
	 * Получить идентфикатор налоговой формы
	 * @return идентфикатор налоговой формы
	 */
	public long getFormDataId() {
		return formDataId;
	}
	
	/**
	 * Задать идентификатор налоговой формы
	 * @param formDataId идентификатор налоговой формы
	 */
	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}
	
	/**
	 * Получить ФИО исполнителя
	 * @return ФИО исполнителя
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать ФИО исполнителя
	 * @param name ФИО исполнителя
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Получить телефон исполнителя
	 * @return телефон исполнителя
	 */
	public String getPhone() {
		return phone;
	}
	
	/**
	 * Задат телефон исполнителя
	 * @param phone телефон исполнителя
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
}
