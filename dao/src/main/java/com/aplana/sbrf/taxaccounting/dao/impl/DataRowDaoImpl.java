package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

@Repository
public class DataRowDaoImpl extends AbstractDao implements DataRowDao {

	@Override
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return phisicalGetRows(fd, new RT[] { RT.DEL, RT.SAME }, filter, range);
	}

	@Override
	public int getSavedSize(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return phisicalGetRows(fd, new RT[] { RT.ADD, RT.SAME }, filter, range);
	}

	@Override
	public int getSize(FormData fd, DataRowFilter filter, DataRowRange range) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateRows(FormData fd, List<DataRow<Cell>> rows) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRow(FormData fd, DataRow<Cell> row) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRow(FormData fd, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataRow<Cell> insertRow(FormData fd, int index, DataRow<Cell> row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRow<Cell> insertRowAfter(FormData fd, DataRow<Cell> afterRow,
			DataRow<Cell> row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(FormData fd) {
		phisicalRemoveRows(fd, RT.DEL);
		phisicalUpdateRowsType(fd, RT.ADD, RT.SAME);
	}

	@Override
	public void cancel(FormData fd) {
		phisicalRemoveRows(fd, RT.ADD);
		phisicalUpdateRowsType(fd, RT.DEL, RT.SAME);
	}

	private void phisicalRemoveRows(FormData fd, RT type) {
		getJdbcTemplate().update(
				"delete from DATA_ROW where FORM_DATA_ID = ? and TYPE = ?",
				fd.getId(), type.getKey());
	}

	private void phisicalUpdateRowsType(FormData fd, RT fromType, RT toType) {
		getJdbcTemplate()
				.update("update DATA_ROW set TYPE = ? where FORM_DATA_ID = ? and TYPE = ?",
						toType.getKey(), fd.getId(), fromType.getKey());
	}

	private List<DataRow<Cell>> phisicalGetRows(FormData fd, RT[] types,
			DataRowFilter filter, DataRowRange range) {
		DataRowMapper mapper = new DataRowMapper(fd, types, filter, range);
		Pair<String, Map<String, Object>> sql = mapper.createSql();
		System.out.println(sql.getFirst());
		System.out.println(sql.getSecond());
		return getNamedParameterJdbcTemplate().query(sql.getFirst(),
				sql.getSecond(), mapper);
	}

	/**
	 * DataRow type
	 * 
	 * @author sgoryachkin
	 */
	private static enum RT {
		// Строка добавлена
		DEL(-1),
		// Строка удалена
		ADD(+1),
		// Строка не изменялась
		SAME(0);

		private int key;

		private RT(int key) {
			this.key = key;
		}

		public int getKey() {
			return key;
		}
		
		public static Set<Integer> rtsToKeys(RT[] types){
			Set<Integer> result = new HashSet<Integer>();
			for (RT rt : types) {
				result.add(rt.getKey());
			}
			return result;
		}
	}

	/**
	 * @author sgoryachkin
	 * 
	 *         <a>http://conf.aplana.com/pages/viewpage.action?pageId=9588773&focusedCommentId=9591393#comment-9591393</a>
	 */
	private static class DataRowMapper implements RowMapper<DataRow<Cell>> {

		private FormData fd;
		private DataRowRange range;
		private RT[] types;

		public DataRowMapper(FormData fd, RT[] types, DataRowFilter filter,
				DataRowRange range) {
			this.fd = fd;
			this.types = types;
			this.range = range;
		}

		public Pair<String, Map<String, Object>> createSql() {

			StringBuilder select = new StringBuilder("select R.ALIAS as A");
			StringBuilder from = new StringBuilder(" from DATA_ROW R");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("formDataId", fd.getId());
			params.put("types", RT.rtsToKeys(types));

			for (Column c : fd.getFormColumns()) {
				params.put(String.format("column%sId", c.getId()), c.getId());
				String valueTableName = getCellValueTableName(c);

				select.append(String.format(", C%s.VALUE as V%s", c.getId(),
						c.getId()));

				from.append(String
						.format(" left join (select COLUMN_ID, ROW_ID, VALUE from %s n join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) C%s on C%s.ROW_ID = R.ID and C%s.COLUMN_ID = :column%sId",
								valueTableName, c.getId(), c.getId(),
								c.getId(), c.getId()));

			}

			StringBuilder sql = new StringBuilder();
			sql.append(select)
					.append(from)
					.append(" where R.FORM_DATA_ID = :formDataId and R.TYPE in (:types) order by R.ORD");

			if (range != null) {
				sql.insert(0, "select rownum IDX, * from(");
				sql.append(") IDX between :from and :to");
				params.put("from", range.getOffset());
				params.put("to", range.getOffset() + range.getLimit());
			}

			return new Pair<String, Map<String, Object>>(sql.toString(), params);

		}

		@Override
		public DataRow<Cell> mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			return fd.createDataRow();
		}

	}

	private static final String[] CELL_VALUE_TABLE_NAMES = { "NUMERIC_VALUE",
			"STRING_VALUE", "DATE_VALUE" };

	private static String getCellValueTableName(Column c) {
		if (c instanceof StringColumn) {
			return CELL_VALUE_TABLE_NAMES[1];
		} else if (c instanceof NumericColumn) {
			return CELL_VALUE_TABLE_NAMES[0];
		} else if (c instanceof DateColumn) {
			return CELL_VALUE_TABLE_NAMES[2];
		} else {
			throw new IllegalArgumentException();
		}
	}

}
