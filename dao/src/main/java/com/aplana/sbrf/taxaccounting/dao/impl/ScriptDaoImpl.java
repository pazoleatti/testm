package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.RowScript;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.model.ValueScript;

@Repository
public class ScriptDaoImpl extends AbstractDao implements ScriptDao {
	private static final int CREATE = 1;
	private static final int CALC = 2;
	private static final int ROW = 3;
	private static final int VALUE = 4;

	private static class ScriptRecord {
		int type;
		String script;
		String condition;
		String name;
		Integer columnId;
		
		ScriptRecord(int type, String script) {
			this.type = type;
			this.script = script;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public void fillFormScripts(final Form form) {
		form.setCalcScript(null);
		form.setCreateScript(null);
		form.getRowScripts().clear();
		for (Column col: form.getColumns()) {
			col.setValueScript(null);
		}
		
		getJdbcTemplate().query(
			"select * from form_script where form_id = ?",
			new Object[] { form.getId() },
			new int[] { Types.NUMERIC },
			new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					int type = rs.getInt("type");
					
					Script script;
					switch (type) {
					case CREATE:
						if (form.getCreateScript() != null) {
							throw new DaoException("Обнаружено несколько скриптов с типом CREATE");
						}						
						script = new Script();
						form.setCreateScript(script);
						break;
					case CALC:
						if (form.getCalcScript() != null) {
							throw new DaoException("Обнаружено несколько скриптов с типом CALC");
						}
						script = new Script();
						form.setCalcScript(script);
						break;
					case ROW:
						RowScript rowScript = new RowScript();
						rowScript.setName(rs.getString("name"));
						rowScript.setCondition(rs.getString("condition"));
						form.getRowScripts().add(rowScript);
						script = rowScript;
						break;
					case VALUE:
						ValueScript valueScript = new ValueScript();
						valueScript.setCondition(rs.getString("condition"));
						int columnId = rs.getInt("column_id");
						form.getColumn(columnId).setValueScript(valueScript);
						script = valueScript;
						break;
					default:
						throw new IllegalArgumentException("Unknown script type: " + type);
					}
					script.setBody(rs.getString("body"));						
				}
			}
		);
	}
	
	@Override
	@Transactional(readOnly = false)
	public void saveFormScripts(Form form) {
		int formId = form.getId();
		
		JdbcTemplate jt = getJdbcTemplate();
		jt.update(
			"delete from form_script where form_id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC }
		);
		
		final List<ScriptRecord> scripts = new ArrayList<ScriptRecord>();
		if (form.getCreateScript() != null && form.getCreateScript().getBody() != null) {
			scripts.add(new ScriptRecord(CREATE, form.getCreateScript().getBody()));
		}
		if (form.getCalcScript() != null && form.getCalcScript().getBody() != null) {
			scripts.add(new ScriptRecord(CALC, form.getCalcScript().getBody()));
		}
		for (RowScript rowScript: form.getRowScripts()) {
			ScriptRecord rec = new ScriptRecord(ROW, rowScript.getBody());
			rec.name = rowScript.getName();
			rec.condition = rowScript.getCondition();
			scripts.add(rec);
		}
		for (Column col: form.getColumns()) {
			ValueScript valScript = col.getValueScript();
			if (valScript != null) {
				ScriptRecord rec = new ScriptRecord(VALUE, valScript.getBody());
				rec.condition = valScript.getCondition();
				scripts.add(rec);
			}
		}
		
		jt.batchUpdate(
			"insert into form_script (form_id, type, name, column_id, body, condition) " +
			"values (" + formId + ", ?, ?, ?, ?, ?)",
			new BatchPreparedStatementSetter() {
				@Override
				public int getBatchSize() {
					return scripts.size();
				}

				@Override
				public void setValues(PreparedStatement ps, int index) throws SQLException {
					ScriptRecord rec = scripts.get(index);
					ps.setInt(1, rec.type);
					ps.setString(2, rec.name);
					if (rec.columnId == null) {
						ps.setNull(3, Types.NUMERIC);	
					} else {
						ps.setInt(3, rec.columnId.intValue());
					}
					ps.setString(4, rec.script);
					ps.setString(5, rec.condition);
				}
			}
		);
	}
}
