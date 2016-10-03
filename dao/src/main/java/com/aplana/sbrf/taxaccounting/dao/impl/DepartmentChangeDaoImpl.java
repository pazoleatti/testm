package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentChangeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DepartmentChange;
import com.aplana.sbrf.taxaccounting.model.DepartmentChangeOperationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class DepartmentChangeDaoImpl extends AbstractDao implements DepartmentChangeDao {

    protected class DepartmentChangeJdbcMapper implements RowMapper<DepartmentChange> {
        @Override
        public DepartmentChange mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                DepartmentChange departmentChange = new DepartmentChange();
                departmentChange.setOperationType(DepartmentChangeOperationType.fromCode(SqlUtils.getInteger(rs, "operationType")));
                departmentChange.setId(SqlUtils.getInteger(rs, "id"));
                departmentChange.setLevel(rs.getInt("hier_level"));
                departmentChange.setName(rs.getString("name"));
                Integer parentId = SqlUtils.getInteger(rs, "parent_id");
                departmentChange.setParentId(rs.wasNull() ? null : parentId);
                Integer type = SqlUtils.getInteger(rs, "type");
                departmentChange.setType(rs.wasNull() ? null : DepartmentType.fromCode(type));
                departmentChange.setShortName(rs.getString("shortname"));
                departmentChange.setTbIndex(rs.getString("tb_index"));
                departmentChange.setSbrfCode(rs.getString("sbrf_code"));
                departmentChange.setRegion(rs.getString("region"));
                departmentChange.setIsActive(rs.getBoolean("is_active"));
                departmentChange.setCode(rs.getLong("code"));
                departmentChange.setGarantUse(rs.getBoolean("garant_use"));
                departmentChange.setSunrUse(rs.getBoolean("sunr_use"));
                return departmentChange;
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
    }

    @Override
    public void clear() {
        getJdbcTemplate().update("delete department_change");
    }

    @Override
    public List<DepartmentChange> getAllChanges() {
        try {
            return getJdbcTemplate().query(
                    "select operationtype, id, hier_level, name, parent_id, type, shortname, tb_index, sbrf_code, region, is_active, code, garant_use, sunr_use " +
                            "from department_change " +
                            "order by log_date",
                    new DepartmentChangeJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<DepartmentChange>();
        }
    }

    @Override
    public void addChange(DepartmentChange departmentChange) {
        if (departmentChange.getOperationType() == DepartmentChangeOperationType.DELETE) {
            getJdbcTemplate().update("insert into department_change " +
                            "(operationtype, log_date, id)" +
                            "values (?, sysdate, ?)",
                    departmentChange.getOperationType().getCode(),
                    departmentChange.getId()
            );
        } else {
            getJdbcTemplate().update("insert into department_change " +
                            "(operationtype, log_date, id, hier_level, name, parent_id, type, shortname, tb_index, sbrf_code, region, is_active, code, garant_use, sunr_use)" +
                            "values (?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    departmentChange.getOperationType().getCode(),
                    departmentChange.getId(),
                    departmentChange.getLevel(),
                    departmentChange.getName(),
                    departmentChange.getParentId(),
                    departmentChange.getType().getCode(),
                    departmentChange.getShortName(),
                    departmentChange.getTbIndex(),
                    departmentChange.getSbrfCode(),
                    departmentChange.getRegion(),
                    departmentChange.getIsActive(),
                    departmentChange.getCode(),
                    departmentChange.getGarantUse(),
                    departmentChange.getSunrUse()
            );
        }
    }

    @Override
    public boolean checkDepartment(int depId, Integer depParentId) {
        return getJdbcTemplate().queryForObject("select count(*) from department_change where id = " + depId + (depParentId != null?(" or id = " + depParentId):""), Integer.class)>0;
    }
}
