package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RefBookCalendarValueMapper extends RefBookAbstractValueMapper{

    public RefBookCalendarValueMapper(RefBook refBook) {
        this.refBook = refBook;
    }

    @Override
    public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
        return super.mapRow(rs, index);
    }

    @Override
    protected Map<String, RefBookValue> createResult(ResultSet rs) throws SQLException {
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        return result;
    }
}
