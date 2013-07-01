package com.aplana.sbrf.taxaccounting.dao.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
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
	public void removeRows(FormData fd, final List<DataRow<Cell>> rows) {
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		
		getJdbcTemplate().batchUpdate("delete from DATA_ROW where ID=? and TYPE=?", new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Long rowId = rows.get(i).getId();
				if (rowId == null){
					throw new IllegalArgumentException();
				}
				ps.setLong(1, rows.get(i).getId());
				ps.setInt(2, RT.ADD.getKey());
			}
			
			@Override
			public int getBatchSize() {
				return rows.size();
			}
			
		});
		
		getJdbcTemplate().batchUpdate("update DATA_ROW set TYPE=? where ID=? and TYPE=?", new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, RT.DEL.getKey());
				ps.setLong(2, rows.get(i).getId());
				ps.setInt(3, RT.SAME.getKey());
			}
			
			@Override
			public int getBatchSize() {
				return rows.size();
			}
		});

	}

	@Override
	public void removeRows(final FormData fd, final int idxFrom, final int idxTo) {
		if ((idxFrom < 1) || (idxTo < idxFrom)){
			throw new IllegalArgumentException();
		}
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		String idsSQL = "select ID from (select rownum as IDX, ID, TYPE from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId order by ORD) RR where IDX between :from and :to";
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("types", RT.rtsToKeys(new RT[]{RT.ADD, RT.SAME}));
		params.put("formDataId", fd.getId());
		params.put("from", idxFrom);
		params.put("to", idxTo);
		params.put("remType", RT.ADD.getKey());
		params.put("updType", RT.SAME.getKey());
		params.put("setType", RT.DEL.getKey());
		
		getNamedParameterJdbcTemplate().update("delete from DATA_ROW where ID in (" + idsSQL + ") and TYPE=:remType", params);
		getNamedParameterJdbcTemplate().update("update DATA_ROW set TYPE=:setType where ID in (" + idsSQL + ") and TYPE=:updType", params);

	}

	@Override
	public DataRow<Cell> insertRows(FormData fd, int index, List<DataRow<Cell>> rows) {
		// Если строка присутствует то ошибка (rowId должно быть null)
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRow<Cell> insertRowsAfter(FormData fd, DataRow<Cell> afterRow,
			List<DataRow<Cell>> rows) {
		// Получаем текущую и следующую строку
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

		public static Set<Integer> rtsToKeys(RT[] types) {
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
	 *         <a>http://conf.aplana.com/pages/viewpage.action?pageId=9588773&
	 *         focusedCommentId=9591393#comment-9591393</a>
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

			StringBuilder select = new StringBuilder("select rownum as IDX, R.ID as ID, R.ALIAS as A");
			StringBuilder from = new StringBuilder(" from DATA_ROW R");

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("formDataId", fd.getId());
			params.put("types", RT.rtsToKeys(types));

			for (Column c : fd.getFormColumns()) {
				params.put(String.format("column%sId", c.getId()), c.getId());
				String valueTableName = getCellValueTableName(c,
						CELL_VALUE_TABLE_NAMES);

				// Values
				select.append(String.format(", C%s.VALUE as V%s", c.getId(),
						c.getId()));
				from.append(String
						.format(" left join (select COLUMN_ID, ROW_ID, VALUE from %s N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) C%s on C%s.ROW_ID = R.ID and C%s.COLUMN_ID = :column%sId",
								valueTableName, c.getId(), c.getId(),
								c.getId(), c.getId()));
				// Styles
				select.append(String.format(", S%s.STYLE_ID as S%s", c.getId(),
						c.getId()));
				from.append(String
						.format(" left join (select COLUMN_ID, ROW_ID, STYLE_ID from CELL_STYLE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) S%s on S%s.ROW_ID = R.ID and S%s.COLUMN_ID = :column%sId",
								c.getId(), c.getId(), c.getId(), c.getId()));
				// Editables
				select.append(String.format(", E%s.EDIT as E%s", c.getId(),
						c.getId()));
				from.append(String
						.format(" left join (select COLUMN_ID, ROW_ID, 1 as EDIT from CELL_EDITABLE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) E%s on E%s.ROW_ID = R.ID and E%s.COLUMN_ID = :column%sId",
								c.getId(), c.getId(), c.getId(), c.getId()));
				// Span Info
				select.append(String.format(", SI%s.COLSPAN as CSI%s, SI%s.ROWSPAN as RSI%s", c.getId(),
						c.getId(), c.getId(), c.getId()));
				from.append(String
						.format(" left join (select COLUMN_ID, ROW_ID, COLSPAN, ROWSPAN from CELL_SPAN_INFO N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) SI%s on SI%s.ROW_ID = R.ID and SI%s.COLUMN_ID = :column%sId",
								c.getId(), c.getId(), c.getId(), c.getId()));

			}

			StringBuilder sql = new StringBuilder();
			sql.append(select)
					.append(from)
					.append(" where R.FORM_DATA_ID = :formDataId and R.TYPE in (:types) order by R.ORD");

			if (range != null) {
				sql.insert(0, "select * from(");
				sql.append(") where IDX between :from and :to");
				params.put("from", range.getOffset());
				params.put("to", range.getOffset() + range.getLimit());
			}

			return new Pair<String, Map<String, Object>>(sql.toString(), params);

		}

		@Override
		public DataRow<Cell> mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			List<Cell> cells = FormDataUtils.createCells(fd.getFormColumns(),
					fd.getFormStyles());
			for (Cell cell : cells) {
				// Values
				CellValueExtractor extr = getCellValueTableName(
						cell.getColumn(), CELL_VALUE_TABLE_EXTRACTORS);
				cell.setValue(extr.getValue(rs,
						String.format("V%s", cell.getColumn().getId())));
				// Styles
				BigDecimal styleId = rs.getBigDecimal(String.format("S%s", cell
						.getColumn().getId()));
				cell.setStyleId(styleId != null ? styleId.intValueExact()
						: null);
				// Editable
				cell.setEditable(rs.getBoolean(String.format("E%s", cell
						.getColumn().getId())));
				// Span Info
				int rowSpan = rs.getInt(String.format("RSI%s", cell.getColumn().getId()));
				cell.setRowSpan(rowSpan==0 ? 1 : rowSpan);
				int colSpan = rs.getInt(String.format("CSI%s", cell.getColumn().getId()));
				cell.setColSpan(colSpan==0 ? 1 : colSpan);
			}
			DataRow<Cell> dataRow = new DataRow<Cell>(rs.getString("A"), cells);
			dataRow.setId(rs.getLong("ID"));
			return dataRow;
		}
	}

	private static final String[] CELL_VALUE_TABLE_NAMES = { "NUMERIC_VALUE",
			"STRING_VALUE", "DATE_VALUE" };

	private static interface CellValueExtractor {
		public Object getValue(ResultSet rs, String columnLabel)
				throws SQLException;
	}

	private static final CellValueExtractor[] CELL_VALUE_TABLE_EXTRACTORS = {
			new CellValueExtractor() {
				@Override
				public Object getValue(ResultSet rs, String columnLabel)
						throws SQLException {
					return rs.getBigDecimal(columnLabel);
				}
			}, new CellValueExtractor() {
				@Override
				public Object getValue(ResultSet rs, String columnLabel)
						throws SQLException {
					return rs.getString(columnLabel);
				}
			}, new CellValueExtractor() {
				@Override
				public Object getValue(ResultSet rs, String columnLabel)
						throws SQLException {
					return rs.getDate(columnLabel);
				}
			} };

	private static <T> T getCellValueTableName(Column c, T[] objects) {
		if (c instanceof StringColumn) {
			return objects[1];
		} else if (c instanceof NumericColumn) {
			return objects[0];
		} else if (c instanceof DateColumn) {
			return objects[2];
		} else {
			throw new IllegalArgumentException();
		}
	}

}
