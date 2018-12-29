package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Реализация дао для работы со справочником АСНУ
 */
@Repository
public class RefBookAsnuDaoImpl extends AbstractDao implements RefBookAsnuDao {

    @Override
    public List<RefBookAsnu> findAll() {
        //Выбирются записи с положительным значением id, т.к. есть фиктивные записи с id=-1
        return getJdbcTemplate().query("select id, code, name, type, priority " +
                        "from ref_book_asnu " +
                        "where id > 0 order by name asc",
                new RefBookAsnuRowMapper());
    }

    @Override
    public List<RefBookAsnu> findAllByIdIn(List<Long> ids) {
        if (!isEmpty(ids)) {
            return getNamedParameterJdbcTemplate().query("select id, code, name, type, priority " +
                            "from ref_book_asnu " +
                            "where id in (:ids)",
                    new MapSqlParameterSource("ids", ids),
                    new RefBookAsnuRowMapper());
        }
        return new ArrayList<>();
    }

    @Override
    public RefBookAsnu findById(Long id) {
        return getNamedParameterJdbcTemplate().queryForObject("select id, code, name, type, priority " +
                        "from ref_book_asnu " +
                        "where id = :id",
                new MapSqlParameterSource("id", id),
                new RefBookAsnuRowMapper());
    }

    @Override
    public RefBookAsnu findByName(String name) {
        try {
            return getNamedParameterJdbcTemplate().queryForObject(
                    "select id, code, name, type, priority from ref_book_asnu " +
                            "where lower(name) = :name",
                    new MapSqlParameterSource("name", name.toLowerCase()),
                    new RefBookAsnuRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class RefBookAsnuRowMapper implements RowMapper<RefBookAsnu> {
        @Override
        public RefBookAsnu mapRow(ResultSet rs, int i) throws SQLException {
            RefBookAsnu refBookAsnu = new RefBookAsnu();
            refBookAsnu.setId(SqlUtils.getLong(rs, "id"));
            refBookAsnu.setCode(rs.getString("code"));
            refBookAsnu.setName(rs.getString("name"));
            refBookAsnu.setType("type");
            refBookAsnu.setPriority(SqlUtils.getInteger(rs, "priority"));
            return refBookAsnu;
        }
    }
}