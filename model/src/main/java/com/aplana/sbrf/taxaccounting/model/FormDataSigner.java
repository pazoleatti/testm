package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Информация о подписанте налогвой формы.
 * У налоговой формы может быть несколько подписантов
 * @author dsultanbekov
 */
public class FormDataSigner implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	private long formDataId;
	private int order;
	private String name;
	private String position;
	
	/**
	 * Получить идентификатор записи
	 * @return идентификатор записи
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Задать идентификатор записи
	 * @param id идентификатор записи
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Получить идентификатор налоговой формы, к которой относится подписант
	 * @return идентификатор налоговой формы
	 */
	public long getFormDataId() {
		return formDataId;
	}
	
	/**
	 * Задать идентификатор налоговой формы, к которой относится подписант
	 * @param formDataId идентификатор налоговой формы
	 */
	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}
	
	/**
	 * Получить порядковый номер подписанта в налоговой форме
	 * @return порядковый номер подписанта в налоговой форме
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * Задать порядковый номер подписанта в налоговой форме
	 * @param order порядковый номер подписанта в налоговой форме
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	
	/**
	 * Получить ФИО подписанта
	 * @return ФИО подписанта
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать ФИО подписанта
	 * @param name ФИО подписанта
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Получить должность подписанта
	 * @return должность подписанта
	 */
	public String getPosition() {
		return position;
	}
	
	/**
	 * Задать должность подписанта
	 * @param position должность подписанта
	 */
	public void setPosition(String position) {
		this.position = position;
	}
}
