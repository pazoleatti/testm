package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;

@Repository
public class DeclarationTemplateEventScriptDaoImpl extends AbstractDao implements DeclarationTemplateEventScriptDao {

    private static final Log LOG = LogFactory.getLog(DeclarationTemplateEventScriptDaoImpl.class);

    private class DeclarationTemplateEventScriptRowMapper implements RowMapper<DeclarationTemplateEventScript> {
        @Override
        public DeclarationTemplateEventScript mapRow(ResultSet resultSet, int i) throws SQLException {
            DeclarationTemplateEventScript toReturn = new DeclarationTemplateEventScript();
            toReturn.setId(SqlUtils.getLong(resultSet, "id"));
            toReturn.setDeclarationTemplateId(SqlUtils.getInteger(resultSet, "declaration_template_id"));
            toReturn.setEventId(SqlUtils.getInteger(resultSet, "event_id"));
            return toReturn;
        }
    }

    @Override
    public List<DeclarationTemplateEventScript> fetch(int declarationTemplateId) {
        String query = "SELECT ID, DECLARATION_TEMPLATE_ID, EVENT_ID, SCRIPT FROM decl_template_event_script where declaration_template_id = ?";
        List<DeclarationTemplateEventScript> toReturn = getJdbcTemplate().query(query,
                new Object[]{declarationTemplateId},
                new int[]{Types.NUMERIC},
                new DeclarationTemplateEventScriptRowMapper());
        for (DeclarationTemplateEventScript eventScript : toReturn) {
            eventScript.setScript(getScript(eventScript.getId()));
        }
        return toReturn;
    }

    @Override
    @Cacheable(value = CacheConstants.DECLARATION_TEMPLATE_EVENT_SCRIPT, key = "#declarationTemplateEventScriptId + new String(\"_eventScript\")")
    public String getScript(long declarationTemplateEventScriptId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select script from decl_template_event_script where id = ?",
                    new Object[]{declarationTemplateEventScriptId},
                    new int[]{Types.NUMERIC},
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return "";
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка получения скрипта налоговой формы.", e);
        }
    }

    @Override
    public String findScript(int declarationTemplateId, int eventId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select script from decl_template_event_script where declaration_template_id = ? and event_id = ?",
                    new Object[]{declarationTemplateId, eventId},
                    new int[]{Types.NUMERIC,
                            Types.NUMERIC},
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка получения скрипта налоговой формы.", e);
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE_EVENT_SCRIPT, beforeInvocation = true, key = "#declarationTemplateEventScriptId + new String(\"_eventScript\")")
    public void updateScript(long declarationTemplateEventScriptId, String script) {
        getJdbcTemplate().update("UPDATE decl_template_event_script SET script = ? where id = ?", script, declarationTemplateEventScriptId);
    }

    @Override
    public boolean checkIfEventScriptPresent(int declarationTemplateId, int formDataEventId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("declarationTemplateId", declarationTemplateId);
        values.put("formDataEventId", formDataEventId);
        try {
            return getNamedParameterJdbcTemplate()
                    .queryForList("SELECT id FROM decl_template_event_script " +
                            "WHERE declaration_template_id = :declarationTemplateId AND event_id = :formDataEventId", values).size() > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    @CacheEvict(value = CacheConstants.DECLARATION_TEMPLATE_EVENT_SCRIPT, beforeInvocation = true, key = "#declarationTemplateEventScriptId + new String(\"_eventScript\")")
    public DeclarationTemplateEventScript create(DeclarationTemplateEventScript declarationTemplateEventScript) {
        long id = generateId("seq_decl_template_event_script", Long.class);
        getJdbcTemplate().update(
                "insert into decl_template_event_script (id, declaration_template_id, event_id, script) VALUES (?, ?, ?, ?)",
                new Object[]{
                        id,
                        declarationTemplateEventScript.getDeclarationTemplateId(),
                        declarationTemplateEventScript.getEventId(),
                        declarationTemplateEventScript.getScript()
                },
                new int[] {
                        Types.NUMERIC,
                        Types.NUMERIC,
                        Types.NUMERIC,
                        Types.CLOB
                }
        );
        declarationTemplateEventScript.setId(id);
        return declarationTemplateEventScript;
    }

    @Override
    public void delete(long declarationTemplateEventScriptId) {
        try {
            getJdbcTemplate().update(
                    "delete from decl_template_event_script where id = ?",
                    new Object[]{declarationTemplateEventScriptId},
                    new int[]{Types.NUMERIC}
            );
        } catch (DataAccessException e) {
            LOG.error("Ошибка во время удаления.", e);
            throw new DaoException("Ошибка во время удаления.", e);
        }
    }

    @Override
    public void updateScriptList(DeclarationTemplate declarationTemplate) {
        List<DeclarationTemplateEventScript> eventScriptsForRemove = fetch(declarationTemplate.getId());
        eventScriptsForRemove.removeAll(declarationTemplate.getEventScripts());
        for (DeclarationTemplateEventScript declarationTemplateEventScript: eventScriptsForRemove) {
            if (declarationTemplateEventScript.getId() != null) {
                delete(declarationTemplateEventScript.getId());
            }
        }
        for (DeclarationTemplateEventScript declarationTemplateEventScript: declarationTemplate.getEventScripts()) {
            if (declarationTemplateEventScript.getId() == null) {
                create(declarationTemplateEventScript);
            } else {
                updateScript(declarationTemplateEventScript.getId(), declarationTemplateEventScript.getScript());
            }
        }
    }
}
