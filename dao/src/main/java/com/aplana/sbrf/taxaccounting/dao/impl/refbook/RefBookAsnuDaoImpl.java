package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Реализация дао для работы со справочником АСНУ
 */
@Repository
public class RefBookAsnuDaoImpl extends AbstractDao implements RefBookAsnuDao {

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookAsnu> fetchAll() {
        //Выбирются записи с положительным значением id, т.к. есть фиктивные записи с id=-1
        return getJdbcTemplate().query("select rba.id, rba.code, rba.name, rba.type " +
                        "from ref_book_asnu rba " +
                        "where rba.id > 0",
                new RefBookAsnuRowMapper());
    }

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Идентификаторы
     * @return Список значений справочника
     */
    @Override
    public List<RefBookAsnu> fetchByIds(List<Long> ids) {
        return getNamedParameterJdbcTemplate().query("select rba.id, rba.code, rba.name, rba.type " +
                        "from ref_book_asnu rba " +
                        "where rba.id in (:ids)",
                new MapSqlParameterSource("ids", ids),
                new RefBookAsnuRowMapper());
    }

    @Override
    public RefBookAsnu fetchById(Long id) {
        return getNamedParameterJdbcTemplate().queryForObject("select rba.id, rba.code, rba.name, rba.type " +
                        "from ref_book_asnu rba " +
                        "where rba.id = :id",
                new MapSqlParameterSource("id", id),
                new RefBookAsnuRowMapper());
    }

    private static final class RefBookAsnuRowMapper implements RowMapper<RefBookAsnu> {
        @Override
        public RefBookAsnu mapRow(ResultSet rs, int i) throws SQLException {
            RefBookAsnu refBookAsnu = new RefBookAsnu();

            refBookAsnu.setId(SqlUtils.getLong(rs, "id"));
            refBookAsnu.setCode(rs.getString("code"));
            refBookAsnu.setName(rs.getString("name"));
            refBookAsnu.setType("type");

            return refBookAsnu;
        }
    }
}