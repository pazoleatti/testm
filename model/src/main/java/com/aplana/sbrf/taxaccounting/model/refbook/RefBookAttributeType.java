package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Типы атрибутов справочника. <br /><br />
 * <i>Примечание: </i>При рефакторинге данного класса порядок следования элементов менять нельзя.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 17:12
 */
public enum RefBookAttributeType {

	/** Строка */
	STRING,
	/** Число */
	NUMBER,
	/** Дата-время */
	DATE,
	/** Ссылка на элемент справочника */
	REFERENCE;

}