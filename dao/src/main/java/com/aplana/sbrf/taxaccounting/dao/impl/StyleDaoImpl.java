package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.StyleDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 22.03.2016 15:34
 */
@Repository
public class StyleDaoImpl extends AbstractDao implements StyleDao  {

	private static final String CACHE_NAME = "PermanentData";
	private static final String SELECT_SQL_ALL = "SELECT alias, font_color, back_color, italic, bold FROM style ORDER BY alias";
	private static final String SELECT_SQL = "SELECT alias, font_color, back_color, italic, bold FROM style WHERE alias = :alias";
	private static final String GET_ERROR_MESSAGE = "Не найден стиль по указанному псевдониму \"%s\"";

	private static final Log LOG = LogFactory.getLog(StyleDaoImpl.class);

	public final static class StyleMapper implements RowMapper<FormStyle> {
		@Override
		public FormStyle mapRow(ResultSet rs, int index) throws SQLException {
			final FormStyle result = new FormStyle();
			result.setAlias(rs.getString("alias"));
			result.setFontColor(Color.getById(SqlUtils.getInteger(rs,"font_color")));
			result.setBackColor(Color.getById(SqlUtils.getInteger(rs,"back_color")));
			result.setItalic(rs.getBoolean("italic"));
			result.setBold(rs.getBoolean("bold"));
			return result;
		}
	}

	@Override
	@Cacheable(value = CACHE_NAME, key = "'Style_' + #alias")
	public FormStyle get(String alias) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("alias", alias);
		try {
			return getNamedParameterJdbcTemplate().queryForObject(SELECT_SQL, params, new StyleMapper());
		} catch (Exception e) {
			throw new DaoException(String.format(GET_ERROR_MESSAGE, alias), e);
		}
	}

	@Override
	@Cacheable(value = CACHE_NAME, key = "'Style_all'")
	public List<FormStyle> getAll() {
		return getNamedParameterJdbcTemplate().query(SELECT_SQL_ALL, new StyleMapper());
	}
}