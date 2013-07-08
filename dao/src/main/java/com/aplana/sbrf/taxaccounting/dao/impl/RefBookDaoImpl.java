package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.AttributeType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
			result.setAttributeType(RefBookAttributeType.values()[rs.getInt("type") - 1]);
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
		return getJdbcTemplate().query(getRefBookSql(refBookId, version), new RefBookValueMapper(refBookId));
	}

	private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yy");

	private static final String WITH_STATEMENT =
			"with t as (select\n" +
					"  max(version) version, record_id\n" +
					"from\n" +
					"  ref_book_record\n" +
					"where\n" +
					"  ref_book_id = %d and version <= to_date('%s', 'dd.mm.yy')\n" +
					"group by\n" +
					"  record_id)\n";

	/**
	 * Динамически формирует запрос для справочника
	 * @param refBookId
	 * @param version
	 * @return
	 */
	private String getRefBookSql(Long refBookId, Date version) {
		RefBook refBook = get(refBookId);
		StringBuilder fromSql = new StringBuilder("from\n");
		fromSql.append("  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)\n");

		StringBuilder sql = new StringBuilder(String.format(WITH_STATEMENT, refBookId, sdf.format(version)));
		sql.append("select\n");
		sql.append("  r.id as id,\n");
		List<RefBookAttribute> attributes = refBook.getAttributes();
		for (int i = 0; i < attributes.size(); i++) {
			RefBookAttribute attribute = attributes.get(i);
			sql.append("  a");
			sql.append(i);
			sql.append(".");
			sql.append(attribute.getAttributeType().toString());
			sql.append("_value as ");
			sql.append(attribute.getAlias());
			if (i < attributes.size() - 1) {
				sql.append(",\n");
			}

			fromSql.append("  left join ref_book_value a");
			fromSql.append(i);
			fromSql.append(" on a");
			fromSql.append(i);
			fromSql.append(".record_id = r.id and a");
			fromSql.append(i);
			fromSql.append(".attribute_id = ");
			fromSql.append(attribute.getId());
			fromSql.append("\n");
		}
		sql.append(fromSql);
		sql.append("where\n  r.ref_book_id = ");
		sql.append(refBookId);
		sql.append("\n");
		sql.append("and\nstatus <> -1");
		return sql.toString();
	}

	private class RefBookValueMapper implements RowMapper<Map<String, RefBookValue>> {
		public RefBookValueMapper(Long refBookId) {

		}
		public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
			Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
			//TODO
			return result;
		}
	}

}