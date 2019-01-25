package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.Collection;
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
    private DBUtilsImpl dbUtils;

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
     * @param sequenceName наименования последовательности, из которой следует получить следующее значение
     * @param resultType   тип возвращаемого значения
     * @return объект класса resultType со значением, следующим за текущим значеним sequenceName
     */
    protected <T extends Number> T generateId(String sequenceName, Class<T> resultType) {
        return getJdbcTemplate().queryForObject("select " + sequenceName + ".nextval from dual", resultType);
    }

    /**
     * Возвращает список сгенерированных id для последовательности sequenceName
     *
     * @param sequenceName наименования последовательности
     * @param resultType   тип возвращаемого значения
     * @return список идентификаторов класса resultType
     */
    protected <T extends Number> List<T> generateIds(String sequenceName, int count, Class<T> resultType) {
        if (isSupportOver())
            return getJdbcTemplate().queryForList("SELECT " + sequenceName + ".NEXTVAL FROM DUAL CONNECT BY LEVEL<= ?", new Object[]{count}, resultType);
        else {
            ArrayList<T> listIds = new ArrayList<>(count);
            for (Integer i = 0; i < count; i++) {
                listIds.add(getJdbcTemplate().queryForObject("SELECT " + sequenceName + ".NEXTVAL FROM DUAL", resultType));
            }
            return listIds;
        }
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
     *
     * @param list основной лист
     * @param i    порядковый номер части
     * @return часть основного листа размером не более 1000, начало которого является элементом лежащего по индексу {@code i} * {@code IN_CLAUSE_LIMIT} основного листа
     */
    protected List<Long> getSubList(List<Long> list, int i) {
        return list.subList(i * IN_CLAUSE_LIMIT, Math.min((i + 1) * IN_CLAUSE_LIMIT, list.size()));
    }

    /**
     * Метод сохраняет новые объекты в БД
     *
     * @param identityObjects объекты обладающий суррогатным ключом
     * @param table           наименование таблицы используемой для хранения данных объекта
     * @param seq             наименование последовательностт используемой для генерации ключей
     * @param columns         массив, содержащий наименование столбцов таблицы для вставки в insert
     * @param fields          массив, содержащий пути в объекте до полей, соответствующих столбцам
     *                        !!! Не допускаются null во вложенных объектах,
     *                        например, для "country.id" country не должен быть равен null, так что используем пустой объект вместо null (TODO?)
     * @param <E>             тип объекта
     */
    protected <E extends IdentityObject> void saveNewObjects(Collection<E> identityObjects, String table, String seq, String[] columns, String[] fields) {
        List<Long> ids = dbUtils.getNextIds(seq, identityObjects.size());
        String insert = SqlUtils.createInsert(table, columns, fields);
        BeanPropertySqlParameterSource[] batchArgs = new BeanPropertySqlParameterSource[identityObjects.size()];
        int i = 0;
        for (E identityObject : identityObjects) {
            identityObject.setId(ids.get(i));
            batchArgs[i] = new BeanPropertySqlParameterSource(identityObject);
            i++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(insert, batchArgs);
    }

    /**
     * Метод сохраняет новые объекты в БД у которых могут быть уже определены идентификаторы. Тогда для таких записей идентификаторы не будут генерироваться.
     *
     * @param identityObjects объекты обладающий суррогатным ключом
     * @param table           наименование таблицы используемой для хранения данных объекта
     * @param seq             наименование последовательностт используемой для генерации ключей
     * @param columns         массив, содержащий наименование столбцов таблицы для вставки в insert
     * @param fields          массив, содержащий пути в объекте до полей, соответствующих столбцам
     *                        !!! Не допускаются null во вложенных объектах,
     *                        например, для "country.id" country не должен быть равен null, так что используем пустой объект вместо null
     * @param <E>             тип объекта
     */
    protected <E extends IdentityObject> void saveNewObjectsWithDefinedIds(Collection<E> identityObjects, String table, String seq, String[] columns, String[] fields) {
        String insert = SqlUtils.createInsert(table, columns, fields);
        BeanPropertySqlParameterSource[] batchArgs = new BeanPropertySqlParameterSource[identityObjects.size()];
        int i = 0;
        for (E identityObject : identityObjects) {
            if (identityObject.getId() == null) {
                identityObject.setId(dbUtils.getNextIds(seq, 1).get(0));
            }
            batchArgs[i] = new BeanPropertySqlParameterSource(identityObject);
            i++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(insert, batchArgs);
    }

    /**
     * Метод обновляет объекты в БД
     *
     * @param identityObjects объекты обладающий суррогатным ключом
     * @param table           наименование таблицы используемой для хранения данных объекта
     * @param columns         массив, содержащий наименование столбцов таблицы для вставки в insert
     * @param fields          массив, содержащий пути в объекте до полей, соответствующих столбцам
     *                        !!! Не допускаются null во вложенных объектах,
     *                        например, для "country.id" country не должен быть равен null, так что используем пустой объект вместо null (TODO?)
     * @param <E>             тип объекта
     */
    protected <E extends IdentityObject> void updateObjects(Collection<E> identityObjects, String table, String[] columns, String[] fields) {
        String update = SqlUtils.createUpdate(table, columns, fields);
        BeanPropertySqlParameterSource[] batchArgs = new BeanPropertySqlParameterSource[identityObjects.size()];
        int i = 0;
        for (E identityObject : identityObjects) {
            batchArgs[i] = new BeanPropertySqlParameterSource(identityObject);
            i++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(update, batchArgs);
    }
}
