package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.mapper.*;
import com.aplana.sbrf.taxaccounting.exception.*;
import com.aplana.sbrf.taxaccounting.model.*;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.*;
import org.springframework.dao.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.sql.*;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {
	private final Log logger = LogFactory.getLog(getClass());
	
	@Autowired 
	private DepartmentMapper departmentMapper;
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;
	
	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;
	
	@Override
	@Cacheable("Department")
	public Department getDepartment(int id) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching department with id = " + id  + " from database");
		}
		Department result = departmentMapper.get(id);
		if (result == null) {
			throw new DaoException("Не удалось найти подразделение банка с id = " + id);
		}
		result.setDepartmentFormTypes(departmentFormTypeDao.get(id));
		result.setDepartmentDeclarationTypes(departmentDeclarationTypeDao.getDepartmentDeclarationTypes(id));
		return result;
	}

	@Override
	public List<Department> getChildren(int parentDepartmentId){
		return departmentMapper.getChildren(parentDepartmentId);
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
        return departmentMapper.getAll();
    }

	@Override
	public List<Department> getIsolatedDepartments() {
		return departmentMapper.getIsolatedDepartments();
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
			department.setDictRegionId(rs.getString("dict_region_id"));
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
}
