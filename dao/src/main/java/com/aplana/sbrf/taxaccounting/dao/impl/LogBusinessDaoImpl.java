package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Repository
public class LogBusinessDaoImpl extends AbstractDao implements LogBusinessDao {

    @Override
    public List<LogBusinessDTO> findAllByDeclarationId(long declarationId, PagingParams pagingParams) {
        String query = "select lb.id, lb.log_date, lb.event_id, e.name event_name, nvl2(u.login, u.name || ' (' || u.login || ')', lb.user_login) AS user_name, " +
                " lb.roles, lb.user_department_name, lb.declaration_data_id, lb.person_id, lb.note " +
                "from log_business lb \n" +
                "left join event e on e.id = lb.event_id \n" +
                "left join sec_user u on u.login = lb.user_login \n" +
                "where declaration_data_id = ?";
        if (pagingParams != null) {
            if (isNotBlank(pagingParams.getProperty()) && isNotBlank(pagingParams.getDirection())) {
                query = query + " order by " + pagingParams.getProperty() + " " + pagingParams.getDirection();
            } else {
                query = query + " order by id";
            }
        }
        return getJdbcTemplate().query(query, new BeanPropertyRowMapper<>(LogBusinessDTO.class), declarationId);
    }

    @Override
    public PagingResult<LogBusinessDTO> findAllByPersonId(long personId, PagingParams pagingParams) {
        String query = "select lb.id, lb.log_date, lb.event_id, e.name event_name, nvl2(u.login, u.name || ' (' || u.login || ')', lb.user_login) AS user_name, " +
                " lb.roles, lb.user_department_name, lb.declaration_data_id, lb.person_id, lb.note " +
                "from log_business lb \n" +
                "left join event e on e.id = lb.event_id \n" +
                "left join sec_user u on u.login = lb.user_login \n" +
                "where person_id = ?";
        List<Object> params = new ArrayList<>();
        params.add(personId);
        if (pagingParams != null) {
            query = pagingParams.wrapQuery(query, params);
        }
        List<LogBusinessDTO> logs = getJdbcTemplate().query(query, params.toArray(), new BeanPropertyRowMapper<>(LogBusinessDTO.class));
        int total = getJdbcTemplate().queryForObject("select count(*) from(" + query + ")", params.toArray(), Integer.class);
        return new PagingResult<>(logs, total);
    }

    @Override
    public Date getFormCreationDate(long formId) {
        try {
            return new Date(
                    getJdbcTemplate().queryForObject(
                            "select log_date from log_business where declaration_data_id = ? and event_id = ? ",
                            new Object[]{formId, FormDataEvent.CREATE.getCode()}, Timestamp.class
                    ).getTime()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String getFormCreationUserName(long declarationData) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select user_login from log_business where declaration_data_id = ? and event_id = ? ",
                    new Object[]{declarationData, FormDataEvent.CREATE.getCode()}, String.class
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void create(LogBusiness logBusiness) {
        Long id = logBusiness.getId();
        if (id == null) {
            id = generateId("seq_log_business", Long.class);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("log_date", logBusiness.getLogDate());
        params.addValue("event_id", logBusiness.getEventId());
        params.addValue("user_login", logBusiness.getUserLogin());
        params.addValue("roles", StringUtils.substring(logBusiness.getRoles(), 0, 1000));
        params.addValue("declaration_data_id", logBusiness.getDeclarationDataId());
        params.addValue("person_id", logBusiness.getPersonId());
        params.addValue("user_department_name", logBusiness.getUserDepartmentName());
        params.addValue("note", StringUtils.substring(logBusiness.getNote(), 0, 2000));

        getNamedParameterJdbcTemplate().update(
                "insert into log_business (id, log_date, event_id, user_login, roles, declaration_data_id, person_id, user_department_name, note)" +
                        " values (:id, :log_date, :event_id, :user_login, :roles, :declaration_data_id, :person_id, :user_department_name, :note)",
                params
        );
    }
}
