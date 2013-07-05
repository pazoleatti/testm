package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 18:48
 */
@Repository("refBookDao")
public class RefBookDaoImpl extends AbstractDao implements RefBookDao {

	@Override
	public RefBook get(Long refBookId) {
		try {
			return getJdbcTemplate().queryForObject(
				"select id, name from ref_book where id = ?",
				new Object[] {refBookId}, new int[] { Types.NUMERIC },
				new RefBookRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format("Не найден справочник с id = %d", refBookId));
		}
	}

	/**
	 * Настройка маппинга для справочника
	 */
	private class RefBookRowMapper implements RowMapper<RefBook> {
		public RefBook mapRow(ResultSet rs, int index) throws SQLException {
			RefBook result = new RefBook();
			result.setId(rs.getLong("id"));
			result.setName(rs.getString("name"));
			result.setAttributes(getAttributes(result.getId()));
			return result;
		}
	}

	/**
	 * По коду справочника возвращает набор его атрибутов
	 * @param refBookId код справочника
	 * @return набор атрибутов
	 */
	private List<RefBookAttribute> getAttributes(Long refBookId) {
		try {
			return getJdbcTemplate().query(
					"select id, name, alias, type, reference_id, attribute_id, visible, precision, width " +
							"from ref_book_attribute where ref_book_id = ? order by ord",
					new Object[] {refBookId}, new int[] {Types.NUMERIC},
					new RefBookAttributeRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(String.format("Не найдены атрибуты для справочника с id = %d", refBookId));
		}
	}

	/**
	 * Настройка маппинга для атрибутов справочника
	 */
	private class RefBookAttributeRowMapper implements RowMapper<RefBookAttribute> {
		public RefBookAttribute mapRow(ResultSet rs, int index) throws SQLException {
			RefBookAttribute result = new RefBookAttribute();
			result.setId(rs.getLong("id"));
			result.setName(rs.getString("name"));
			result.setAlias(rs.getString("alias"));
			result.setAttributeType(RefBookAttributeType.values()[rs.getInt("type")]);
			result.setRefBookId(rs.getLong("reference_id"));
			result.setRefBookAttributeId(rs.getLong("attribute_id"));
			result.setVisible(rs.getBoolean("visible"));
			result.setPrecision(rs.getInt("precision"));
			result.setWidth(rs.getInt("width"));
			return result;
		}
	}

	@Override
	public List<Map<String, RefBookValue>> getData(Long refBookId, Date version) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

}
