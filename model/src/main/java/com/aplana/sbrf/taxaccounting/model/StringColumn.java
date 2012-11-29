package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.model.dictionary.SimpleDictionaryItem;

/**
 * Реализация {@link Column}, предназначенная для представления столбцов со строковыми данными
 * 
 * Для строковых столбцов существует возможность задать справочник, значения из которого могут использоваться для заполнения столбца
 * @author dsultanbekov
 */
public class StringColumn extends Column  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String dictionaryCode;

	/**
	 * Получить код справочника, связанного с данным столбцов.
	 * 
	 * Обращаю внимание, что поскольку значение любой ячейки в налоговой может быть изменено из скриптов,
	 * то использование справочника не гарантирует того, что в столбце могут оказаться только значения, перечисленные в справочнике,
	 * автоматических проверок такой целосостности не производится.
	 * 
	 * В качестве значения, записываемого в столбец используется {@link SimpleDictionaryItem#getValue() значение} элемента справочника, 
	 * а не его {@link SimpleDictionaryItem#getName() описание}. 
	 * 
	 * @see {@link DictionaryManager}
	 * 
	 * @return код справочника, или null, если с данным столбцов не связан справочник
	 */
	public String getDictionaryCode() {
		return dictionaryCode;
	}

	/**
	 * Задать справочник, связанный с данным столбцом
	 * 
	 * @param dictionaryCode код справочника
	 */
	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}
}
