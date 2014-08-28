package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
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
            changes.setEvent(FormDataEvent.getByCode(SqlUtils.getInteger(rs, "event")));
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
                    new Object[]{templateEventId, templateChanges.getEvent().getCode(), templateChanges.getEventDate(), templateChanges.getAuthor().getId(),
                            templateChanges.getFormTemplateId(), templateChanges.getDeclarationTemplateId()},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.TIMESTAMP, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
            return templateEventId;
        } catch (DataAccessException e){
            logger.error("Ошибка при добавлении истории событий.", e);
            throw new DaoException("Ошибка при добавлении истории событий.", e);
        }

    }

    @Override
    public List<TemplateChanges> getByFormTemplateId(int ftId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        StringBuilder sql = new StringBuilder(
                "SELECT tch.id, event, author, date_event, form_template_id, declaration_template_id\n" +
                        "FROM template_changes tch\n");
        if (ordering == VersionHistorySearchOrdering.EVENT)
            sql.append("LEFT JOIN event ev on tch.event=ev.\"ID\"\n");
        sql.append("WHERE form_template_id = ?\n");
        sql.append(sortingClause(ordering, isAscSorting));

        try {
            return getJdbcTemplate().query(sql.toString(),
                    new Object[]{ftId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e) {
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e);
        }
    }

    @Override
    public List<TemplateChanges> getByDeclarationTemplateId(int dtId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        StringBuilder sql = new StringBuilder(
                "SELECT tch.id, event, author, date_event, form_template_id, declaration_template_id\n" +
                        "FROM template_changes tch\n");
        if (ordering == VersionHistorySearchOrdering.EVENT)
            sql.append("LEFT JOIN event ev on tch.event=ev.\"ID\"\n");
        sql.append("WHERE declaration_template_id = ?\n");
        sql.append(sortingClause(ordering, isAscSorting));
        try {
            return getJdbcTemplate().query(sql.toString(),
                    new Object[]{dtId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e);
        }
    }

    @Override
    public List<TemplateChanges> getByFormTypeIds(int ftTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        StringBuilder sql = new StringBuilder(
                "SELECT tch.id, event, author, date_event, form_template_id, declaration_template_id\n" +
                        "FROM template_changes tch\n");
        if (ordering == VersionHistorySearchOrdering.EVENT)
            sql.append("LEFT JOIN event ev on tch.event=ev.\"ID\"\n");
        sql.append("WHERE form_template_id IN\n" +
                        "(SELECT id FROM form_template WHERE type_id = ? AND status != 2)\n");
        sql.append(sortingClause(ordering, isAscSorting));
        try {
            return getJdbcTemplate().query(sql.toString(),
                    new Object[]{ftTypeId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e);
        }
    }

    @Override
    public List<TemplateChanges> getByDeclarationTypeId(int dtTypeId, VersionHistorySearchOrdering ordering, boolean isAscSorting) {
        StringBuilder sql = new StringBuilder(
                "SELECT tch.id, event, author, date_event, form_template_id, declaration_template_id\n" +
                        "FROM template_changes tch\n");
        if (ordering == VersionHistorySearchOrdering.EVENT)
            sql.append("LEFT JOIN event ev on tch.event=ev.\"ID\"\n");
        sql.append(" WHERE declaration_template_id IN\n" +
                        "(SELECT id FROM declaration_template WHERE declaration_type_id = ? AND status != 2)\n");
        sql.append(sortingClause(ordering, isAscSorting));
        try {
            return getJdbcTemplate().query(sql.toString(),
                    new Object[]{dtTypeId},
                    new int[]{Types.NUMERIC},
                    new TemplateChangesMapper());
        } catch (DataAccessException e){
            logger.error("Ошибка при получении истории изменнений.", e);
            throw new DaoException("Ошибка при получении истории изменнений.", e);
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

    private String sortingClause(VersionHistorySearchOrdering ordering, boolean isAscSorting) {

        StringBuilder clause = new StringBuilder();

        switch (ordering) {
            case VERSION:
                clause.append("ORDER BY form_template_id");
                break;
            case EVENT:
                clause.append("ORDER BY ev.name");
                break;
            case DATE:
                clause.append("ORDER BY date_event");
                break;
            case USER:
                clause.append("ORDER BY author");
                break;
        }

        if (!isAscSorting) clause.append(" DESC");

        return clause.toString();
    }
}
