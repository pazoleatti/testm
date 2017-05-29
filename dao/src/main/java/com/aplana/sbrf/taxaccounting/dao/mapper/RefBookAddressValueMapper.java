package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RefBookAddressValueMapper extends RefBookAbstractValueMapper{

    public RefBookAddressValueMapper(RefBook refBook) {
        this.refBook = refBook;
    }

    private static final List<String> aliasList = Arrays.asList("REGION_CODE", "POSTAL_CODE", "DISTRICT", "CITY", "LOCALITY", "STREET", "HOUSE", "BUILD", "APPARTMENT");

    public static void addAddressAttribute(Map<String, RefBookValue> result) {
        List<String> attributeValues = new ArrayList<String>();
        if (result.get("ADDRESS_TYPE").getNumberValue().intValue() == 0) {
            for (String alias : aliasList) {
                attributeValues.add(result.get(alias).getStringValue() == null ? "" : result.get(alias).getValue().toString());
            }
            result.put("ADDRESS_FULL", new RefBookValue(RefBookAttributeType.STRING, StringUtils.join(attributeValues.toArray(), ", ", null)));
        } else {
            result.put("ADDRESS_FULL", result.get("ADDRESS"));
        }
    }

    @Override
    public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
        Map<String, RefBookValue> result = super.mapRow(rs, index);
        addAddressAttribute(result);
        return result;
    }

    @Override
    protected Map<String, RefBookValue> createResult(ResultSet rs) throws SQLException {
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, SqlUtils.getLong(rs, RefBook.RECORD_ID_ALIAS)));
        return result;
    }
}
