package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SubsystemDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import org.intellij.lang.annotations.Language;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;


@Repository
public class SubsystemDaoImpl extends AbstractDao implements SubsystemDao {

    private static final String SUBSYSTEM_TABLE_NAME = "vw_subsystem_syn";

    @Language("sql")
    private static final String SELECT_SUBSYSTEM = "select id, code, name, short_name from " + SUBSYSTEM_TABLE_NAME;


    @Override
    public Subsystem findById(long id) {
        try {
            String sql = SELECT_SUBSYSTEM + " where id = ?";
            return getJdbcTemplate().queryForObject(sql, new Object[]{id}, new int[]{Types.BIGINT}, new SubsystemRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public PagingResult<Subsystem> findByName(String name) {

        String conditions = " lower(name) like :name";
        SqlParameterSource params = new MapSqlParameterSource().addValue("name", "%" + name.toLowerCase() + "%");

        String selectByName = SELECT_SUBSYSTEM + " where " + conditions;
        String selectCountByName = "select count(*) from " + SUBSYSTEM_TABLE_NAME + " where " + conditions;

        List<Subsystem> result = getNamedParameterJdbcTemplate().query(selectByName, params, new SubsystemRowMapper());
        int count = getNamedParameterJdbcTemplate().queryForObject(selectCountByName, params, Integer.class);

        return new PagingResult<>(result, count);
    }


    private static final class SubsystemRowMapper implements RowMapper<Subsystem> {
        @Override
        public Subsystem mapRow(ResultSet rs, int index) throws SQLException {
            return Subsystem.builder()
                    .id(rs.getLong("id"))
                    .code(rs.getString("code"))
                    .name(rs.getString("name"))
                    .shortName(rs.getString("short_name"))
                    .build();
        }
    }
}
