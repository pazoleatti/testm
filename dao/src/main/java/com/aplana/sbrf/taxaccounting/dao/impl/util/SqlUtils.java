package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;

import java.util.List;

/**
 * Вспомогательные методы для работы с SQL в DAO
 * 
 * @author srybakov
 */
// TODO (Marat Fayzullin 10.03.2013) оптимизировать бы операции работы со
// строками. Слишком много явной конкатенации. В циклах лишнего добавления ","
// можно избежать
// (Semyon Goryachkin 19.04.2013) а ещё помоему JDBC поддерживает работу со
// списками параметров
// (Marat Fayzullin 29.10.2013) да, поддерживает через, например, batchUpdate()
public final class SqlUtils {

	/**
	 * Запрещаем создавать экземляры класса
	 */
	private SqlUtils() {
	}

	/**
	 * Функция преобразует список (например, содержащий элементы 1,2,3,4) в
	 * строку вида "(1,2,3,4)", которая бдует использоваться в SQL запросах вида
	 * "...where param in (1,2,3,4)";
	 */
	public static String transformToSqlInStatement(List<?> list) {
		StringBuffer stringBuffer = new StringBuffer(list.toString());
		return '(' + (stringBuffer.substring(1, stringBuffer.length() - 1)) + ')';
	}

	public static String transformFormStatesToSqlInStatement(
			List<WorkflowState> source) {
		StringBuffer result = new StringBuffer("");
		for (WorkflowState workflowState : source) {
			result.append(workflowState.getId()).append(',');
		}
		return '(' + result.substring(0, result.length() - 1) + ')';
	}

	public static String transformTaxTypeToSqlInStatement(List<TaxType> source) {
		StringBuffer result = new StringBuffer("");
		for (TaxType taxType : source) {
			result.append('\'').append(taxType.getCode()).append('\'')
					.append(',');
		}
		return '(' + result.substring(0, result.length() - 1) + ')';
	}

	public static String transformFormKindsToSqlInStatement(
			List<FormDataKind> source) {
		StringBuffer result = new StringBuffer("");
		for (FormDataKind formDataKind : source) {
			result.append(formDataKind.getId()).append(',');
		}
		return '(' + result.substring(0, result.length() - 1) + ')';
	}
}
