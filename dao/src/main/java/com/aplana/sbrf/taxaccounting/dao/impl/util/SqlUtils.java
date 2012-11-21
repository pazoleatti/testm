package com.aplana.sbrf.taxaccounting.dao.impl.util;

import java.util.List;

/**
 * Вспомогательные методы для работы с SQL в DAO
 * @author srybakov
 */
public class SqlUtils {
	/**
	 * Функция преобразует список (например, содержащий элементы 1,2,3,4) в строку вида "(1,2,3,4)", которая бдует
	 * использоваться в SQL запросах вида "...where param in (1,2,3,4)";
	 */
	public static String transformToSqlInStatement(List list){
		StringBuffer stringBuffer = new StringBuffer(list.toString());
		return "(" + (stringBuffer.substring(1, stringBuffer.length() - 1)) + ")";
	}
}
