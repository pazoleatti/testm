package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Абстрактный класс, представляющий объект, сохраняемый в БД и имеющий идентификатор
 * Этот класс должен являться базовым для классов, на которые предполагается накладывать блокировки
 * @author dsultanbekov
 */
public abstract class IdentityObject<IdType extends Number> implements Serializable {
	private static final long serialVersionUID = 3614498773660756556L;
	
	protected IdType id;	
	
	/**
	 * Задать идентификатор записи
	 * У новых объектов, которые еще не сохранялись в БД равен null
	 * @param id идентификатор записи. 
	 */
	public void setId(IdType id) {
		this.id = id;
	}

	/**
	 * Получить идентификатор записи
	 * У новых объектов, которые еще не сохранялись в БД равен null
	 * @return идентфиикатор записи
	 */
	public IdType getId() {
		return id;
	}
}
