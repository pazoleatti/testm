package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.RefBookDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 04.07.13 18:48
 */
@Repository
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

	@Override
	public List<RefBook> getAll() {
		return getJdbcTemplate().query(
			"select id, name from ref_book order by name",
			new RefBookRowMapper());
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
	public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
															  String filter, RefBookAttribute sortAttribute) {
		String sql = getRefBookSql(refBookId, version, sortAttribute);
		RefBook refBook = get(refBookId);
		List<Map<String, RefBookValue>> records = getJdbcTemplate().query(sql, new RefBookValueMapper(refBook));
		PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
		result.setRecords(records);
		result.setTotalRecordCount(1000); //TODO: не реализовано (Marat Fayzullin 2013-07-10)
		return result;
	}

	@Override
	public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
		String sql = getRefBookRecordSql(refBookId, recordId);
		RefBook refBook = get(refBookId);
		return getJdbcTemplate().queryForObject(sql, new RefBookValueMapper(refBook));
	}

	private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy");

	private static final String WITH_STATEMENT =
			"with t as (select\n" +
					"  max(version) version, record_id\n" +
					"from\n" +
					"  ref_book_record\n" +
					"where\n" +
					"  ref_book_id = %d and version <= to_date('%s', 'DD.MM.YYYY')\n" +
					"group by\n" +
					"  record_id)\n";

	/**
	 * Динамически формирует запрос для справочника
	 * @param refBookId код справочника
	 * @param version дата актуальности данных справочника
	 * @param sortAttribute сортируемый столбец. Может быть не задан
	 * @return
	 */
	private String getRefBookSql(Long refBookId, Date version, RefBookAttribute sortAttribute) {
		RefBook refBook = get(refBookId);
		List<RefBookAttribute> attributes = refBook.getAttributes();

		if (sortAttribute != null && !attributes.contains(sortAttribute)) {
			throw new IllegalArgumentException(String.format("Reference book (id=%d) doesn't contains attribute \"%s\"",
					refBookId, sortAttribute.getAlias()));
		}

		StringBuilder fromSql = new StringBuilder("\nfrom\n");
		fromSql.append("  ref_book_record r join t on (r.version = t.version and r.record_id = t.record_id)\n");

		StringBuilder sql = new StringBuilder(String.format(WITH_STATEMENT, refBookId, sdf.format(version)));
		sql.append("select\n");
		sql.append("  r.id as ");
		sql.append(RefBook.RECORD_ID_ALIAS);
		sql.append(",\n");
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
		sql.append(" and\n  status <> -1\n");
		if (sortAttribute != null) {
			sql.append("order by\n");
			sql.append(sortAttribute.getAlias());
		}
		return sql.toString();
	}

	/**
	 * Динамически формирует запрос для справочника
	 * @param refBookId код справочника
	 * @param recordId код строки справочника
	 * @return
	 */
	private String getRefBookRecordSql(Long refBookId, Long recordId) {
		RefBook refBook = get(refBookId);
		StringBuilder fromSql = new StringBuilder("\nfrom\n");
		fromSql.append("  ref_book_record r\n");

		StringBuilder sql = new StringBuilder();
		sql.append("select\n");
		sql.append("  r.id as ");
		sql.append(RefBook.RECORD_ID_ALIAS);
		sql.append(",\n");
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
		sql.append("where\n  r.id = ");
		sql.append(recordId);
		return sql.toString();
	}

	private class RefBookValueMapper implements RowMapper<Map<String, RefBookValue>> {

		private final RefBook refBook;

		public RefBookValueMapper(RefBook refBook) {
			this.refBook = refBook;
		}
		public Map<String, RefBookValue> mapRow(ResultSet rs, int index) throws SQLException {
			Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
			result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, rs.getLong(1)));
			List<RefBookAttribute> attributes = refBook.getAttributes();
			for (int i = 0; i < attributes.size(); i++) {
				RefBookAttribute attribute = attributes.get(i);
				Object value = null;
				switch (attribute.getAttributeType()) {
					case STRING: {
						value = rs.getString(i + 2);
					}
					break;
					case NUMBER: {
						value = rs.getDouble(i + 2);
					}
					break;
					case DATE: {
						value = rs.getDate(i + 2);
					}
					break;
					case REFERENCE: {
						value = rs.getLong(i + 2);
					}
					break;
				}
				result.put(attribute.getAlias(), new RefBookValue(attribute.getAttributeType(), value));
			}
			return result;
		}
	}

	private static final String RECORD_VERSION =
		"select\n"+
		"  version\n"+
		"from\n"+
		"  ref_book_record\n"+
		"where\n"+
		"  ref_book_id = %d and\n" +
		"  version >= to_date('%s', 'DD.MM.YYYY') and\n"+
		"  version <= to_date('%s', 'DD.MM.YYYY')\n"+
		"group by\n"+
		"  version\n"+
		"order by\n" +
		"  version";
	@Override
	public List<Date> getVersions(Long refBookId, Date startDate, Date endDate) {
		String sql = String.format(RECORD_VERSION, refBookId, sdf.format(startDate), sdf.format(endDate));
		return getJdbcTemplate().query(sql, new RowMapper<Date>() {

			@Override
			public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getDate(1);
			}
		});
	}

	@Override
	public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return null; //TODO: не реализовано (Marat Fayzullin 2013-07-10)
	}
}