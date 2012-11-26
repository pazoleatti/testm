package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

@Repository
public class ScriptDaoImpl extends AbstractDao implements ScriptDao {
	@SuppressWarnings("UnusedDeclaration")
	private static final Logger log = Logger.getLogger(ScriptDaoImpl.class.getName());

	@Override
	@Transactional(readOnly = true)
	public void fillFormScripts(final FormTemplate form) {
		final Map<Integer, Script> scriptMap = new HashMap<Integer, Script>();

		form.clearScripts();

		getJdbcTemplate().query(
				"select * from form_script where form_id = ? order by ord",
				new Object[]{form.getId()},
				new int[]{Types.NUMERIC},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						final Script script = new Script();
						script.setCondition(rs.getString("condition"));
						script.setName(rs.getString("name"));
						script.setId(rs.getInt("id"));
						script.setBody(rs.getString("body"));
						script.setRowScript(rs.getInt("per_row") != 0);
						scriptMap.put(script.getId(), script);
						form.addScript(script);
					}
				}
		);

		getJdbcTemplate().query(
				"select es.event_code, es.script_id from event_script es join form_script fs on es.script_id=fs.id where fs.form_id = ? order by es.event_code, es.ord",
				new Object[]{form.getId()}, new int[]{Types.NUMERIC},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						form.addEventScript(FormDataEvent.getByCode(rs.getInt("event_code")), scriptMap.get(rs.getInt("script_id")));
					}
				}
		);
	}

	@Override
	@Transactional(readOnly = false)
	public void saveFormScripts(FormTemplate form) {
		Set<Integer> removedScriptIds = selectFormScriptIds(form.getId());

		// Clear joins between scripts and events.
		deleteEventJoins(form);

		List<Script> oldScripts = new ArrayList<Script>();
		List<Script> newScripts = new ArrayList<Script>();

		for (Script script : form.getScripts()) {
			if (script.getId() <= 0) {
				newScripts.add(script);
			} else {
				removedScriptIds.remove(script.getId());
				oldScripts.add(script);
			}
		}

		// Insert new scripts
		insertScripts(form, newScripts);

		// Update old scripts
		updateScripts(form, oldScripts);

		// Delete deleted scripts
		deleteScripts(removedScriptIds);

		// Add joins between scripts and events
		insertEventJoins(form);
	}

	/**
	 * Create joins between events and scripts of form.
	 *
	 * @param form form template
	 */
	private void insertEventJoins(FormTemplate form) {
		Map<FormDataEvent, List<Script>> eventScripts = form.getEventScripts();
		if (eventScripts != null) {
			final List<Object[]> args = new ArrayList<Object[]>();
			for (Map.Entry<FormDataEvent, List<Script>> entry : eventScripts.entrySet()) {
				FormDataEvent event = entry.getKey();
				List<Script> scripts = entry.getValue();
				for (int i = 0; i < scripts.size(); i++) {
					args.add(new Object[]{event.getCode(), scripts.get(i).getId(), i});
				}
			}

			if (!args.isEmpty()) {
					getJdbcTemplate().batchUpdate(
							"insert into event_script(event_code, script_id, ord) values(?,?,?)",
							new BatchPreparedStatementSetter() {
								@Override
								public void setValues(PreparedStatement ps, int i) throws SQLException {
									ps.setInt(1, (Integer) args.get(i)[0]);
									ps.setInt(2, (Integer) args.get(i)[1]);
									ps.setInt(3, (Integer) args.get(i)[2]);
								}

								@Override
								public int getBatchSize() {
									return args.size();
								}
							}
					);
			}
		}
	}

	/**
	 * Delete all joins between events and scripts by script's ids.
	 * Clear EVENT_SCRIPT table.
	 *
	 * @param form форма, в которой нужно удалить связи скриптов с событиями
	 */
	private void deleteEventJoins(FormTemplate form) {
		getJdbcTemplate().update(
				"delete from event_script where script_id in (select id from form_script where form_id=?)",
				form.getId()
		);
	}

	/**
	 * Select all script's ids from DB by form id.
	 *
	 * @param formId the form identifier
	 * @return the set of the identifiers of all scripts of the form.
	 */
	private Set<Integer> selectFormScriptIds(int formId) {
		JdbcTemplate jt = getJdbcTemplate();

		Set<Integer> removedScriptIds = new HashSet<Integer>();
		removedScriptIds.addAll(jt.queryForList(
				"select id from form_script where form_id = ?",
				new Object[]{formId},
				new int[]{Types.NUMERIC},
				Integer.class
		));
		return removedScriptIds;
	}

	/**
	 * Adds new scripts to DB.
	 *
	 * @param form    we add scripts to this form
	 * @param scripts list of scripts to adding
	 */
	private void insertScripts(FormTemplate form, List<Script> scripts) {
		if (!scripts.isEmpty()) {
			for (Script script : scripts) {
				insert(form, script);
			}
		}
	}

	/**
	 * Insert script to DB.
	 *
	 * @param form   form template with this script
	 * @param script script
	 */
	private void insert(final FormTemplate form, final Script script) {
		script.setId(generateId("seq_form_script", Integer.class));

		getJdbcTemplate().update(
				new PreparedStatementCreator() {
					@Override
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(
								"insert into form_script (id, form_id, name, ord, body, condition, per_row) values (?, ?, ?, ?, ?, ?, ?)"
						);
						ps.setInt(1, script.getId());
						ps.setInt(2, form.getId());
						ps.setString(3, script.getName());
						ps.setInt(4, form.indexOfScript(script));
						ps.setString(5, script.getBody());
						ps.setString(6, script.getCondition());
						ps.setBoolean(7, script.isRowScript());
						return ps;
					}
				}
		);
	}

	/**
	 * Updates existed scripts.
	 *
	 * @param form    form of scripts
	 * @param scripts list of scripts for updating.
	 */
	private void updateScripts(final FormTemplate form, final List<Script> scripts) {
		if (!scripts.isEmpty()) {
			getJdbcTemplate().batchUpdate(
					"update form_script set name = ?, ord = ?, body = ?, condition = ?, per_row=? where id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public int getBatchSize() {
							return scripts.size();
						}

						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							Script script = scripts.get(index);
							ps.setString(1, script.getName());
							ps.setInt(2, form.indexOfScript(script));
							ps.setString(3, script.getBody());
							ps.setString(4, script.getCondition());
							ps.setBoolean(5, script.isRowScript());
							ps.setInt(6, script.getId());
						}
					}
			);
		}
	}

	/**
	 * Deletes all scripts by ids in set.
	 *
	 * @param ids identifiers of scripts which must be deleted.
	 */
	private void deleteScripts(final Set<Integer> ids) {
		if (!ids.isEmpty()) {
			getJdbcTemplate().batchUpdate(
					"delete from form_script where id = ?",
					new BatchPreparedStatementSetter() {
						Iterator<Integer> iterator = ids.iterator();

						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							ps.setInt(1, iterator.next());
						}

						@Override
						public int getBatchSize() {
							return ids.size();
						}
					}
			);
		}
	}

}
