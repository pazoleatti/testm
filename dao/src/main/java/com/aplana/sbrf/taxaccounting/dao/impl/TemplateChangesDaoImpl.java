package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.TemplateChangesEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * User: avanteev
 */
@Repository
public class TemplateChangesDaoImpl extends AbstractDao implements TemplateChangesDao {

    @Autowired
    private TAUserDao taUserDao;

    private final Log logger = LogFactory.getLog(getClass());

    private class TemplateChangesMapper implements RowMapper<TemplateChanges>{

        @Override
        public TemplateChanges mapRow(ResultSet rs, int rowNum) throws SQLException {
            TemplateChanges changes = new TemplateChanges();
            changes.setId(SqlUtils.getInteger(rs, "id"));
            changes.setEventDate(new Date(rs.getTimestamp("date_event").getTime()));
            changes.setAuthor(taUserDao.getUser(SqlUtils.getInteger(rs,"author")));
            changes.setEvent(TemplateChangesEvent.fromId(SqlUtils.getInteger(rs,"event")));
            changes.setFormTemplateId(SqlUtils.getInteger(rs,"form_template_id"));
            changes.setDeclarationTemplateId(SqlUtils.getInteger(rs,"declaration_template_id"));
            return changes;
        }
    }

    @Override
    public int add(TemplateChanges templateChanges) {
        try {
            int templateEventId = generateId("seq_template_changes", Integer.class);
            getJdbcTemplate().update("INSERT INTO template_changes(id, event, date_event, author, form_template_id, declaration_template_id)" +
                    " VALUES(?, ?, ?, ?, ?, ?)",
                    new Object[]{templateEventId, templateChanges.getEvent().getId(), templateChanges.getEventDate(), templateChanges.getAuthor().getId(),
                            templateChanges.getFormTemplateId(), templateChanges.getDeclarationTemplateId()},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
            return templateEventId;
        } catch (DataAccessException e){
            logger.error("Ошибка при добавлении истории событий.", e);
            throw new DaoException("Ошибка при добавлении истории событий.", e);
        }

    }

    @Override
    public List<TemplateChanges> getByFormTemplateId(int ftId) {
        try {
            return getJdbcTemplate().query("SELECT id, event, author, date_event, form_template_id, declaration_template_id FROM template_changes WHERE form_template_id = ? ORDER BY date_event",
                    new Object[]{ftId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e.getMessage());
        }
    }

    @Override
    public List<TemplateChanges> getByDeclarationTemplateId(int dtId) {
        try {
            return getJdbcTemplate().query("SELECT id, event, author, date_event, form_template_id, declaration_template_id FROM template_changes WHERE declaration_template_id = ? ORDER BY date_event",
                    new Object[]{dtId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e.getMessage());
        }
    }

    @Override
    public List<TemplateChanges> getByFormTypeIds(int ftTypeId) {
        try {
            return getJdbcTemplate().query("SELECT id, event, author, date_event, form_template_id, declaration_template_id FROM template_changes WHERE form_template_id IN" +
                    "(SELECT id FROM form_template WHERE type_id = ? AND status != 2) ORDER BY date_event",
                    new Object[]{ftTypeId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e.getMessage());
        }
    }

    @Override
    public List<TemplateChanges> getByDeclarationTypeId(int dtTypeId) {
        try {
            return getJdbcTemplate().query("SELECT id, event, author, date_event, form_template_id, declaration_template_id FROM template_changes WHERE declaration_template_id IN" +
                    "(SELECT id FROM declaration_template WHERE declaration_type_id = ? AND status != 2) ORDER BY date_event",
                    new Object[]{dtTypeId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e.getMessage());
        }
    }

    @Override
    public void delete(final Collection<Integer> ids) {
        try {
            getNamedParameterJdbcTemplate().update("delete from template_changes where id in (:ids)",
                    new HashMap<String, Object>(){{put("ids", ids);}});
        } catch (DataAccessException e){
            logger.error("Удаление записей журнала изменений", e);
            throw new DaoException("Удаление записей журнала изменений", e);
        }
    }
}
