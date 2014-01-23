package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
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
            changes.setId(rs.getInt("id"));
            changes.setEventDate(rs.getDate("date_event"));
            changes.setAuthor(taUserDao.getUser(rs.getInt("author")));
            changes.setEvent(TemplateChangesEvent.fromId(rs.getInt("event")));
            changes.setFormTemplateId(rs.getInt("form_template_id"));
            changes.setDeclarationTemplateId(rs.getInt("declaration_template_id"));
            return changes;
        }
    }

    @Override
    public int add(TemplateChanges templateChanges) {
        try {
            int templateEventId = generateId("seq_template_changes", Integer.class);
            getJdbcTemplate().update("insert into template_changes(id, event, date_event, author, form_template_id, declaration_template_id)" +
                    " values(?, ?, ?, ?, ?, ?)",
                    new Object[]{templateEventId, templateChanges.getEvent().getId(), templateChanges.getEventDate(), templateChanges.getAuthor().getId(),
                            templateChanges.getFormTemplateId(), templateChanges.getDeclarationTemplateId()},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.DATE, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
            return templateEventId;
        } catch (DataAccessException e){
            logger.error("Ошибка ппи добавлении истории событий.", e);
            throw new DaoException("Ошибка ппи добавлении истории событий.", e);
        }

    }

    @Override
    public List<TemplateChanges> getByFormTemplateId(int ftId) {
        try {
            return getJdbcTemplate().query("select id, event, author, date_event, form_template_id, declaration_template_id from template_changes where form_template_id = ?",
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
            return getJdbcTemplate().query("select id, event, author, date_event, form_template_id, declaration_template_id from template_changes where declaration_template_id = ?",
                    new Object[]{dtId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e.getMessage());
        }
    }
}
