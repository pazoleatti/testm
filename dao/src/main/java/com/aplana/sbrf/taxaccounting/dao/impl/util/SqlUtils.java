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
     * Метод возвращает строку вида prefix in (...) or prefix in (...) разбивая
     * параметры в условии in по size штук.
     * @param prefix название поля в бд
     * @param collection коллекция идентификаторо
     * @param size размер идентификаторов в условии in
	 */
    public static String transformToSqlInStatement(String prefix, Collection<? extends Number> collection, int size) {
        checkListSize(collection);

        List<String> strings = new ArrayList<String>();
        List<List<? extends Number>> lists = new ArrayList<List<? extends Number>>(splitCollection(collection, size));

        for (List<? extends Number> list : lists) {
            StringBuffer buffer = new StringBuffer();
            buffer
                    .append(prefix)
                    .append(" in ")
                    .append("(")
                    .append(StringUtils.join(list.toArray(), ','))
                    .append(")");

            strings.add(buffer.toString());
        }

        StringBuffer buffer = new StringBuffer();
        buffer
                .append("(")
                .append(StringUtils.join(strings.toArray(), " or ", ""))
                .append(")");

        return buffer.toString();
    }

    public static String transformToSqlInStatement(String prefix, Collection<? extends Number> collection) {
        return transformToSqlInStatement(prefix, collection, IN_CAUSE_LIMIT);
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
        List<T> list =  new ArrayList<T>();;
        Iterator<T> iterator = data.iterator();
        while(iterator.hasNext()){
            if (c == size){
                c = 0;
            }

            if (c == 0 && list.size() > 0){
                result.add(list);
                list = new ArrayList<T>();
            }

            list.add(iterator.next());
            c = c == size ? 0 : c + 1;
        }

        if (list.size() > 0){
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
}
