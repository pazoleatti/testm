package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell.CellEditableDao;
import com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell.CellSpanInfoDao;
import com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell.CellStyleDao;
import com.aplana.sbrf.taxaccounting.dao.impl.datarow.cell.CellValueDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

@Repository
public class DataRowDaoImpl extends AbstractDao implements DataRowDao {
	
	@Autowired
	private CellEditableDao cellEditableDao;
	@Autowired
	private CellValueDao cellValueDao;
	@Autowired
	private CellStyleDao cellStyleDao;
	@Autowired
	private CellSpanInfoDao cellSpanInfoDao;

	@Override
	public List<DataRow<Cell>> getSavedRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return phisicalGetRows(fd,
				new TypeFlag[] { TypeFlag.DEL, TypeFlag.SAME }, filter, range);
	}

	@Override
	public int getSavedSize(FormData fd, DataRowFilter filter) {
		return phisicalGetSize(fd,
				new TypeFlag[] { TypeFlag.DEL, TypeFlag.SAME }, filter);
	}

	@Override
	public List<DataRow<Cell>> getRows(FormData fd, DataRowFilter filter,
			DataRowRange range) {
		return phisicalGetRows(fd,
				new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, filter, range);
	}

	@Override
	public int getSize(FormData fd, DataRowFilter filter) {
		return phisicalGetSize(fd,
				new TypeFlag[] { TypeFlag.ADD, TypeFlag.SAME }, filter);
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

		getJdbcTemplate().batchUpdate(
				"delete from DATA_ROW where ID=? and TYPE=?",
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						Long rowId = rows.get(i).getId();
						if (rowId == null) {
							throw new IllegalArgumentException();
						}
						ps.setLong(1, rows.get(i).getId());
						ps.setInt(2, TypeFlag.ADD.getKey());
					}

					@Override
					public int getBatchSize() {
						return rows.size();
					}

				});

		getJdbcTemplate().batchUpdate(
				"update DATA_ROW set TYPE=? where ID=? and TYPE=?",
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i)
							throws SQLException {
						ps.setInt(1, TypeFlag.DEL.getKey());
						ps.setLong(2, rows.get(i).getId());
						ps.setInt(3, TypeFlag.SAME.getKey());
					}

					@Override
					public int getBatchSize() {
						return rows.size();
					}
				});

	}

	@Override
	public void removeRows(final FormData fd, final int idxFrom, final int idxTo) {
		if ((idxFrom < 1) || (idxTo < idxFrom)) {
			throw new IllegalArgumentException();
		}
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE
		String idsSQL = "select ID from (select rownum as IDX, ID, TYPE from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId order by ORD) RR where IDX between :from and :to";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("types", TypeFlag.rtsToKeys(new TypeFlag[] { TypeFlag.ADD,
				TypeFlag.SAME }));
		params.put("formDataId", fd.getId());
		params.put("from", idxFrom);
		params.put("to", idxTo);
		params.put("remType", TypeFlag.ADD.getKey());
		params.put("updType", TypeFlag.SAME.getKey());
		params.put("setType", TypeFlag.DEL.getKey());

		getNamedParameterJdbcTemplate().update(
				"delete from DATA_ROW where ID in (" + idsSQL
						+ ") and TYPE=:remType", params);
		getNamedParameterJdbcTemplate().update(
				"update DATA_ROW set TYPE=:setType where ID in (" + idsSQL
						+ ") and TYPE=:updType", params);

	}
	
	@Override
	public void removeRows(FormData fd) {
		// Если строка помечена как ADD, то физическое удаление
		// Если строка помесена как DELETE, то ничего не делаем
		// Если строка помечена как SAME, то помечаем как DELETE

		getJdbcTemplate().update("delete from DATA_ROW where FORM_DATA_ID=? and TYPE=?", new Object[]{fd.getId(), TypeFlag.ADD.getKey()}); 
		getJdbcTemplate().update("update DATA_ROW set TYPE=? where FORM_DATA_ID=? and TYPE=?", new Object[]{TypeFlag.DEL.getKey(), fd.getId(), TypeFlag.SAME.getKey()});		
	}
	
	@Override
	public void saveRows(final FormData fd, final List<DataRow<Cell>> dataRows) {
		// Полностью чистим временный срез строк.
		removeRows(fd);
		
		if (dataRows.isEmpty()) {
			return;
		}
		
		// SBRFACCTAX-2201, SBRFACCTAX-2082
		FormDataUtils.cleanValueOners(dataRows);

		final long formDataId = fd.getId();

		BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int orderIndex)
					throws SQLException {
				DataRow<Cell> dr = dataRows.get(orderIndex);
				String rowAlias = dr.getAlias();
				ps.setLong(1, fd.getId());
				ps.setString(2, rowAlias);
				ps.setBigDecimal(3, BigDecimal.valueOf(orderIndex));
				ps.setInt(4, TypeFlag.ADD.getKey());
			}

			@Override
			public int getBatchSize() {
				return dataRows.size();
			}
		};

		getJdbcTemplate().batchUpdate(
				"insert into data_row (id, form_data_id, alias, ord, type) values (seq_data_row.nextval, ?, ?, ?, ?)", bpss);

		// Получаем массив идентификаторов строк, индекс записи в массиве
		// соответствует порядковому номеру строки (меньше на единицу)
		final List<Long> rowIds = getJdbcTemplate().queryForList(
				"select ID from DATA_ROW where TYPE in (?) and FORM_DATA_ID = ? order by ORD",
				new Object[] { TypeFlag.ADD.getKey(), formDataId }, new int[] { Types.NUMERIC, Types.NUMERIC },
				Long.class);

		Map<Long, DataRow<Cell>> rowIdMap = new HashMap<Long, DataRow<Cell>>();
		for (int i = 0; i < rowIds.size(); i ++) {
			rowIdMap.put(rowIds.get(i), dataRows.get(i));
		}

		cellValueDao.saveCellValue(rowIdMap);
		cellStyleDao.saveCellStyle(rowIdMap);
		cellSpanInfoDao.saveCellSpanInfo(rowIdMap);
		cellEditableDao.saveCellEditable(rowIdMap);
		
	}

	@Override
	public DataRow<Cell> insertRows(FormData fd, int index,
			List<DataRow<Cell>> rows) {
		// Получаем значение ORD для текущей (index) и следующей строки
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
	public void commit(FormData fd) {
		phisicalRemoveRows(fd, TypeFlag.DEL);
		phisicalUpdateRowsType(fd, TypeFlag.ADD, TypeFlag.SAME);
	}

	@Override
	public void rollback(FormData fd) {
		phisicalRemoveRows(fd, TypeFlag.ADD);
		phisicalUpdateRowsType(fd, TypeFlag.DEL, TypeFlag.SAME);
	}

	private void phisicalRemoveRows(FormData fd, TypeFlag type) {
		getJdbcTemplate().update(
				"delete from DATA_ROW where FORM_DATA_ID = ? and TYPE = ?",
				fd.getId(), type.getKey());
	}

	private void phisicalUpdateRowsType(FormData fd, TypeFlag fromType,
			TypeFlag toType) {
		getJdbcTemplate()
				.update("update DATA_ROW set TYPE = ? where FORM_DATA_ID = ? and TYPE = ?",
						toType.getKey(), fd.getId(), fromType.getKey());
	}

	private List<DataRow<Cell>> phisicalGetRows(FormData fd, TypeFlag[] types,
			DataRowFilter filter, DataRowRange range) {
		DataRowMapper dataRowMapper = new DataRowMapper(fd, types, filter,
				range);
		Pair<String, Map<String, Object>> sql = dataRowMapper.createSql();
		List<DataRow<Cell>> dataRows = getNamedParameterJdbcTemplate().query(sql.getFirst(),
				sql.getSecond(), dataRowMapper);
		// SBRFACCTAX-2082
		FormDataUtils.setValueOners(dataRows);
		return dataRows;
	}

	private int phisicalGetSize(FormData fd, TypeFlag[] types,
			DataRowFilter filter) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		return getNamedParameterJdbcTemplate()
				.queryForInt(
						"select count(ID) from DATA_ROW where FORM_DATA_ID = :formDataId and TYPE in (:types)",
						params);
	}
	
	private BigDecimal getOrd(FormData fd, TypeFlag[] types, int dataRowIndex){
		String sql = "select ORD from (select rownum as IDX, ORD from DATA_ROW where TYPE in (:types) and FORM_DATA_ID=:formDataId order by ORD) RR where IDX = :dataRowIndex";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		params.put("dataRowIndex", dataRowIndex);
		return DataAccessUtils.requiredSingleResult(getNamedParameterJdbcTemplate().queryForList(sql, params, BigDecimal.class));
	}
	
	private BigDecimal getOrd(FormData fd, TypeFlag[] types, DataRow<Cell> dataRow){
		String sql = "select ORD from DATA_ROW where ID = :dataRowId and TYPE in (:types) and FORM_DATA_ID=:formDataId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));
		params.put("dataRowId", fd.getId());
		return DataAccessUtils.requiredSingleResult(getNamedParameterJdbcTemplate().queryForList(sql, params, BigDecimal.class));
	}




	
	

}
