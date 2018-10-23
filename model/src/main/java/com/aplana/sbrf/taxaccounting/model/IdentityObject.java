package com.aplana.sbrf.taxaccounting.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Абстрактный класс, представляющий объект, сохраняемый в БД и имеющий идентификатор
 * Этот класс должен являться базовым для классов, на которые предполагается накладывать блокировки
 * @author dsultanbekov
 */
@Data
public abstract class IdentityObject<IdType extends Number> implements Serializable {
	private static final long serialVersionUID = 3614498773660756556L;
	
	protected IdType id;	

}
