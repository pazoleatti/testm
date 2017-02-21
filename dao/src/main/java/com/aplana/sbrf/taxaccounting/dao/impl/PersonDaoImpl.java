package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.PersonDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс для работы со справочником физ. лиц, для специфики по дубликатам.
 * @author lhaziev
 *
 */

@Repository
public class PersonDaoImpl extends AbstractDao implements PersonDao {

    @Override
    public void setDuplicate(List<Long> recordIds, Long originalId) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("originalId", originalId);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId, old_id = record_id, old_status = status, status = -1 " +
                "where old_id is null and %s", SqlUtils.transformToSqlInStatement("record_id", recordIds)), valueMap);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId " +
                "where old_id is not null and %s", SqlUtils.transformToSqlInStatement("record_id", recordIds)), valueMap);
    }

    @Override
    public void changeRecordId(List<Long> recordIds, Long originalId) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("originalId", originalId);
        getNamedParameterJdbcTemplate().update(String.format("update ref_book_person set record_id = :originalId " +
                "where old_id is not null and %s", SqlUtils.transformToSqlInStatement("old_id", recordIds)), valueMap);
    }

    @Override
    public void setOriginal(List<Long> recordIds) {
        getJdbcTemplate().update(String.format("update ref_book_person set record_id = old_id, old_id = null, status = old_status, old_status = null " +
                "where %s", SqlUtils.transformToSqlInStatement("old_id", recordIds)));
    }

    @Override
    public Long getOriginal(Long recordId) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("recordId", recordId);
        return getNamedParameterJdbcTemplate().queryForObject("select id from ref_book_person where record_id = :recordId and old_id is null", valueMap, Long.class);
    }

    @Override
    public List<Long> getDuplicate(Long recordId) {
        Map<String, Object> valueMap =  new HashMap<String, Object>();
        valueMap.put("recordId", recordId);
        return getNamedParameterJdbcTemplate().query("select id from ref_book_person where record_id = :recordId and old_id is not null and old_status = 0", valueMap, new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getLong("id");
            }
        });
    }

}
