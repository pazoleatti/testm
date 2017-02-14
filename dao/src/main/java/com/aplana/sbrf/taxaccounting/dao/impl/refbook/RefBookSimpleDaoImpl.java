package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * DAO для RefBookSimpleDataProvider, поддерживающий редактируемые версионируемые справочники.
 * Иногда такие справочники могут не поддерживать по отдельности редактирование или версионирование.
 *
 * Справочники должны иметь поля STATUS, VERSION, RECORD_ID
 */
@Repository
public class RefBookSimpleDaoImpl extends AbstractDao implements RefBookSimpleDao {

    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;
    @Autowired
    private BDUtils dbUtils;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Загружает данные справочника из отдельной таблицы на определенную дату актуальности
     * @param refBook справочник
     * @param version дата актуальности
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @param isSortAscending признак сортировки по возрастанию
     * @return список записей
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(RefBook refBook, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, sortAttribute, filter, pagingParams, isSortAscending, false);

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBook);

        ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, sortAttribute, filter, null, isSortAscending, false);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    /**
     * Получает запись по уникальному идентификатору
     * @param refBook справочник
     * @param id уникальный идентификатор записи
     * @return Map, где key - alias атрибута, а value - его значение ({@link RefBookValue})
     */
    @Override
    public Map<String, RefBookValue> getRecordData(final RefBook refBook, final Long id) {
        PreparedStatementData ps = queryBuilder.psGetRecordData(refBook);
        ps.addNamedParam("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(ps.getQueryString(), ps.getNamedParams(), new RefBookValueMapper(refBook));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Получение структуры Код строки → Строка справочника по списку кодов строк
     *
     * @param refBook справочник
     * @param recordIds список кодов строк справочника
     */
    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(RefBook refBook, List<Long> recordIds) {
        PreparedStatementData ps = queryBuilder.psGetRecordsData(refBook, recordIds);
        List<Map<String, RefBookValue>> recordsList;
        try {
            recordsList = getJdbcTemplate().query(ps.getQueryString(), new RefBookValueMapper(refBook));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
        Map<Long, Map<String, RefBookValue>> recordData = new HashMap<Long, Map<String, RefBookValue>>();
        for (Map<String, RefBookValue> record : recordsList) {
            recordData.put(record.get("id").getNumberValue().longValue(), record);
        }
        return recordData;
    }

    /**
     * Загружает данные иерархического справочника на определенную дату актуальности
     * @param version дата актуальности
     * @param parentRecordId код родительского элемента
     * @param pagingParams определяет параметры запрашиваемой страницы данных
     * @param filter условие фильтрации строк
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return список записей
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(RefBook refBook, Date version,
                                                                      Long parentRecordId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute) {
        return getChildrenRecords(refBook, version, parentRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(RefBook refBook, Date version,
                                                                      Long parentRecordId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {

        if (refBook.isHierarchic() && parentRecordId == null && filter == null) {
            String fullFilter = RefBook.RECORD_PARENT_ID_ALIAS + " is null";
            return getRecords(refBook, version, pagingParams, fullFilter, sortAttribute, true);
        } else if (!refBook.isHierarchic()){
            throw new IllegalArgumentException(String.format(RefBookDaoImpl.NOT_HIERARCHICAL_REF_BOOK_ERROR, refBook.getName(), refBook.getId()));
        }

        PreparedStatementData ps = queryBuilder.psGetChildrenRecordsQuery(refBook, parentRecordId, version, sortAttribute, filter, pagingParams, isSortAscending);
        List<Map<String, RefBookValue>> records = refBookDao.getRecordsWithHasChild(ps, refBook);

        ps = queryBuilder.psGetChildrenRecordsQuery(refBook, parentRecordId, version, sortAttribute, filter, null, isSortAscending);

        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    /**
     * Получение row_num записи по заданным параметрам
     * @param refBook справочник
     * @param version дата актуальности
     * @param recordId идентификатор искомой записи
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return номер записи
     */
    @Override
    public Long getRowNum(@NotNull RefBook refBook, Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, recordId, null, version, sortAttribute, filter, null, isSortAscending, false);

        return refBookDao.getRowNum(ps, recordId);
    }

    /**
     * Получает уникальные идентификаторы записей, удовлетворяющих условиям фильтра
     *
     * @param refBook справочник
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return список идентификаторов
     */
    @Override
    public List<Long> getUniqueRecordIds(RefBook refBook, Date version, String filter) {
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, null, filter, null, false, true);

        return getJdbcTemplate().queryForList(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
    }

    /**
     * Получает количество уникальных записей, удовлетворяющих условиям фильтра
     * @param version дата актуальности
     * @param filter условие фильтрации строк. Может быть не задано
     * @return количество
     */
    @Override
    public int getRecordsCount(RefBook refBook, Date version, String filter) {
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, null, filter, null, false, false);
        return refBookDao.getRecordsCount(ps);
    }

    private static final String SQL_GET_RECORD_VERSION = "with currentVersion as (select id, version, record_id from %1$s where id = ?),\n" +
            "minNextVersion as (select r.record_id, min(r.version) version from %1$s r, currentVersion cv where r.version > cv.version and r.record_id= cv.record_id and r.status != -1 group by r.record_id),\n" +
            "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %1$s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
            "select cv.id as %2$s, \n" +
            "cv.version as versionStart, \n" +
            "nve.version - interval '1' day as versionEnd, \n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from currentVersion cv \n" +
            "left join nextVersionEnd nve on nve.record_id= cv.record_id";

    /**
     * Возвращает информацию по версии записи справочника
     *
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return версия
     */
    @Override
    public RefBookRecordVersion getRecordVersionInfo(RefBook refBook, Long uniqueRecordId) {
        try {
            String sql = String.format(SQL_GET_RECORD_VERSION, refBook.getTableName(), RefBook.RECORD_ID_ALIAS);
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{
                            uniqueRecordId
                    },
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new DaoException("Не найдены версии для указанного элемента справочника", ex);
        }
    }

    /**
     * Перечень версий записей за период
     * @param refBook справочник
     * @return список дат - версий
     */
    @Override
    public List<Date> getVersions(RefBook refBook, Date startDate, Date endDate) {
        String sql = String.format("SELECT version FROM %s where version >= ? and version <= ? GROUP BY version", refBook.getTableName());
        return getJdbcTemplate().queryForList(sql, new Object[]{startDate, endDate}, new int[]{Types.DATE, Types.DATE}, Date.class);
    }

    /**
     * Возвращает количество существующих версий для элемента справочника
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @return количество версий
     */
    @Override
    public int getRecordVersionsCount(RefBook refBook, Long uniqueRecordId) {
        String sql = "select count(*) as cnt from %1$s where STATUS=" + VersionedObjectStatus.NORMAL.getId() + " and RECORD_ID=(select RECORD_ID from %1$s where ID=?)";
        return getJdbcTemplate().queryForObject(String.format(sql, refBook.getTableName()), Integer.class, uniqueRecordId);
    }

    static final String SQL_GET_RECORD_ID = "select record_id from %s where id = %d";

    /**
     * Возвращает идентификатор записи справочника без учета версий
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи
     * @return
     */
    @Override
    public Long getRecordId(RefBook refBook, Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForObject(String.format(SQL_GET_RECORD_ID, refBook.getTableName(), uniqueRecordId), Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдена запись справочника '%s' (id = %d) с id = %d", (refBook !=null ? refBook.getName() : "null"), (refBook!=null ? refBook.getId() : null), uniqueRecordId));
        }
    }

    /**
     * Возвращает все версии из указанной группы версий записи справочника
     * @param refBook справочник
     * @param recordId идентификатор группы версий записи справочника
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(RefBook refBook, Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        RefBook refBookClone = SerializationUtils.clone(refBook);
        refBookClone.setAttributes(new ArrayList<RefBookAttribute>());
        refBookClone.getAttributes().addAll(refBook.getAttributes());

        // Получение количества данных в справочнике
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBookClone, recordId, null, null, sortAttribute, filter, pagingParams, false, false);
        Integer recordsCount = refBookDao.getRecordsCount(ps);

        refBookClone.addAttribute(RefBook.getVersionFromAttribute());
        refBookClone.addAttribute(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBookClone);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(recordsCount);
        return result;
    }

    /**
     *
     * Поиск среди всех элементов справочника (без учета версий) значений уникальных атрибутов, которые бы дублировались с новыми
     * Обеспечение соблюдения уникальности атрибутов в пределах справочника
     *
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор записи справочника. Может быть null (при создании нового элемента). Используется для исключения из проверки указанного элемента справочника
     * @param record новые значения полей элемента справочника
     * @return список пар идентификатор записи-имя атрибута, у которых совпали значения уникальных атрибутов
     */
    @Override
    public List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(RefBook refBook, Long uniqueRecordId,
                                                                        RefBookRecord record) {

        List<RefBookAttribute> attributes = refBook.getAttributes();
        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> recordsGroupsUniqueAttributesValues =
                aggregateUniqueAttributesAndValues(attributes, record);

        if (recordsGroupsUniqueAttributesValues.isEmpty()) {
            return new ArrayList<Pair<Long, String>>();
        }

        PreparedStatementData ps = queryBuilder.psGetMatchedRecordsByUniqueAttributes(refBook, uniqueRecordId, record,
                recordsGroupsUniqueAttributesValues);

        List<Pair<Long, String>> result = getNamedParameterJdbcTemplate().query(ps.getQueryString(), ps.getNamedParams(),
                new RowMapper<Pair<Long, String>>() {
                    
                    public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Long, String>(SqlUtils.getLong(rs, "id"), rs.getString("name"));
                    }
                });

        return !result.isEmpty() ? aggregateUniqueAttributeNamesByRecords(result) : result;
    }

    /**
     * Агрегировать список пар уникальный атрибут - его значение по группам уникальности и по записям
     *
     * @param attributes атрибуты справочника
     * @param record    запись
     * @return список записей с группами уникальности списков пар уникальных атрибутов и значений
     */
    private Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> aggregateUniqueAttributesAndValues(List<RefBookAttribute> attributes, RefBookRecord record) {
        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> attributeValues = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0) {
                List<Pair<RefBookAttribute, RefBookValue>> values;
                if (attributeValues.get(attribute.getUnique()) != null) {
                    values = attributeValues.get(attribute.getUnique());
                } else {
                    values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
                }
                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, record.getValues().get(attribute.getAlias())));
                attributeValues.put(attribute.getUnique(), values);
            }
        }
        return attributeValues;
    }

    /**
     * Агрегировать названия уникальных аттрибутов по записям
     *
     * @param result список пар идентификатор записи - название уникального атрибута
     * @return список пар идентификатор записи - названия уникальных атрибутов через запятую
     */
    private List<Pair<Long, String>> aggregateUniqueAttributeNamesByRecords(List<Pair<Long, String>> result) {
        List<Pair<Long, String>> matchedRecords = new ArrayList<Pair<Long, String>>();
        Long prevRecordId = 0L;
        String prevName = "";
        for (Pair<Long, String> pair : result) {

            if (!prevRecordId.equals(pair.getFirst()) && prevRecordId != 0) {
                Pair<Long, String> newPair = new Pair<Long, String>(prevRecordId, prevName);
                matchedRecords.add(newPair);
            }

            if (prevRecordId.equals(pair.getFirst())) {
                prevName = prevName + ", " + pair.getSecond();
            } else {
                prevName = pair.getSecond();
            }

            prevRecordId = pair.getFirst();
        }

        Pair<Long, String> newPair = new Pair<Long, String>(prevRecordId, prevName);
        matchedRecords.add(newPair);
        return matchedRecords;
    }

    /**
     * Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
     * @param recordPairs записи, у которых совпали уникальные атрибуты
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата конца актуальности новой версии
     * @return список идентификаторов записей, в которых есть пересечение
     */
    @Override
    public List<Long> checkConflictValuesVersions(RefBook refBook, List<Pair<Long, String>> recordPairs, Date versionFrom, Date versionTo) {
        List<Long> recordIds = new ArrayList<Long>();
        for (Pair<Long, String> pair : recordPairs) {
            recordIds.add(pair.getFirst());
        }
        PreparedStatementData ps = queryBuilder.psCheckConflictValuesVersions(refBook, recordIds, versionFrom, versionTo);
        return getNamedParameterJdbcTemplate().queryForList(ps.getQueryString(), ps.getNamedParams(), Long.class);
    }

    /**
     * Проверяет существуют ли конфликты в датах актуальности у проверяемых записей и их родительских записей (в иерархических справочниках)
     * @param versionFrom дата начала актуальности
     * @param records проверяемые записи
     */
    @Override
    public List<Pair<Long, Integer>> checkParentConflict(RefBook refBook, Date versionFrom, List<RefBookRecord> records) {
        final Set<Pair<Long, Integer>> result = new HashSet<Pair<Long, Integer>>();
        for (RefBookRecord record : records) {
            Long parentId = record.getValues().get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
            if (parentId != null) {
                PreparedStatementData ps = queryBuilder.psCheckParentConflict(refBook, parentId, versionFrom, record.getVersionTo());
                getNamedParameterJdbcTemplate().query(ps.getQueryString(), ps.getNamedParams(), new RowCallbackHandler() {
                    
                    public void processRow(ResultSet rs) throws SQLException {
                        result.add(new Pair<Long, Integer>(SqlUtils.getLong(rs, "id"), SqlUtils.getInteger(rs, "result")));
                    }
                });
            }
        }
        return new ArrayList<Pair<Long, Integer>>(result);
    }

    /**
     * Поиск существующих версий, которые могут пересекаться с новой версией
     * @param refBook справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности новой версии
     * @param versionTo дата окончания актуальности новой версии
     * @param excludedRecordId идентификатор версии записи справочника, которая исключается из проверки пересечения. Используется только при редактировании
     * @return результат проверки по каждой версии, с которой есть пересечение
     */
    @Override
    public List<CheckCrossVersionsResult> checkCrossVersions(RefBook refBook, Long recordId,
                                                             Date versionFrom, Date versionTo, Long excludedRecordId) {


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", refBook.getId());
        params.put("recordId", recordId);
        params.put("excludedRecordId", excludedRecordId);
        params.put("versionFrom", versionFrom);
        params.put("versionTo", versionTo);

        PreparedStatementData ps = queryBuilder.psCheckCrossVersions(refBook);

        return getNamedParameterJdbcTemplate().query(ps.getQueryString(), params, new RowMapper<CheckCrossVersionsResult>() {
            
            public CheckCrossVersionsResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                CheckCrossVersionsResult result = new CheckCrossVersionsResult();
                result.setNum(SqlUtils.getInteger(rs, "NUM"));
                result.setRecordId(SqlUtils.getLong(rs, "ID"));
                result.setVersion(rs.getDate("VERSION"));
                result.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "STATUS")));
                result.setNextVersion(rs.getDate("NEXTVERSION"));
                if (SqlUtils.getInteger(rs, "NEXTSTATUS") != null)
                    result.setNextStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "NEXTSTATUS")));
                else
                    result.setNextStatus(null);
                result.setResult(CrossResult.getResultById(SqlUtils.getInteger(rs, "RESULT")));
                return result;
            }
        });
    }

    /**
     * Проверяет использование записи как родителя для дочерних
     * @param refBook справочник
     * @param parentId уникальный идентификатор записи
     * @param versionFrom дата начала актуальности новой версии
     * @return список пар <дата начала - дата окончания> периода актуальности обнаруженных дочерних записей
     */
    @Override
    public List<Pair<Date, Date>> isVersionUsedLikeParent(RefBook refBook, Long parentId, Date versionFrom) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("versionFrom", versionFrom);
        parameters.put("parentId", parentId);

        PreparedStatementData ps = queryBuilder.psVersionUsedLikeParent(refBook);
        return getNamedParameterJdbcTemplate().query(ps.getQueryString(), parameters,
                new RowMapper<Pair<Date, Date>>() {
                    
                    public Pair<Date, Date> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Date, Date>(rs.getDate("version"), rs.getDate("versionEnd"));
                    }
                });
    }

    /**
     * Возвращает дату начала версии следующей за указанной
     * @param refBook справочник
     * @param version дата актуальности
     * @param filter фильтр для отбора записей. Обязательное поле, т.к записи не фильтруются по RECORD_ID
     * @return дата начала следующей версии
     */
    @Override
    public Date getNextVersion(RefBook refBook, Date version, String filter) {
        PreparedStatementData ps = queryBuilder.psGetNextVersion(refBook, version, filter);

        try {
            List<Date> versions = getNamedParameterJdbcTemplate().query(ps.getQuery().toString(), ps.getNamedParams(),
                    new RowMapper<Date>() {
                        
                        public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getDate("version");
                        }
                    });
            return versions.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String SQL_GET_NEXT_RECORD_VERSION =
            "with nextVersion as (select r.id, r.record_id, r.status, r.version from %1$s r where r.record_id = :recordId and r.status != -1 and r.version  = \n" +
            "\t(select min(version) from %1$s where record_id=r.record_id and status=0 and version > :versionFrom)),\n" +
            "minNextVersion as (select r.record_id, min(r.version) version from %1$s r, nextVersion nv where r.version > nv.version and r.record_id= nv.record_id and r.status != -1 group by r.record_id),\n" +
            "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %1$s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
            "select nv.id as %2$s, nv.version as versionStart, nve.version - interval '1' day as versionEnd,\n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from nextVersion nv \n" +
            "left join nextVersionEnd nve on (nve.record_id= nv.record_id)";

    /**
     * Возвращает данные о версии следующей за указанной
     * @param refBook справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    @Override
    public RefBookRecordVersion getNextVersion(RefBook refBook, Long recordId, Date versionFrom) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("recordId", recordId);
        parameters.addValue("versionFrom", versionFrom);

        String sql = String.format(SQL_GET_NEXT_RECORD_VERSION, refBook.getTableName(), RefBook.RECORD_ID_ALIAS);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql,
                    parameters,
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String SQL_GET_PREVIOUS_RECORD_VERSION =
            "with previousVersion as (select r.id, r.record_id, r.status, r.version from %1$s r where r.record_id = :recordId and r.status = 0 and r.version  = \n" +
            "\t(select max(version) from %1$s where record_id=r.record_id and status=0 and version < :versionFrom)),\n" +
            "minNextVersion as (select r.record_id, min(r.version) version from %1$s r, previousVersion pv where r.version > pv.version and r.record_id= pv.record_id and r.status != -1 group by r.record_id),\n" +
            "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %1$s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
            "select pv.id as %2$s, pv.version as versionStart, nve.version - interval '1' day as versionEnd,\n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from previousVersion pv \n" +
            "left join nextVersionEnd nve on (nve.record_id= pv.record_id)";

    /**
     * Возвращает данные о версии следующей до указанной
     * @param refBook справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param versionFrom дата начала актуальности версии текущей версии, после которой будет выполняться поиск следующей версии
     * @return данные версии
     */
    @Override
    public RefBookRecordVersion getPreviousVersion(RefBook refBook, Long recordId, Date versionFrom) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("recordId", recordId);
        parameters.addValue("versionFrom", versionFrom);

        String sql = String.format(SQL_GET_PREVIOUS_RECORD_VERSION, refBook.getTableName(), RefBook.RECORD_ID_ALIAS);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql,
                    parameters,
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Создает фиктивную запись, являющуюся датой окончания периода актуальности какой то версии
     * @param refBook справочник
     * @param recordId идентификатор записи справочника без учета версий
     * @param version версия записи справочника
     */
    @Override
    public void createFakeRecordVersion(RefBook refBook, Long recordId, Date version) {

        PreparedStatementData ps = queryBuilder.psCreateFakeRecordVersion(refBook, recordId, version);
        getNamedParameterJdbcTemplate().update(ps.getQueryString(), ps.getNamedParams());
    }

    /**
     * Создает новые версии записи в справочнике.
     * Если задан параметр recordId - то создается новая версия записи справочника
     * @param refBook справочник
     * @param version дата актуальности новых записей
     * @param status статус записи
     * @param records список новых записей
     * @return идентификатор записи справочника (без учета версий)
     */
    @Override
    public List<Long> createRecordVersion(final RefBook refBook, final Date version, final VersionedObjectStatus status,
                                          final List<RefBookRecord> records) {

        final List<Long> refBookRecordIds = dbUtils.getNextRefBookRecordIds((long) records.size());
        MapSqlParameterSource[] allParameters = makeParametersForCreateRecordVersion(refBook, version, status, records, refBookRecordIds);

        PreparedStatementData ps = queryBuilder.psCreateRecordVersion(refBook);
        getNamedParameterJdbcTemplate().batchUpdate(ps.getQueryString(), allParameters);
        return refBookRecordIds;
    }

    private MapSqlParameterSource[] makeParametersForCreateRecordVersion(RefBook refBook, Date version, VersionedObjectStatus status,
                                                                         List<RefBookRecord> records, List<Long> refBookRecordIds) {
        MapSqlParameterSource[] allParameters = new MapSqlParameterSource[refBookRecordIds.size()];
        for (int i = 0; i < refBookRecordIds.size(); i++) {
            RefBookRecord record = records.get(i);

            MapSqlParameterSource recordParameters = new MapSqlParameterSource();
            recordParameters.addValue("id", refBookRecordIds.get(i));
            recordParameters.addValue("recordId", record.getRecordId());
            recordParameters.addValue("version", new java.sql.Date(version.getTime()));
            recordParameters.addValue("status", status.getId());

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                Object recordValue = getValueAsObjectFromRecordByAttribute(record, attribute);
                recordParameters.addValue(attribute.getAlias(), recordValue);
            }
            allParameters[i] = recordParameters;
        }
        return allParameters;
    }

    private Object getValueAsObjectFromRecordByAttribute(RefBookRecord record, RefBookAttribute attribute) {
        RefBookValue recordAttributeValue = record.getValues().get(attribute.getAlias());
        if (recordAttributeValue == null) {
            return null;
        } else {
            switch (attribute.getAttributeType()) {
                case STRING:
                    return recordAttributeValue.getStringValue();
                case NUMBER:
                    return recordAttributeValue.getNumberValue();
                case DATE:
                    return recordAttributeValue.getDateValue();
                case REFERENCE:
                    return recordAttributeValue.getReferenceValue();
            }
        }
        return null;
    }

    /**
     * Возвращает все версии указанной записи справочника
     * @param refBook идентификатор справочник
     * @param uniqueRecordId уникальный идентификатор записи, все версии которой будут получены
     * @param pagingParams определяет параметры запрашиваемой страницы данных. Могут быть не заданы
     * @param filter условие фильтрации строк. Может быть не задано
     * @param sortAttribute сортируемый столбец. Может быть не задан
     * @return
     */
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(RefBook refBook, Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook newRefBook = SerializationUtils.clone(refBook);
        // получаем страницу с данными
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(newRefBook, null, uniqueRecordId, null, sortAttribute, filter, pagingParams, isSortAscending, false);

        //Добавляем атрибуты версии, т.к они не хранятся в бд
        newRefBook.getAttributes().add(RefBook.getVersionFromAttribute());
        newRefBook.getAttributes().add(RefBook.getVersionToAttribute());
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(newRefBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве версий
        result.setTotalCount(getRecordVersionsCount(newRefBook, uniqueRecordId));
        return result;
    }

    /**
     * Проверяет существует ли циклическая зависимость для указанных записей справочника
     * Если среди дочерних элементов указанной записи существует указанный родительский элемент, то существует цикл
     * @param uniqueRecordId идентификатор записи
     * @param parentRecordId идентификатор родительской записи
     * @return циклическая зависимость существует?
     */
    public boolean hasLoops(Long uniqueRecordId, Long parentRecordId) {
        throw new UnsupportedOperationException();
    }

    private final static String SQL_FIND_RECORD =
            "select id from %s where record_id = :recordId and version = :version and status != -1";

    /**
     * Возвращает уникальный идентификатор записи, удовлетворяющей указанным условиям
     * @param refBook справочник
     * @param recordId идентификатор записи справочника (без учета версий)
     * @param version дата
     * @return уникальный идентификатор записи, удовлетворяющей указанным условиям
     */
    @Override
    public Long findRecord(RefBook refBook, Long recordId, Date version) {
        String sql = String.format(SQL_FIND_RECORD, refBook.getTableName());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("recordId", recordId);
        parameters.addValue("version", version);

        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql,
                    parameters, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String SQL_GET_RELATED_VERSIONS =
            "with currentRecord as (select id, record_id from %1$s where %2$s),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, STATUS, VERSION, row_number() over(%3$s) rn from %1$s r, currentRecord cr where r.record_id=cr.record_id and r.status != -1) \n" +
            "select rv2.ID from currentRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where cr.id=rv.id and rv2.status=%4$d";

    private static final String SQL_GET_RELATED_VERSIONS_PARTITION = "partition by r.RECORD_ID order by r.version";

    /**
     * Возвращает идентификаторы фиктивных версии, являющихся окончанием указанных версии.
     * Без привязки ко входным параметрам, т.к метод используется просто для удаления по id
     * @param refBook справочник
     * @param uniqueRecordIds идентификаторы версии записи справочника
     * @return идентификаторы фиктивных версии
     */
    @Override
    public List<Long> getRelatedVersions(RefBook refBook, List<Long> uniqueRecordIds) {
        String partition = isSupportOver() ? SQL_GET_RELATED_VERSIONS_PARTITION : "";
        String sql = String.format(SQL_GET_RELATED_VERSIONS,
                refBook.getTableName(), transformToSqlInStatement("id", uniqueRecordIds), partition, VersionedObjectStatus.FAKE.getId());

        try {
            return getJdbcTemplate().queryForList(sql, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    /**
     * Проверяет существование версий записи справочника
     * @param refBook справочник
     * @param recordIds идентификаторы записей справочника без учета версий
     * @param version версия записи справочника
     * @return
     */
    @Override
    public boolean isVersionsExist(RefBook refBook, List<Long> recordIds, Date version) {
        String sql = "select count(*) from %1$s where %2$s and version = trunc(:version, 'DD') and status != -1";
        MapSqlParameterSource parameters = new MapSqlParameterSource("version", version);
        return getNamedParameterJdbcTemplate().queryForObject(String.format(sql, refBook.getTableName(), transformToSqlInStatement("record_id", recordIds)),
                parameters, Integer.class) != 0;
    }

    /**
     * Обновляет значения атрибутов у указанной версии
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника
     * @param records список значений атрибутов
     */
    @Override
    public void updateRecordVersion(RefBook refBook, Long uniqueRecordId, Map<String, RefBookValue> records) {
        try {
            if (records.isEmpty()) {
                return;
            }

            PreparedStatementData ps = queryBuilder.psUpdateRecordVersion(refBook, uniqueRecordId, records);

            getNamedParameterJdbcTemplate().update(ps.getQueryString(), ps.getNamedParams());
        } catch (Exception ex) {
            throw new DaoException("Не удалось обновить значения справочника", ex);
        }
    }

    private final static String SQL_GET_FIRST_RECORD_ID =
            "with allRecords as (select id, version from %1$s where record_id = (select record_id from %1$s where id = :id) and id != :id and status not in (-1, 2))\n" +
            "select id from allRecords where version = (select min(version) from allRecords)";

    /**
     * Получает идентификатор записи, который имеет наименьшую дату начала актуальности для указанной версии
     * @param refBook справочник
     * @param uniqueRecordId идентификатор версии записи справочника
     * @return
     */
    @Override
    public Long getFirstRecordId(RefBook refBook, Long uniqueRecordId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("id", uniqueRecordId);
        String query = String.format(SQL_GET_FIRST_RECORD_ID, refBook.getTableName());
        try {
            return getNamedParameterJdbcTemplate().queryForObject(query,
                    parameters, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String SQL_DELETE_ALL_VERSIONS = "update %1$s set status = -1 where record_id in (select record_id from %1$s where %2$s)";

    /**
     * Удаляет все версии записи из справочника
     * @param refBook справочник
     * @param uniqueRecordIds список идентификаторов записей, все версии которых будут удалены
     */
    @Override
    public void deleteAllRecordVersions(RefBook refBook, List<Long> uniqueRecordIds) {
        String sql = String.format(SQL_DELETE_ALL_VERSIONS, refBook.getTableName(), transformToSqlInStatement("id", uniqueRecordIds));
        getJdbcTemplate().update(sql);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(String tableName, @NotNull Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        // TODO сейчас параметры version и needAccurateVersion игнорируются
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT ");
        ps.appendQuery("id, RECORD_ID");
        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" frb");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        ps.appendQuery(" WHERE status = 0 ");
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" AND ");
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
        }

        if (version != null && needAccurateVersion) {
            ps.appendQuery(" AND ");
            ps.appendQuery(" version = ?");
            ps.addParam(version);
        }

        try {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                    new RowMapper<Pair<Long, Long>>() {
                        @Override
                        public Pair<Long, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Pair<Long, Long>(SqlUtils.getLong(rs, "ID"), SqlUtils.getLong(rs, "RECORD_ID"));
                        }
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
