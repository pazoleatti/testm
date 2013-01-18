package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class FormStyleDaoImpl extends AbstractDao implements FormStyleDao {

	private final static class FormStyleMapper implements RowMapper<FormStyle> {
		public FormStyle mapRow(ResultSet rs, int index) throws SQLException {
			final FormStyle result = new FormStyle();
			result.setId(rs.getInt("id"));
			result.setAlias(rs.getString("alias"));
			result.setFontColor(Color.fromId(rs.getInt("font_color")));
			result.setBackColor(Color.fromId(rs.getInt("back_color")));
			result.setItalic(rs.getBoolean("italic"));
			result.setBold(rs.getBoolean("bold"));
			return result;
		}
	}

	@Override
	public List<FormStyle> getFormStyles(int formId) {
		return getJdbcTemplate().query(
				"select * from form_style where form_id = ?",
				new Object[] { formId },
				new int[] { Types.NUMERIC },
				new FormStyleMapper()
		);
	}

	@Override
	public Map<Integer, FormStyle> getIdToFormStyleMap(int formId){
		Map<Integer, FormStyle> result = new HashMap<Integer, FormStyle>();
		List<FormStyle> formStyleList = getFormStyles(formId);
		for(FormStyle formStyle : formStyleList){
			result.put(formStyle.getId(), formStyle);
		}
		return result;
	}

	@Override
	public Map<String, FormStyle> getAliasToFormStyleMap(int formId){
		Map<String, FormStyle> result = new HashMap<String, FormStyle>();
		List<FormStyle> formStyleList = getFormStyles(formId);
		for(FormStyle formStyle : formStyleList){
			result.put(formStyle.getAlias(), formStyle);
		}
		return result;
	}

	@Transactional(readOnly = false)
	@Override
	public void saveFormStyles(final FormTemplate form) {
		int formId = form.getId();

		JdbcTemplate jt = getJdbcTemplate();

		final Set<Integer> removedStyles = new HashSet<Integer>(jt.queryForList(
				"select id from form_style where form_id = ?",
				new Object[] { formId },
				new int[] { Types.NUMERIC },
				Integer.class
		));

		final List<FormStyle> newStyles = new ArrayList<FormStyle>();
		final List<FormStyle> oldStyles = new ArrayList<FormStyle>();

		List<FormStyle> styles = form.getStyles();

		for (FormStyle style: styles) {
			if (style.getId() == null) {
				newStyles.add(style);
			} else {
				oldStyles.add(style);
				removedStyles.remove(style.getId());
			}
		}

		if(!removedStyles.isEmpty()){
			jt.batchUpdate(
					"delete from form_style where id = ?",
					new BatchPreparedStatementSetter() {

						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							ps.setInt(1, iterator.next());
						}

						@Override
						public int getBatchSize() {
							return removedStyles.size();
						}

						private Iterator<Integer> iterator = removedStyles.iterator();
					}
			);
		}

		jt.batchUpdate(
				"insert into form_style (id, alias, form_id, font_color, back_color, italic, bold) " +
						"values (seq_form_style.nextval, ?, " + formId + ", ?, ?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						FormStyle formStyle = newStyles.get(index);
						ps.setString(1, formStyle.getAlias());
						ps.setInt(2, formStyle.getFontColor().getId());
						ps.setInt(3, formStyle.getBackColor().getId());
						ps.setInt(4, formStyle.isItalic() ? 1 : 0);
						ps.setInt(5, formStyle.isBold() ? 1 : 0);
					}

					@Override
					public int getBatchSize() {
						return newStyles.size();
					}
				}
		);

		if(!oldStyles.isEmpty()){
			jt.batchUpdate(
					"update form_style set alias = ?, font_color = ?, back_color = ?, italic = ?, bold = ? " +
							"where id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							FormStyle formStyle = oldStyles.get(index);
							ps.setString(1, formStyle.getAlias());
							ps.setInt(2, formStyle.getFontColor().getId());
							ps.setInt(3, formStyle.getBackColor().getId());
							ps.setInt(4, formStyle.isItalic() ? 1 : 0);
							ps.setInt(5, formStyle.isBold() ? 1 : 0);
							ps.setInt(6, formStyle.getId());
						}

						@Override
						public int getBatchSize() {
							return oldStyles.size();
						}
					}
			);
		}
		jt.query(
				"select id, alias from form_style where form_id = " + formId,
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String alias = rs.getString("alias");
						int columnId = rs.getInt("id");
						form.getStyle(alias).setId(columnId);
					}
				}
		);
	}
}
