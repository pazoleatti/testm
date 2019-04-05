package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SubsystemDao;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import org.intellij.lang.annotations.Language;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


@Repository
public class SubsystemDaoImpl extends AbstractDao implements SubsystemDao {

    @Language("sql")
    private static final String SELECT_SUBSYSTEM = "select id, code, name, short_name from vw_subsystem_syn";


    @Override
    public Subsystem findById(long id) {
        try {
            String sql = SELECT_SUBSYSTEM + " where id = ?";
            return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new int[]{Types.BIGINT}, new SubsystemRowMapper());
        } catch (
                EmptyResultDataAccessException e) {
            return null;
        }
    }


    private static final class SubsystemRowMapper implements RowMapper<Subsystem> {
        @Override
        public Subsystem mapRow(ResultSet rs, int index) throws SQLException {
            Subsystem result = new Subsystem();
            result.setId(rs.getLong("id"));
            result.setCode(rs.getString("code"));
            result.setName(rs.getString("name"));
            result.setShortName(rs.getString("short_name"));
            return result;
        }
    }
}
