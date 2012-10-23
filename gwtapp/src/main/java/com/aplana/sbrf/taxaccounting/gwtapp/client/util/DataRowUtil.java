package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;

public class DataRowUtil {
	public static Map<String, Object> dataRowToMap(DataRow row) {
		Map<String, Object> map = new HashMap<String, Object>();
		String alias = row.getAlias();
		if (alias != null) {
			map.put("alias", row.getAlias());
		}
		map.putAll(row);
		return map;
	}

	public static List<Map<String, Object>> dataRowsToListOfMap(List<DataRow> rows) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (DataRow row: rows) {
			result.add(dataRowToMap(row));
		}
		return result;
	}

	public static void addRowToFormData(FormData formData, Map<String, Object> rowDataMap) {
		String alias = (String)rowDataMap.get("alias");
		rowDataMap.remove("alias");
		DataRow row = formData.appendDataRow(alias);
		for (Map.Entry<String, Object> entry: rowDataMap.entrySet()) {
			String columnAlias = entry.getKey();
			Object value = entry.getValue();
			row.put(columnAlias, value);
		}
	}
}
