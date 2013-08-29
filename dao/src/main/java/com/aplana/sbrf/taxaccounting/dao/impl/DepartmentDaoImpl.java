package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentMapper;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

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
        return departmentMapper.getAllChildren(parentDepartmentId);
    }
}
