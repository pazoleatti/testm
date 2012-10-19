package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.util.OrderUtils;

@Repository
public class ScriptDaoImpl extends AbstractDao implements ScriptDao {
	private static final int CREATE = 1;
	private static final int CALC = 2;
	private static final int ROW = 3;

	private static class ScriptRecord {
		int id;
		int type;
		String body;
		String condition;
		String name;
		Integer order;
		
		ScriptRecord(int id, int type, String body) {
			this.id = id;
			this.type = type;
			this.body = body == null ? "" : body.trim();
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public void fillFormScripts(final Form form) {
		form.setCreateScript(null);
		form.getCalcScripts().clear();
		
		getJdbcTemplate().query(
			"select * from form_script where form_id = ? order by order",
			new Object[] { form.getId() },
			new int[] { Types.NUMERIC },
			new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					int type = rs.getInt("type");
					final Script script = new Script();
					script.setCondition(rs.getString("condition"));
					switch (type) {
					case CREATE:
						if (form.getCreateScript() != null) {
							throw new DaoException("Обнаружено несколько скриптов с типом CREATE");
						}						
						form.setCreateScript(script);
						break;
					case CALC:
						script.setRowScript(false);
						form.getCalcScripts().add(script);
						break;
					case ROW:						
						script.setRowScript(true);
						form.getCalcScripts().add(script);
						break;
					default:
						throw new IllegalArgumentException("Unknown script type: " + type);
					}
					script.setOrder(rs.getInt("order"));
					script.setName(rs.getString("name"));
					script.setId(rs.getInt("id"));
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
		
		final Set<Integer> removedScriptIds = new HashSet<Integer>();		
		removedScriptIds.addAll(jt.queryForList(
			"select id from form_script where form_id = ?",
			new Object[] { formId },
			new int[] { Types.NUMERIC },
			Integer.class
		));
		
		final List<ScriptRecord> oldScripts = new ArrayList<ScriptRecord>();
		final List<ScriptRecord> newScripts = new ArrayList<ScriptRecord>();
		
		Script createScript = form.getCreateScript(); 
		if (createScript != null && createScript.getBody() != null && !"".equals(createScript.getBody().trim())) {
			ScriptRecord scriptRecord = new ScriptRecord(createScript.getId(), CREATE, form.getCreateScript().getBody());
			if (createScript.getId() <= 0) {
				newScripts.add(scriptRecord);
			} else {
				oldScripts.add(scriptRecord);
				removedScriptIds.remove(createScript.getId());
			}
		}

		OrderUtils.reorder(form.getCalcScripts());
		for (Script calcScript: form.getCalcScripts()) {
			int type = calcScript.isRowScript() ? ROW : CALC;
			ScriptRecord rec = new ScriptRecord(calcScript.getId(), type, calcScript.getBody());
			rec.name = calcScript.getName();
			if (calcScript.getCondition() != null && !"".equals(calcScript.getCondition())) {
				rec.condition = calcScript.getCondition().trim();
			}
			rec.order = calcScript.getOrder();
			if (rec.id <= 0) {
				newScripts.add(rec);
			} else {
				removedScriptIds.remove(rec.id);
				oldScripts.add(rec);
			}
		}
		
		if (!newScripts.isEmpty()) {
			jt.batchUpdate(
				"insert into form_script (id, form_id, type, name, order, body, condition) " +
				"values (nextval for seq_form_script, " + formId + ", ?, ?, ?, ?, ?)",
				new BatchPreparedStatementSetter() {
					@Override
					public int getBatchSize() {
						return newScripts.size();
					}
	
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						ScriptRecord rec = newScripts.get(index);
						ps.setInt(1, rec.type);
						ps.setString(2, rec.name);
						ps.setObject(3, rec.order, Types.NUMERIC);
						ps.setString(4, rec.body);
						ps.setString(5, rec.condition);
					}
				}
			);
		}
		
		if (!oldScripts.isEmpty()) {
			jt.batchUpdate(
				"update form_script set (type, name, order, body, condition) = " +
				"(?, ?, ?, ?, ?) where id = ?",
				new BatchPreparedStatementSetter() {
					@Override
					public int getBatchSize() {
						return oldScripts.size();
					}
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						ScriptRecord rec = oldScripts.get(index);
						ps.setInt(1, rec.type);
						ps.setString(2, rec.name);
						ps.setObject(3, rec.order, Types.NUMERIC);
						ps.setString(4, rec.body);
						ps.setString(5, rec.condition);
						ps.setInt(6, rec.id);
					}
				}
			);
		}
		
		if (!removedScriptIds.isEmpty()) {
			jt.batchUpdate(
				"delete from form_script where id = ?",
				new BatchPreparedStatementSetter() {
					Iterator<Integer> iterator = removedScriptIds.iterator();
					@Override
					public void setValues(PreparedStatement ps, int index) throws SQLException {
						ps.setInt(1, iterator.next());
					}
					
					@Override
					public int getBatchSize() {
						return removedScriptIds.size();
					}
				}
			);
		}
	}
}
