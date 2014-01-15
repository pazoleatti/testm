package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {
	private final Log logger = LogFactory.getLog(getClass());

	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;

	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

    @Autowired
    DBInfo dbInfo;

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
    public List<Department> getDepartmentsByType(int type) {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM department dp WHERE dp.type = ?",
                    new Object[] { type },
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }

    @Override
    public Department getDepartmenTB(int departmentId) {
        return getParentDepartmentByType(departmentId, 2);
    }

    /**
     * Подготовка строки вида "?,?,?,..."
     * @param length
     * @return
     */
    private String preparePlaceHolders(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length;) {
            builder.append("?");
            if (++i < length) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    @Override
    public List<Department> getRequiredForTreeDepartments(List<Integer> availableDepartments) {
        try {
            String recursive = isWithRecursive() ? "recursive" : "";
            return getJdbcTemplate().query(
                    "with " + recursive + " tree (id, parent_id) as " +
                            "(select id, parent_id from department where id in " +
                            "(" + preparePlaceHolders(availableDepartments.size()) + ") " +
                            "union all " +
                            "select d.id, d.parent_id from department d inner join tree t on d.id = t.parent_id) " +
                            "select distinct d.* from tree t, department d where d.id = t.id",
                    new DepartmentJdbcMapper(),
                    availableDepartments.toArray()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }

    /**
     * Получение родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
     * @param departmentId
     * @param typeId
     * @return
     */
    private Department getParentDepartmentByType(int departmentId, int typeId) {
        try {
            String recursive = isWithRecursive() ? "recursive" : "";
            return getJdbcTemplate().queryForObject("with " + recursive + " tree (id, parent_id, type) as " +
                    "(select id, parent_id, type from department where id = ? " +
                    "union all select d.id, d.parent_id, d.type from department d " +
                    "inner join tree t on d.id = t.parent_id) " +
                    "select d.* from department d, tree where d.id = tree.id and tree.type = ?",
                    new Object[]{departmentId, typeId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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
