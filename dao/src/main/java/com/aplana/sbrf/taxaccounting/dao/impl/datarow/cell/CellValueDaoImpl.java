package com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.datarow.DataRowDaoImplUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;


@Repository
@Transactional(readOnly=true)
public class CellValueDaoImpl extends AbstractDao implements CellValueDao {

	@Override
	@Transactional(readOnly=false)
	public void saveCellValue(Map<Long, DataRow<Cell>> rowIdMap) {
		
		Map<String, List<Object[]>> paramsMap = new HashMap<String, List<Object[]>>();
		for (String tableName : DataRowDaoImplUtils.CELL_VALUE_TABLE_NAMES) {
			paramsMap.put(tableName, new ArrayList<Object[]>());
		}
		
		
		for (Map.Entry<Long, DataRow<Cell>> rowId : rowIdMap.entrySet()) {
			for (String alias : rowId.getValue().keySet()) {
				Cell cell = rowId.getValue().getCell(alias);
				Column c = cell.getColumn();
				Object val = cell.getValue();
				if (val != null){
					String tableName = DataRowDaoImplUtils.getCellValueTableName(c);
					List<Object[]> batchList = paramsMap.get(tableName);
					batchList.add(new Object[]{rowId.getKey(), c.getId(), val});
				}
			}
		}
		
		for (String tableName : DataRowDaoImplUtils.CELL_VALUE_TABLE_NAMES) {
			List<Object[]> batchList = paramsMap.get(tableName);
			if (!batchList.isEmpty()) {
				getJdbcTemplate().batchUpdate(
						"insert into " + tableName
						+ " (row_id, column_id, value) values (?, ?, ?)", paramsMap.get(tableName));
			}
		}

	}

}
