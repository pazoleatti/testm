package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ScriptDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Script;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * DAO для работы со скриптами.
 *
 * Как таковой работы со скриптами не происходит. Все происходит прозрачно через сохранение и получение шаблона формы.
 * Именно DAO шаблона формы использует этот класс
 *
 * @see FormTemplateDaoImpl
 */
@Repository
public class ScriptDaoImpl extends AbstractDao implements ScriptDao {
	/**
	 * Получает скрипты шаблона формы из БД и привязывает их к объектной модели.
	 * @param formTemplate шаблон формы
	 */
	@Override
	@Transactional(readOnly = true)
	public void fillFormScripts(final FormTemplate formTemplate) {
		final Map<Integer, Script> scriptMap = new HashMap<Integer, Script>();

		formTemplate.clearScripts();

		getJdbcTemplate().query(
				"select * from form_script where form_id = ? order by ord",
				new Object[]{formTemplate.getId()},
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
						formTemplate.addScript(script);
					}
				}
		);

		getJdbcTemplate().query(
				"select es.event_code, es.script_id from event_script es join form_script fs on es.script_id=fs.id where fs.form_id = ? order by es.event_code, es.ord",
				new Object[]{formTemplate.getId()}, new int[]{Types.NUMERIC},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						formTemplate.addEventScript(FormDataEvent.getByCode(rs.getInt("event_code")), scriptMap.get(rs.getInt("script_id")));
					}
				}
		);
	}

	/**
	 * Берет скрипты из объектной модели и сохраняет/обновляет их в БД. Удаляет ненужные.
	 * @param formTemplate шаблон формы
	 */
	@Override
	@Transactional(readOnly = false)
	public void saveFormScripts(FormTemplate formTemplate) {
		Set<Integer> removedScriptIds = selectFormScriptIds(formTemplate.getId());

		// Clear joins between scripts and events.
		deleteEventJoins(formTemplate);

		List<Script> oldScripts = new ArrayList<Script>();
		List<Script> newScripts = new ArrayList<Script>();

		for (Script script : formTemplate.getScripts()) {
			if (script.getId() == null) {
				newScripts.add(script);
			} else {
				removedScriptIds.remove(script.getId());
				oldScripts.add(script);
			}
		}

		// Insert new scripts
		insertScripts(formTemplate, newScripts);

		// Update old scripts
		updateScripts(formTemplate, oldScripts);

		// Delete deleted scripts
		deleteScripts(removedScriptIds);

		// Add joins between scripts and events
		insertEventJoins(formTemplate);
	}

	/**
	 * Сохраняет назначения скриптов на соцыбтия формы.
	 *
	 * @param formTemplate шаблон формы
	 */
	private void insertEventJoins(FormTemplate formTemplate) {
		Map<FormDataEvent, List<Script>> eventScripts = formTemplate.getEventScripts();
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
	 * Удаляет все связи событий формы и скриптов для определенной формы.
	 * Очищает таблицу EVENT_SCRIPT.
	 *
	 * @param formTemplate форма, в которой нужно удалить связи скриптов с событиями
	 */
	private void deleteEventJoins(FormTemplate formTemplate) {
		getJdbcTemplate().update(
				"delete from event_script where script_id in (select id from form_script where form_id=?)",
				formTemplate.getId()
		);
	}

	/**
	 * Получает все скрипты по идентификатору шаблона формы.
	 *
	 * @param formId идентификатор шаблона формы
	 * @return множество скриптов формы
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
	 * Сохраняет новые скрипты в БД.
	 *
	 * @param form    форма, в которую добавляются скрипты
	 * @param scripts список скриптов
	 */
	private void insertScripts(final FormTemplate form, final List<Script> scripts) {
		if (!scripts.isEmpty()) {
			for (Script script : scripts) {
				script.setId(generateId("seq_form_script", Integer.class));
			}

			getJdbcTemplate().batchUpdate(
					"insert into form_script (id, form_id, name, ord, body, condition, per_row) values (?, ?, ?, ?, ?, ?, ?)",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int i) throws SQLException {
							Script script = scripts.get(i);
							ps.setInt(1, script.getId());
							ps.setInt(2, form.getId());
							ps.setString(3, script.getName());
							ps.setInt(4, form.indexOfScript(script));
							ps.setString(5, script.getBody());
							ps.setString(6, script.getCondition());
							ps.setBoolean(7, script.isRowScript());
						}

						@Override
						public int getBatchSize() {
							return scripts.size();
						}
					}
			);
		}
	}

	/**
	 * обновляет существующие скрипты
	 *
	 * @param form    шаблон формы
	 * @param scripts списко скриптов для обновления
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
	 * удаляет скрипты по идентификаторам
	 *
	 * @param ids множество идентификаторов удаляемых скриптов
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
