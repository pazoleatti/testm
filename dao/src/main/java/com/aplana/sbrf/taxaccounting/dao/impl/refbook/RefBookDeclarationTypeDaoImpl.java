package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Реализация дао для работы со справочником Виды форм
 */
@Repository
public class RefBookDeclarationTypeDaoImpl extends AbstractDao implements RefBookDeclarationTypeDao {

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDeclarationType> fetchAll() {
        return getJdbcTemplate().query("select dt.id, dt.name " +
                        "from declaration_type dt " +
                        "where dt.status = 0 " +
                        "order by dt.name asc",
                new RefBookDeclarationTypeRowMapper());
    }

    /**
     * Получение значений справочника на основе типа формы, подразделения и начала отчетного периода. Выполняется поиск
     * назначенных подразделению видов форм с действующей на момент начала периода версией шаблона формы указанного типа.
     * Т.е. видов форм, назначенных заданному подразделению, имеющих статус версии "действующий" и для которых есть шаблон
     * формы с заданным типом формы, "действующим" статусом версии и версией не более поздней, чем заданное начало
     * отчетного периода
     *
     * @param declarationKind Тип налоговой формы
     * @param departmentId    ID подразделения
     * @param periodStartDate Начало отчетного периода
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDeclarationType> fetchDeclarationTypes(Long declarationKind, Integer departmentId, Date periodStartDate) {
        String query = "select dt.id, dt.name " +
                "from declaration_type dt " +
                "join department_declaration_type ddt " +
                "on dt.id = ddt.declaration_type_id " +
                "where dt.status = 0 " +
                "and dt.id in " +
                "(select distinct dtemplate.declaration_type_id " +
                "from declaration_template dtemplate " +
                "where dtemplate.status = 0 " +
                "and dtemplate.form_kind = :declarationKind " +
                "and not trunc(dtemplate.version) > :periodStartDate) and ddt.department_id = :departmentId " +
                "order by dt.name asc";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationKind", declarationKind)
                .addValue("departmentId", departmentId)
                .addValue("periodStartDate", periodStartDate);
        try {
            return getNamedParameterJdbcTemplate().query(query, params, new RefBookDeclarationTypeRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    private static final class RefBookDeclarationTypeRowMapper implements RowMapper<RefBookDeclarationType> {
        @Override
        public RefBookDeclarationType mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookDeclarationType refBookDeclarationType = new RefBookDeclarationType();

            refBookDeclarationType.setId(SqlUtils.getLong(resultSet, "id"));
            refBookDeclarationType.setName(resultSet.getString("name"));

            return refBookDeclarationType;
        }
    }
}