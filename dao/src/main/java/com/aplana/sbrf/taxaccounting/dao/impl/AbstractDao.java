package com.aplana.sbrf.taxaccounting.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;

/**
 * Абстрактный класс, позволяющий делать автовайринг с DataSource'ом
 * и содержащий вспомогательные методы, востребованные наследниками
 */
public abstract class AbstractDao {

    /**
     * Указатель на строку в результерующем наборе данных
     */
    public static final int CURSOR = -10;

    /**
     * Ограничение элементов у оператора in в sql выражении.
     */
    protected static final int IN_CLAUSE_LIMIT = 1000;


    @Autowired
    private DBInfo dbInfo;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected JdbcTemplate getJdbcTemplate() {
        return (JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations();
    }

    protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    /**
     * Возвращает новое значение id, следующее за текущим значением sequenceName
     *
     * @param sequenceName - наименования последовательности, из которой следует получить следующее значение
     * @param resultType   - тип возвращаемого значения
     * @return объект класса resultType со значением, следующим за текущим значеним sequenceName
     */
    protected <T extends Number> T generateId(String sequenceName, Class<T> resultType) {
        return getJdbcTemplate().queryForObject("select " + sequenceName + ".nextval from dual", resultType);
    }

    /**
     * Возвращает признак поддержки функции row_number() over (...)
     *
     * @return признак поддержки
     */
    protected boolean isSupportOver() {
        return dbInfo.isSupportOver();
    }

    /**
     * Возвращает признак добавления служебного слова 'recursive' в конструкцию рекурсивных запросов with...
     * не работает с Oracle
     *
     * @return признак добавления
     */
    protected boolean isWithRecursive() {
        return dbInfo.isWithRecursive();
    }

    protected boolean isDateDiffNumber() {
        return dbInfo.isDateDiffNumber();
    }

    /**
     * Получить часть листа размер которого ограничен значением {@code IN_CLAUSE_LIMIT}.
     * @param list  основной лист
     * @param i     порядковый номер части
     * @return  часть основного листа размером не более 1000, начало которого является элементом лежащего по индексу {@code i} * {@code IN_CLAUSE_LIMIT} основного листа
     */
    protected List<Long> getSubList(List<Long> list, int i) {
        return list.subList(i * IN_CLAUSE_LIMIT, Math.min((i + 1) * IN_CLAUSE_LIMIT, list.size()));
    }
}
