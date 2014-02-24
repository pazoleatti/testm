package com.aplana.sbrf.taxaccounting.model.refbook;

import java.util.Date;

/**
 * Типы атрибутов справочника. <br /><br />
 * <i>Примечание: </i>При рефакторинге данного класса порядок следования элементов менять нельзя.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 17:12
 */
public enum RefBookAttributeType {

	/** Строка */
	STRING(String.class),
	/** Число */
	NUMBER(Number.class),
	/** Дата-время */
	DATE(Date.class),
	/** Ссылка на элемент справочника */
	REFERENCE(Long.class);

	private Class typeClass;

	RefBookAttributeType(Class typeClass) {
		this.typeClass = typeClass;
	}

	public Class getTypeClass() {
		return typeClass;
	}
}