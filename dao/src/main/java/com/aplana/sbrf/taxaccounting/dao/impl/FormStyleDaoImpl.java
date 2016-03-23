package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormStyleDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

	private static final Log LOG = LogFactory.getLog(FormStyleDaoImpl.class);

	@Override
	public List<FormStyle> getFormStyles(int formTemplateId) {
		return getJdbcTemplate().query(
				"select alias, font_color, back_color, italic, bold from form_style where form_template_id = ?",
				new Object[] { formTemplateId },
				new int[] { Types.NUMERIC },
				new StyleDaoImpl.StyleMapper()
		);
	}

	@Override
	public Map<String, FormStyle> getAliasToFormStyleMap(int formTemplateId){
		Map<String, FormStyle> result = new HashMap<String, FormStyle>();
		List<FormStyle> formStyleList = getFormStyles(formTemplateId);
		for(FormStyle formStyle : formStyleList){
			result.put(formStyle.getAlias(), formStyle);
		}
		return result;
	}

	@Transactional(readOnly = false)
	@Override
	public void saveFormStyles(final FormTemplate formTemplate) {
		final int formTemplateId = formTemplate.getId();

		JdbcTemplate jt = getJdbcTemplate();

		final Set<String> styleToRemove = new HashSet<String>(jt.queryForList(
				"select alias from form_style where form_template_id = ?",
				new Object[] { formTemplateId },
				new int[] { Types.NUMERIC },
				String.class
		));

		final List<FormStyle> newStyles = new ArrayList<FormStyle>();
		final List<FormStyle> oldStyles = new ArrayList<FormStyle>();

		List<FormStyle> styles = formTemplate.getStyles();

		for (FormStyle style: styles) {
			if (!styleToRemove.contains(style.getAlias())) {
				newStyles.add(style);
			} else {
				oldStyles.add(style);
				styleToRemove.remove(style.getAlias());
			}
		}

		if(!styleToRemove.isEmpty()){
            final String[] alias = new String[1];
            Map<String, FormStyle> stylesMap = getAliasToFormStyleMap(formTemplateId);
			try {
                jt.batchUpdate(
                        "delete from form_style where alias = ? and form_template_id = ?",
                        new BatchPreparedStatementSetter() {

                            @Override
                            public void setValues(PreparedStatement ps, int index) throws SQLException {
                                alias[0] = iterator.next();
                                ps.setString(1, alias[0]);
                                ps.setInt(2, formTemplateId);
                            }

                            @Override
                            public int getBatchSize() {
                                return styleToRemove.size();
                            }

                            private Iterator<String> iterator = styleToRemove.iterator();
                        }
                );
            } catch (DataIntegrityViolationException e){
				LOG.error("", e);
                throw new DaoException("Обнаружено использование стиля с алиасом " + alias[0], e);
            }
		}
		if (!newStyles.isEmpty()) {
			jt.batchUpdate(
					"insert into form_style (alias, form_template_id, font_color, back_color, italic, bold) " +
							"values (?, " + formTemplateId + ", ?, ?, ?, ?)",
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
		}

		if(!oldStyles.isEmpty()){
			jt.batchUpdate(
					"update form_style set font_color = ?, back_color = ?, italic = ?, bold = ? " +
							"where alias = ? and form_template_id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							FormStyle formStyle = oldStyles.get(index);
							ps.setInt(1, formStyle.getFontColor().getId());
							ps.setInt(2, formStyle.getBackColor().getId());
							ps.setInt(3, formStyle.isItalic() ? 1 : 0);
							ps.setInt(4, formStyle.isBold() ? 1 : 0);
                            ps.setString(5, formStyle.getAlias());
							ps.setInt(6, formTemplateId);
						}

						@Override
						public int getBatchSize() {
							return oldStyles.size();
						}
					}
			);
		}
	}
}