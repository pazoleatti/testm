package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
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
@Configurable
public final class SqlUtils extends AbstractDao {
    private static final Log LOG = LogFactory.getLog(SqlUtils.class);

    private static SqlUtils repository = new SqlUtils();

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
     * transformToSqlInStatement("form_data.id", [309, 376, 410], 1000)
     * </p>
     *
     * @param prefix     название поля в бд
     * @param collection коллекция идентификаторо
     * @param size       размер идентификаторов в условии in
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
     * transformToSqlInStatement("form_data.id", [309, 376, 410], 1000)
     * </p>
     */
    public static String transformToSqlInStatement(String prefix, Collection<?> collection) {
        return transformToSqlInStatement(prefix, collection, IN_CAUSE_LIMIT);
    }

    /**
     * Alias for {@link #transformToSqlInStatement(String, Collection)}
     */
    public static String in(String prefix, Collection<?> collection) {
        return transformToSqlInStatement(prefix, collection);
    }

    /**
     * Формирует in () sql-выражение для строковой коллекции с добавлением одинарных кавычек для запроса
     *
     * @param prefix     значение, которое будет соединяться со значениями из коллекции
     * @param collection коллекция строк
     * @return sql-условие вида "prefix in (...)"
     */
    public static String transformToSqlInStatementForString(String prefix, Collection<String> collection) {
        List<String> strings = new ArrayList<String>();
        for (String s : collection) {
            strings.add("'" + s + "'");
        }

        return transformToSqlInStatement(prefix, strings, IN_CAUSE_LIMIT);
    }

    /**
     * Формирует in () sql-выражение для строковой с добавлением одинарных кавычек для запроса
     * в основном применимо для коллекций enum-ов
     *
     * @param prefix     значение, которое будет соединяться со значениями из коллекции
     * @param collection коллекция объектов, строковое значение которых используется в запросе (enum)
     * @return sql-условие вида "prefix in (...)"
     */
    public static String transformToSqlInStatementForStringFromObject(String prefix, Collection<?> collection) {
        List<String> strings = new ArrayList<String>();
        for (Object object : collection) {
            strings.add("'" + object + "'");
        }

        return transformToSqlInStatement(prefix, strings, IN_CAUSE_LIMIT);
    }

    /**
     * Сохраняет коллекцию во временную таблицу, а затем формирует sql-условие in с этой таблицей вида "prefix in (select * from tmp)"
     *
     * @param prefix     значение, которое будет соединяться со значениями из коллекции
     * @param collection коллекция чисел
     * @return sql-условие вида "prefix in (...)"
     */
    public static String transformToSqlInStatementViaTmpTable(String prefix, Collection<? extends Number> collection) {
        return transformToSqlInStatementViaTmpTable(prefix, collection, 0);
    }

    /**
     * Формирует in () sql-выражение для коллекции чисел.
     * Значения коллекции не будут лежать в самом тексте запроса, а будут добавлены и затем запрошены из временной таблицы
     *
     * @param prefix       значение, которое будет соединяться со значениями из коллекции
     * @param collection   коллекция чисел
     * @param statementNum если в запросе несколько таких условий, то для каждого указывать разные значения для параметра (начиная с 0, пока доступно 2)
     * @return sql-условие вида "prefix in (...)"
     */
    public static String transformToSqlInStatementViaTmpTable(String prefix, Collection<? extends Number> collection, int statementNum) {
        checkListSize(collection);
        String collectionTable = saveCollectionAsTable(collection, statementNum);
        return String.format("%s in (select num from %s)", prefix, collectionTable);
    }

    /**
     * Формирует prefix in (...) sql выражение для объектов, которые наследуют {@link IdentityObject}
     * для поиска значений по id
     *
     * @param prefix  значение, которое будет соединяться со значениями из коллекции
     * @param objects коллекция объектов, по id которых необходимо проводить поиск
     * @return sql условие вида "prefix in (...)"
     */
    public static String transformToSqlInStatementById(String prefix, Collection<? extends IdentityObject> objects) {
        List<Number> result = new ArrayList<>();
        for (IdentityObject object : objects) {
            result.add(object.getId());
        }
        return transformToSqlInStatement(prefix, result);
    }

    /**
     * Сохраняет коллекцию чисел в определенную временную таблицу и возвращяет название этой таблицы
     *
     * @param collection коллекция чисел
     * @param tableIndex индекс временной таблицы
     * @return название таблицы, в которую были сохранена коллекция
     */
    private static String saveCollectionAsTable(final Collection<? extends Number> collection, int tableIndex) {
        String tableName = "TMP_NUMBERS" + tableIndex;// - global temporary table on commit delete rows
        if (tableIndex > 1) {
            throw new IllegalArgumentException(String.format("table %s not exists", tableName));
        }
        truncateTable(tableName); // нужно т.к. в одной транзакции могут быть несколько запросов
        saveCollectionAsTable(collection, tableName);
        return tableName;
    }

    private static void saveCollectionAsTable(final Collection<? extends Number> collection, String tableName) {
        final Iterator<? extends Number> iterator = collection.iterator();
        repository.getJdbcTemplate().batchUpdate("insert into " + tableName + "(num) VALUES(?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, iterator.next().longValue());
            }

            @Override
            public int getBatchSize() {
                return collection.size();
            }
        });
    }

    /**
     * Формирует in () sql-выражение для коллекции пар строк.
     * Значения коллекции не будут лежать в самом тексте запроса, а будут добавлены и затем запрошены из временной таблицы
     *
     * @param prefix     значение, которое будет соединяться со значениями из коллекции
     * @param collection коллекция пар строк
     * @return sql-условие вида "prefix in (...)"
     */
    public static String pairInStatement(String prefix, Collection<Pair<String, String>> collection) {
        String collectionTable = saveCollectionAsTable(collection);
        return String.format("%s in (select string1, string2 from %s)", prefix, collectionTable);
    }

    /**
     * Сохраняет коллекцию пар строк в определенную временную таблицу и возвращяет название этой таблицы
     *
     * @param collection коллекция чисел
     * @return название таблицы, в которую были сохранена коллекция
     */
    private static String saveCollectionAsTable(final Collection<Pair<String, String>> collection) {
        String tableName = "TMP_STRING_PAIRS";// - global temporary table on commit delete rows
        truncateTable(tableName); // нужно т.к. в одной транзакции могут быть несколько запросов
        savePairCollectionAsTable(collection, tableName);
        return tableName;
    }

    private static void savePairCollectionAsTable(final Collection<Pair<String, String>> collection, String tableName) {
        if (collection != null && !collection.isEmpty()) {
            final Iterator<Pair<String, String>> iterator = collection.iterator();
            repository.getJdbcTemplate().batchUpdate("insert into " + tableName + "(string1, string2) VALUES(?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Pair<String, String> pair = iterator.next();
                    ps.setString(1, pair.getFirst());
                    ps.setString(2, pair.getSecond());
                }

                @Override
                public int getBatchSize() {
                    return collection.size();
                }
            });
        }
    }

    /**
     * Очищает таблицу
     *
     * @param tableName таблица
     */
    private static void truncateTable(String tableName) {
        try {
            repository.getJdbcTemplate().update("truncate table " + tableName);
        } catch (DataAccessException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Метод разбивает коллекцию на коллекции определенного размера
     *
     * @param data
     * @param size
     * @param <T>
     * @return
     */
    public static <T> Collection<List<T>> splitCollection(Collection<T> data, int size) {
        Collection<List<T>> result = new ArrayList<List<T>>();
        int c = 0;
        List<T> list = new ArrayList<T>();
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            if (c == size) {
                c = 0;
            }
            if (c == 0 && !list.isEmpty()) {
                result.add(list);
                list = new ArrayList<T>();
            }
            list.add(iterator.next());
            c = c == size ? 0 : c + 1;
        }
        if (!list.isEmpty()) {
            result.add(list);
        }
        return result;
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

    public static String transformDeclarationFormKindsToSqlInStatement(List<DeclarationFormKind> source) {
        checkListSize(source);
        StringBuilder result = new StringBuilder();
        result.append('(');
        for (DeclarationFormKind declarationFormKind : source) {
            result.append(declarationFormKind.getId()).append(',');
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
        for (int i = 0; i < length; ) {
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
        return resultSet.wasNull() ? null : ret;
    }

    public static Long getLong(ResultSet resultSet, int columnIndex) throws SQLException {
        Long ret = resultSet.getLong(columnIndex);
        return resultSet.wasNull() ? null : ret;
    }

    /**
     * Возвращает значение целочисленного столбца. Если значения нет, то вернет null
     *
     * @param resultSet   набор данных
     * @param columnLabel название столбца
     * @return целое число, либо null
     * @throws SQLException
     */
    public static Integer getInteger(ResultSet resultSet, String columnLabel) throws SQLException {
        int ret = resultSet.getInt(columnLabel);
        return resultSet.wasNull() ? null : ret;
    }

    /**
     * Возвращает значение целочисленного столбца. Если значения нет, то вернет null
     *
     * @param resultSet   набор данных
     * @param columnIndex индекс столбца
     * @return целое число, либо null
     * @throws SQLException
     */
    public static Integer getInteger(ResultSet resultSet, int columnIndex) throws SQLException {
        int ret = resultSet.getInt(columnIndex);
        return resultSet.wasNull() ? null : ret;
    }

    /**
     * Возвращает строку наименований столбцов на основании их массива
     *
     * @param columnNames - массив наименований столбцов
     * @param prefix      - символ, который необходимо вставить перед именем столбца
     * @return
     */
    public static String getColumnsToString(String[] columnNames, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            sb.append(prefix != null ? prefix + columnNames[i] : columnNames[i]);
            if (i < columnNames.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Проверяет существует ли столбец
     *
     * @param rs
     * @param columnName - имя столбца
     * @return
     * @throws SQLException
     */
    public static boolean isExistColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
        }
        return false;
    }


    public static String createInsert(String table, String[] columns, String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table);
        sb.append(toSqlString(columns));
        sb.append(" VALUES ");
        sb.append(toSqlParameters(fields));
        return sb.toString();
    }

    public static String createUpdate(String table, String[] columns, String[] fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("update ")
                .append(table)
                .append(" set ");
        if (columns.length != fields.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < columns.length; i++) {
            if (!columns[i].equals("id")) {
                sb.append(columns[i])
                        .append(" = :")
                        .append(fields[i])
                        .append(", ");
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" ")
                .append("where id = :id");
        return sb.toString();
    }

    /**
     * Метод преобразует массив {"a", "b", "c"} в строку "(a, b, c)"
     *
     * @param a исходный массив
     * @return строка
     */
    public static String toSqlString(Object[] a) {
        if (a == null) {
            return "";
        }
        int iMax = a.length - 1;
        if (iMax == -1) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append('(');
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax) {
                return b.append(')').toString();
            }
            b.append(", ");
        }
    }

    private static String toSqlParameters(String[] fields) {
        List<String> result = new ArrayList<>();
        for (String field : fields) {
            result.add(":" + field);
        }
        return toSqlString(result.toArray());
    }
}