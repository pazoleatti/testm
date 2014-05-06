package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
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

	static void checkListSize(List<?> list) {
		if (list == null) {
			throw new IllegalArgumentException("List parameter must be defined");
		}
		if (list.size() < 1) {
			throw new IllegalArgumentException("List must not be empty");
		}
	}

	/**
	 * Функция преобразует список (например, содержащий элементы 1,2,3,4) в
	 * строку вида "(1,2,3,4)", которая бдует использоваться в SQL запросах вида
	 * "...where param in (1,2,3,4)";
	 */
	public static String transformToSqlInStatement(List<? extends Number> list) {
		checkListSize(list);
		return '(' + StringUtils.join(list.toArray(), ',') + ')';
	}

	public static String transformFormStatesToSqlInStatement(List<WorkflowState> source) {
		checkListSize(source);
		StringBuilder result = new StringBuilder();
		result.append('(');
		for (WorkflowState workflowState : source) {
			result.append(workflowState.getId()).append(',');
		}
		return result.deleteCharAt(result.length() - 1).append(')').toString();
	}

	public static String transformTaxTypeToSqlInStatement(List<TaxType> source) {
		checkListSize(source);
		StringBuilder result = new StringBuilder();
		result.append('(');
		for (TaxType taxType : source) {
			result.append('\'').append(taxType.getCode()).append('\'')
					.append(',');
		}
		return result.deleteCharAt(result.length() - 1).append(')').toString();
	}

	public static String transformFormKindsToSqlInStatement(List<FormDataKind> source) {
		checkListSize(source);
		StringBuilder result = new StringBuilder();
		result.append('(');
		for (FormDataKind formDataKind : source) {
			result.append(formDataKind.getId()).append(',');
		}
		return result.deleteCharAt(result.length() - 1).append(')').toString();
	}

    /**
     * Подготовка строки вида "?,?,?,..."
     */
    public static String preparePlaceHolders(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("Parameter 'length' must be positive integer number");
		}
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length;) {
			result.append('?');
			if (++i == length) {
				return result.toString();
			}
			result.append(',');
        }
	    return null; // недостижимый код
    }

    public static Long getLong(ResultSet resultSet, String columnLabel) throws SQLException {
        Long ret = resultSet.getLong(columnLabel);
        return resultSet.wasNull()?null:ret;
    }

    public static Long getLong(ResultSet resultSet, int columnIndex) throws SQLException {
        Long ret = resultSet.getLong(columnIndex);
        return resultSet.wasNull()?null:ret;
    }

    public static Integer getInteger(ResultSet resultSet, String columnLabel) throws SQLException {
        Integer ret = resultSet.getInt(columnLabel);
        return resultSet.wasNull()?null:ret;
    }

    public static Integer getInteger(ResultSet resultSet, int columnIndex) throws SQLException{
        Integer ret = resultSet.getInt(columnIndex);
        return resultSet.wasNull()?null:ret;
    }
}
