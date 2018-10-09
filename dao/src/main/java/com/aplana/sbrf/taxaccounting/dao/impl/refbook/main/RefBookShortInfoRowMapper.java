package com.aplana.sbrf.taxaccounting.dao.impl.refbook.main;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookShortInfo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class RefBookShortInfoRowMapper implements RowMapper<RefBookShortInfo> {
    @Override
    public RefBookShortInfo mapRow(ResultSet rs, int index) throws SQLException {
        RefBookShortInfo result = new RefBookShortInfo();
        result.setId(SqlUtils.getInteger(rs, "id"));
        result.setName(rs.getString("name"));
        result.setReadOnly(rs.getBoolean("read_only"));
        return result;
    }
}
