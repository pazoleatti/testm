package com.aplana.sbrf.taxaccounting.dao.impl.refbook.main;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

class RefBookConfRowMapper implements RowMapper<RefBookConfListItem> {
    @Override
    public RefBookConfListItem mapRow(ResultSet rs, int index) throws SQLException {
        RefBookConfListItem result = new RefBookConfListItem();
        result.setId(SqlUtils.getLong(rs, "id"));
        result.setName(rs.getString("name"));
        result.setVisible(rs.getBoolean("visible"));
        result.setReadOnly(rs.getBoolean("read_only"));
        result.setRefBookType(RefBookType.get(SqlUtils.getInteger(rs, "type")));
        result.setRegionality(rs.getObject("REGION_ATTRIBUTE_ID") == null ? "Общий" : "Региональный");
        return result;
    }
}
