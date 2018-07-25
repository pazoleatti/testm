package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

/**
 * Реализация дао для работы со справочником Подразделения
 */
@Repository
public class RefBookDepartmentDataDaoImpl extends AbstractDao implements RefBookDepartmentDataDao {

    /**
     * Получение значения справочника по идентификатору
     *
     * @param id Идентификатор подразделения
     * @return Значение справочника
     */
    @Override
    public RefBookDepartment fetchDepartmentById(Integer id) {
        String query = "SELECT * FROM ( " +
                REF_BOOK_DEPARTMENT_SELECT +
                "WHERE dep.is_active = 1 AND dep.id = ? " +
                ") WHERE rownum = 1";
        return getJdbcTemplate().queryForObject(query, new Object[]{id}, new RefBookDepartmentRowMapper());
    }

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Список идентификаторов
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDepartment> fetchDepartments(Collection<Integer> ids) {
        return fetchDepartments(ids, false);
    }

    /**
     * Получение действующих значений справочника по идентификаторам
     *
     * @param ids Список идентификаторов
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDepartment> fetchActiveDepartments(Collection<Integer> ids) {
        return fetchDepartments(ids, true);
    }

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids        Список идентификаторов
     * @param activeOnly Искать только действующие подразделения
     * @return Список значений справочника
     */
    private List<RefBookDepartment> fetchDepartments(Collection<Integer> ids, boolean activeOnly) {
        String sql = REF_BOOK_DEPARTMENT_SELECT + "where dep.is_active = 1 AND dep.id IN (:ids)";
        return getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", ids), new RefBookDepartmentRowMapper());
    }

    /**
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    public PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, PagingParams pagingParams) {
        return fetchDepartments(ids, name, false, pagingParams);
    }

    /**
     * Получение действующих значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    public PagingResult<RefBookDepartment> fetchActiveDepartments(Collection<Integer> ids, String name, PagingParams pagingParams) {
        return fetchDepartments(ids, name, true, pagingParams);
    }

    @Override
    public List<RefBookDepartment> fetchAllActiveByType(DepartmentType type) {
        return getJdbcTemplate().query(REF_BOOK_DEPARTMENT_SELECT + " WHERE dep.type = ? and dep.is_active = 1", new RefBookDepartmentRowMapper(), type.getCode());
    }

    /**
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     * Также можно выбрать все подразделения или только действующие
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param activeOnly   Искать только действующие подразделения
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    private PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, boolean activeOnly, PagingParams pagingParams) {
        String baseSql = String.format(REF_BOOK_DEPARTMENT_SELECT +
                "where %s ", SqlUtils.transformToSqlInStatement("dep.id", ids));

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (!StringUtils.isBlank(name)) {
            baseSql += " and (lower(dep.name) like :name or lower(df.shortname) like :name) ";
            params.addValue("name", "%" + name.toLowerCase() + "%");
        }
        if (activeOnly) {
            baseSql += " and dep.is_active = 1 ";
        }

        if (StringUtils.isNotBlank(pagingParams.getProperty()) && StringUtils.isNotBlank(pagingParams.getDirection())) {
            baseSql += new Formatter().format(" order by %s %s ",
                    FormatUtils.convertToUnderlineStyle(pagingParams.getProperty()),
                    pagingParams.getDirection()).toString();
        }

        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());

        List<RefBookDepartment> departments = getNamedParameterJdbcTemplate().query(
                "select * from ( select a.*, rownum rn from (\n" + baseSql + ") a \n) where rn > :startIndex and rowNum <= :count",
                params,
                new RefBookDepartmentRowMapper());

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (\n" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(departments, totalCount);
    }

    private final static String REF_BOOK_DEPARTMENT_SELECT =
            "select dep.id, dep.name, dep.shortname, dep.parent_id, dep.type, dep.tb_index, dep.sbrf_code, " +
                    "dep.region_id, dep.is_active, dep.code, df.shortname as full_name " +
                    "from department dep " +
                    "inner join department_fullpath df on dep.id = df.id ";

    private static final class RefBookDepartmentRowMapper implements RowMapper<RefBookDepartment> {
        @Override
        public RefBookDepartment mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookDepartment refBookDepartment = new RefBookDepartment();

            refBookDepartment.setId(SqlUtils.getInteger(resultSet, "id"));
            refBookDepartment.setName(resultSet.getString("name"));
            refBookDepartment.setShortName(resultSet.getString("shortname"));
            refBookDepartment.setParentId(SqlUtils.getInteger(resultSet, "parent_id"));
            refBookDepartment.setType(DepartmentType.fromCode(resultSet.getInt("type")));
            refBookDepartment.setTbIndex(resultSet.getString("tb_index"));
            refBookDepartment.setSbrfCode(resultSet.getString("sbrf_code"));
            refBookDepartment.setRegionId(SqlUtils.getLong(resultSet, "region_id"));
            refBookDepartment.setActive(resultSet.getBoolean("is_active"));
            refBookDepartment.setCode(SqlUtils.getLong(resultSet, "code"));
            refBookDepartment.setFullName(resultSet.getString("full_name"));

            return refBookDepartment;
        }
    }
}
