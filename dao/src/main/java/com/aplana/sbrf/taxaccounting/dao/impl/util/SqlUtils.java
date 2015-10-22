package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    public static final int IN_CAUSE_LIMIT = 1000;

	static void checkListSize(Collection<?> collection) {
		if (collection == null) {
			throw new IllegalArgumentException("List parameter must be defined");
		}
		if (collection.size() < 1) {
			throw new IllegalArgumentException("List must not be empty");
		}
	}

	/**
     * <p>
	 * Метод возвращает строку вида prefix in (...) or prefix in (...) разбивая
     * параметры в условии in по size штук.
	 * </p>
	 * Пример вызова:
	 * <p>
	 * 	 transformToSqlInStatement("form_data.id", [309, 376, 410], 1000)
	 * </p>
	 *
     * @param prefix название поля в бд
     * @param collection коллекция идентификаторо
     * @param size размер идентификаторов в условии in
	 */
    public static String transformToSqlInStatement(String prefix, Collection<?> collection, int size) {
        HashSet<Object> set = new HashSet<Object>(collection);
        checkListSize(set);

        List<String> strings = new ArrayList<String>();
        List<List<?>> lists = new ArrayList<List<?>>(splitCollection(set, size));

        for (List<?> list : lists) {
            StringBuffer buffer = new StringBuffer();
            buffer
                    .append(prefix)
                    .append(" IN ")
                    .append("(")
                    .append(StringUtils.join(list.toArray(), ','))
                    .append(")");

            strings.add(buffer.toString());
        }

        StringBuffer buffer = new StringBuffer();
        buffer
                .append("(")
                .append(StringUtils.join(strings.toArray(), " OR ", ""))
                .append(")");

        return buffer.toString();
    }

	/**
	 * <p>
	 * Метод возвращает строку вида prefix in (...) or prefix in (...) разбивая параметры в условии in по IN_CAUSE_LIMIT штук.
	 * </p>
	 * Пример вызова:
	 * <p>
	 * 	 transformToSqlInStatement("form_data.id", [309, 376, 410], 1000)
	 * </p>
	 */
    public static String transformToSqlInStatement(String prefix, Collection<?> collection) {
        return transformToSqlInStatement(prefix, collection, IN_CAUSE_LIMIT);
	}

    public static String transformToSqlInStatementForString(String prefix, Collection<String> collection) {
        List<String> strings = new ArrayList<String>();
        for (String s : collection) {
            strings.add("'" + s + "'");
        }

        return transformToSqlInStatement(prefix, strings, IN_CAUSE_LIMIT);
    }

    /**
     * Метод разбивает коллекцию на коллекции определенного размера
     * @param data
     * @param size
     * @param <T>
     * @return
     */
    public static <T> Collection<List<T>> splitCollection(Collection<T> data, int size){
        Collection<List<T>> result = new ArrayList<List<T>>();
        int c = 0;
        List<T> list =  new ArrayList<T>();
        Iterator<T> iterator = data.iterator();
        while(iterator.hasNext()){
            if (c == size){
                c = 0;
            }
            if (c == 0 && !list.isEmpty()){
                result.add(list);
                list = new ArrayList<T>();
            }
            list.add(iterator.next());
            c = c == size ? 0 : c + 1;
        }
        if (!list.isEmpty()){
            result.add(list);
        }
        return result;
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
