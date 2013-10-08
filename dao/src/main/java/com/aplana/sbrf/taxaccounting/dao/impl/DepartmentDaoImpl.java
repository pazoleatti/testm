package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {
	private final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;
	
	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
	
	@Override
	@Cacheable(CacheConstants.DEPARTMENT)
	public Department getDepartment(int id) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching department with id = " + id  + " from database");
		}
        try {
            Department result = getJdbcTemplate().queryForObject(
                    "select * from department where id = ?",
                    new Object[] { id },
                    new DepartmentJdbcMapper()
            );
            return result;
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти подразделение банка с id = " + id);
        }
	}

	@Override
	public List<Department> getChildren(int parentDepartmentId){
        try {
            return getJdbcTemplate().query(
                    "select * from department where parent_id = ?",
                    new Object[] { parentDepartmentId },
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
	}

	@Override
	public Department getParent(int departmentId){
		Department department = getDepartment(departmentId);
		try {
			return getJdbcTemplate().queryForObject(
					"SELECT * FROM department dp WHERE dp.id = ?",
					new Object[]{department.getParentId()},
					new DepartmentJdbcMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

    @Override
    public List<Department> listDepartments(){
        try {
            return getJdbcTemplate().query(
                    "select * from department",
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }
	

	protected class DepartmentJdbcMapper implements RowMapper<Department> {
		@Override
		public Department mapRow(ResultSet rs, int rowNum) throws SQLException {
			Department department = new Department();
			department.setId(rs.getInt("id"));
			department.setName(rs.getString("name"));
			Integer parentId = rs.getInt("parent_id");
			if(parentId == 0){
				department.setParentId(null);
			} else {
				department.setParentId(parentId);
			}
			department.setType(DepartmentType.fromCode(rs.getInt("type")));
			department.setShortName(rs.getString("shortname"));
			department.setTbIndex(rs.getString("tb_index"));
			department.setSbrfCode(rs.getString("sbrf_code"));
			return department;
		}

	}


	@Override
	public Department getDepartmentBySbrfCode(String sbrfCode) {
		return getJdbcTemplate().queryForObject(
				"SELECT * FROM department dp WHERE dp.sbrf_code = ?",
				new Object[]{sbrfCode},
				new DepartmentJdbcMapper()
		);
	}

	@Override
	public Department getDepartmentByName(String name) {
		return getJdbcTemplate().queryForObject(
				"SELECT * FROM department dp WHERE dp.name = ?",
				new Object[]{name},
				new DepartmentJdbcMapper()
		);
	}

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        try {
            return getJdbcTemplate().query(
                    "select * from department CONNECT BY prior id = parent_id start with id = ?",
                    new Object[] { parentDepartmentId },
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }
}
