package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Repository
public class LogBusinessDaoImpl extends AbstractDao implements LogBusinessDao {

    @Autowired
    private DepartmentDao departmentDao;

    private RowMapper logBusinessDTORowMapper = new BeanPropertyRowMapper<>(LogBusinessDTO.class);

    @Override
    public List<LogBusinessDTO> findAllByDeclarationId(long declarationId, PagingParams pagingParams) {
        String query = "select lb.id, lb.log_date, lb.event_id, e.name event_name, nvl2(u.login, u.name || ' (' || u.login || ')', lb.user_login) AS user_name, " +
                " lb.roles, lb.user_department_name, lb.declaration_data_id, lb.person_id, lb.note, lb.log_id " +
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
        return getJdbcTemplate().query(query, logBusinessDTORowMapper, declarationId);
    }

    @Override
    public PagingResult<LogBusinessDTO> findAllByPersonId(long personId, PagingParams pagingParams) {
        String query = "select lb.id, lb.log_date, lb.event_id, e.name event_name, nvl2(u.login, u.name || ' (' || u.login || ')', lb.user_login) AS user_name, " +
                " lb.roles, lb.user_department_name, lb.declaration_data_id, lb.person_id, lb.note " +
                "from log_business lb \n" +
                "left join event e on e.id = lb.event_id \n" +
                "left join sec_user u on u.login = lb.user_login \n" +
                "where person_id = :personId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("personId", personId);
        if (pagingParams != null) {
            query = pagingParams.wrapQuery(query, params);
        }
        List<LogBusinessDTO> logs = getNamedParameterJdbcTemplate().query(query, params, logBusinessDTORowMapper);
        int total = getNamedParameterJdbcTemplate().queryForObject("select count(*) from(" + query + ")", params, Integer.class);
        return new PagingResult<>(logs, total);
    }

    @Override
    public Date getMaxLogDateByDeclarationIdAndEvent(Long declarationId, FormDataEvent event) {
        try {
            return new Date(getJdbcTemplate().queryForObject("" +
                    "select max(log_date) from log_business \n" +
                    "where declaration_data_id = ? \n" +
                    "and event_id = ?", Date.class, declarationId, event.getCode()).getTime());
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
        params.addValue("event_id", logBusiness.getEvent().getCode());
        params.addValue("user_login", logBusiness.getUserLogin());
        params.addValue("roles", StringUtils.substring(logBusiness.getRoles(), 0, 1000));
        params.addValue("declaration_data_id", logBusiness.getDeclarationDataId());
        params.addValue("person_id", logBusiness.getPersonId());
        params.addValue("user_department_name", departmentDao.getParentsHierarchyShortNames(logBusiness.getUser().getDepartmentId()));
        params.addValue("note", StringUtils.substring(logBusiness.getNote(), 0, 2000));
        params.addValue("log_id", logBusiness.getLogId());

        getNamedParameterJdbcTemplate().update(
                "insert into log_business (id, log_date, event_id, user_login, roles, declaration_data_id, person_id, user_department_name, note, log_id)" +
                        " values (:id, :log_date, :event_id, :user_login, :roles, :declaration_data_id, :person_id, :user_department_name, :note, :log_id)",
                params
        );
    }
}
