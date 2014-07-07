package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {

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
            return getJdbcTemplate().queryForObject(
                    "select * from department where id = ?",
                    new Object[] { id },
                    new DepartmentJdbcMapper()
            );
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
    public String getParentsHierarchy(Integer departmentId) {
        try {
            return getJdbcTemplate().queryForObject(sqlParentHierarchy("name"),
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getString("path");
                        }
                    },
                    departmentId
            );
        } catch (EmptyResultDataAccessException e) {
            return getDepartment(departmentId).getName();
        }
    }

	@Override
	public String getParentsHierarchyShortNames(Integer departmentId) {
		try {
            return getJdbcTemplate().queryForObject(sqlParentHierarchy("shortname"),
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getString("path");
                        }
                    },
                    departmentId
            );
		} catch (EmptyResultDataAccessException e) {
			return getDepartment(departmentId).getShortName();
		}
	}

    private String sqlParentHierarchy(String columnName){
        return "SELECT LTRIM(SYS_CONNECT_BY_PATH("+columnName+", '/'), '/') as path \n" +
                "FROM department\n" +
                "WHERE id = ? \n" +
                "START WITH parent_id in (select id from department where parent_id is null)  \n" +
                "CONNECT BY PRIOR id = parent_id";
    }

    @Override
    public Integer getParentTBId(int departmentId) {
        try {
            return getJdbcTemplate().queryForInt("SELECT id FROM department WHERE parent_id = 0 and type = 2 " +
                    "START WITH id = ? CONNECT BY id = prior parent_id", departmentId);
        } catch (EmptyResultDataAccessException e){
            return null;
        } catch (DataAccessException e){
            throw new DaoException("", e);
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

    @Override
    public List<Integer> listDepartmentIds() {
        try {
            return getJdbcTemplate().queryForList(
                    "select id from department",
                    Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }

    protected class DepartmentJdbcMapper implements RowMapper<Department> {
		@Override
		public Department mapRow(ResultSet rs, int rowNum) throws SQLException {
			Department department = new Department();
			department.setId(SqlUtils.getInteger(rs,"id"));
			department.setName(rs.getString("name"));
			Integer parentId = SqlUtils.getInteger(rs,"parent_id");
            // В ResultSet есть особенность что если пришло значение нул то вернет значение по умолчанию - то есть для Integer'a вернет 0
            // а так как у нас в базе 0 используется в качестве идентификатора то нужно null нужно првоерять через .wasNull()
            department.setParentId(rs.wasNull() ? null : parentId);
			department.setType(DepartmentType.fromCode(SqlUtils.getInteger(rs,"type")));
			department.setShortName(rs.getString("shortname"));
			department.setTbIndex(rs.getString("tb_index"));
			department.setSbrfCode(rs.getString("sbrf_code"));
            department.setRegionId(SqlUtils.getLong(rs,"region_id"));
            if (rs.wasNull()) {
                department.setRegionId(null);
            }
            department.setActive(rs.getBoolean("is_active"));
            department.setCode(rs.getInt("code"));
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
    public Department getDepartmentByCode(int code) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM department dp WHERE dp.code = ?",
                    new Object[]{code},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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
    public List<Integer> getDepartmentIdsByType(int type) {
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT id FROM department dp WHERE dp.type = ?",
                    new Object[]{type},
                    Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }

    @Override
    public Department getDepartmenTB(int departmentId) {
        return getParentDepartmentByType(departmentId, 2);
    }

    @Override
    public List<Department> getDepartmenTBChildren(int departmentId) {
        return getParentDepartmentChildByType(departmentId, 2);
    }

    @Override
    public List<Integer> getDepartmenTBChildrenId(int departmentId) {
        return getParentDepartmentChildIdByType(departmentId, 2);
    }

    @Override
    public List<Department> getRequiredForTreeDepartments(List<Integer> availableDepartments) {
        try {
            String recursive = isWithRecursive() ? "recursive" : "";
            return getJdbcTemplate().query(
                    "with " + recursive + " tree (id, parent_id) as " +
                            "(select id, parent_id from department where " +
                            SqlUtils.transformToSqlInStatement("id", availableDepartments) +
                            "union all " +
                            "select d.id, d.parent_id from department d inner join tree t on d.id = t.parent_id) " +
                            "select distinct d.* from tree t, department d where d.id = t.id",
                    new DepartmentJdbcMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }

    /**
     * Получение родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
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

    /**
     * Получение родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
     * + все дочерние подразделения
     */
    private List<Department> getParentDepartmentChildByType(int departmentId, int typeId) {
        try {
            return getJdbcTemplate().query(createQueryParentDepartmentChildByType(false),
                    new Object[]{departmentId, typeId, typeId, typeId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }

    /**
     * Получение идентификатора родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
     * + все идентификаторы дочерних подразделений
     */
    private List<Integer> getParentDepartmentChildIdByType(int departmentId, int typeId) {
        try {
            return getJdbcTemplate().queryForList(createQueryParentDepartmentChildByType(true),
                    new Object[]{departmentId, typeId, typeId, typeId},
                    Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }

    /**
     * Формирование запроса для методов
     * @see DepartmentDaoImpl#getParentDepartmentChildIdByType(int, int)
     * @see DepartmentDaoImpl#getParentDepartmentChildByType(int, int)
     * @param idOnly true - выборка только идентификаторов, false - выборка полной модели
     * @return строку запроса
     */
    private String createQueryParentDepartmentChildByType(boolean idOnly) {
        String recursive = isWithRecursive() ? "recursive" : "";
        String rez = "with " + recursive + " tree1 (id, parent_id, type) as " +
                "(select id, parent_id, type from department where id = ? " +
                "union all " +
                "select d.id, d.parent_id, d.type from " +
                "department d inner join tree1 t1 on d.id = t1.parent_id where d.type >= ?), " +
                "tree2 (id, root_id, type) as " +
                "(select id, id root_id, type from department where type = ? " +
                "union all select d.id, t2.root_id, d.type " +
                "from department d inner join tree2 t2 on d.parent_id = t2.id) " +
                (idOnly ? "select d.id " : "select d.* ") +                         // определяем
                "from tree1 t1, tree2 t2, department d where t1.type = ? " +
                "and t2.root_id = t1.id and t2.id = d.id";
        System.out.println("" + rez+"");
        return rez;
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

    @Override
    public List<Integer> getAllChildrenIds(int parentDepartmentId) {
        try {
            return getJdbcTemplate().queryForList(
                    "select id from department CONNECT BY prior id = parent_id start with id = ?",
                    new Object[] { parentDepartmentId },
                    Integer.class
            );
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка получения дочерних подразделений", e);
        }
    }

    @Override
    public List<Integer> getAllParentIds(int depId) {
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT id FROM department START WITH id = ? CONNECT BY  id = prior parent_id",
                    new Object[]{depId},
                    Integer.class);
        } catch (DataAccessException e){
            throw new DaoException("Ошибка получения родительских подразделений.", e);
        }
    }

    @Override
    public List<Integer> getDepartmentsBySourceControl(int userDepartmentId, List<TaxType> taxTypes) {
        return getDepartmentsBySource(userDepartmentId, taxTypes, false);
    }

    @Override
    public List<Integer> getDepartmentsBySourceControlNs(int userDepartmentId, List<TaxType> taxTypes) {
        return getDepartmentsBySource(userDepartmentId, taxTypes, true);
    }

    @Override
    public List<Integer> getPerformers(List<Integer> departments, int formType) {
        String sql = String.format("SELECT performer_dep_id " +
                "FROM department_form_type " +
                "WHERE %s AND performer_dep_id IS NOT null  AND form_type_id = :formtype " +
                "GROUP BY performer_dep_id", SqlUtils.transformToSqlInStatement("department_id", departments));

        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("formtype", formType);

        return  getNamedParameterJdbcTemplate().queryForList(sql, parameterMap, Integer.class);
    }

	@Override
	public List<Integer> getPerformers(List<Integer> departments, List<TaxType> taxTypes) {
		String sql = String.format("SELECT performer_dep_id " +
				"FROM department_form_type dft " +
				"LEFT JOIN form_type ft on dft.FORM_TYPE_ID=ft.ID " +
				"WHERE %s AND ft.tax_type in (:tt) AND performer_dep_id IS NOT null " +
				"GROUP BY performer_dep_id", SqlUtils.transformToSqlInStatement("department_id", departments));

		MapSqlParameterSource parameterMap = new MapSqlParameterSource();
		List<String> types = new ArrayList<String>();
		for (TaxType type : taxTypes) {
			types.add(String.valueOf(type.getCode()));
		}
		parameterMap.addValue("tt", types);

		return  getNamedParameterJdbcTemplate().queryForList(sql, parameterMap, Integer.class);
	}

    /**
     * Поиск подразделений, доступных по иерархии и подразделений доступных по связи приемник-источник для этих подразделений
     * @param userDepartmentId Подразделение пользователя
     * @param taxTypes Типы налога
     * @param isNs true - для роли "Контролер НС", false для роли "Контролер"
     * @return Список id подразделений
     */
    private List<Integer> getDepartmentsBySource(int userDepartmentId, List<TaxType> taxTypes, boolean isNs) {
        String recursive = isWithRecursive() ? "recursive" : "";

        String availableDepartmentsSql = isNs ?
                "with " + recursive + " tree1 (id, parent_id, type) as " +
                        "(select id, parent_id, type from department where id = ? " +
                        "union all " +
                        "select d.id, d.parent_id, d.type from department d inner join tree1 t1 on d.id = t1.parent_id " +
                        "where d.type >= 2), tree2 (id, root_id, type) as " +
                        "(select id, id root_id, type from department where type = 2 " +
                        "union all " +
                        "select d.id, t2.root_id, d.type from department d inner join tree2 t2 on d.parent_id = t2.id) " +
                        "select tree2.id from tree1, tree2 where tree1.type = 2 and tree2.root_id = tree1.id"
                :
                "with " + recursive + " tree (id) as " +
                        "(select id from department where id = ? " +
                        "union all " +
                        "select d.id from department d inner join tree t on d.parent_id = t.id) " +
                        "select id from tree";

        // Параметры запроса: id подразделения и типы налога (дважды)
        Object[] sqlParams = new Object[taxTypes.size() * 2 + 1];
        int cnt = 1;
        sqlParams[0] = userDepartmentId;
        for (TaxType taxType : taxTypes) {
            sqlParams[cnt] = String.valueOf(taxType.getCode());
            sqlParams[taxTypes.size() + cnt] = String.valueOf(taxType.getCode());
            cnt++;
        }

        try {
            return getJdbcTemplate().queryForList("select id from " +
                    "(select distinct " +
                    "case when t3.c = 0 then av_dep.id else link_dep.id end as id " +
                    "from (" + availableDepartmentsSql +
                    ") av_dep left join ( " +
                    "select distinct ddt.department_id parent_id, dft.department_id id " +
                    "from declaration_source ds, department_form_type dft, department_declaration_type ddt, declaration_type dt " +
                    "where ds.department_declaration_type_id = ddt.id and ds.src_department_form_type_id = dft.id " +
                    "and dt.id = ddt.declaration_type_id and dt.tax_type in (" + SqlUtils.preparePlaceHolders(taxTypes.size()) + ") " +
                    "union " +
                    "select distinct dft.department_id parent_id, dfts.department_id id " +
                    "from form_data_source fds, department_form_type dft, department_form_type dfts, form_type ft " +
                    "where fds.department_form_type_id = dft.id and fds.src_department_form_type_id = dfts.id " +
                    "and ft.id = dft.form_type_id and ft.tax_type in (" + SqlUtils.preparePlaceHolders(taxTypes.size()) + ")) link_dep " +
                    "on av_dep.id = link_dep.parent_id, (select 0 as c from dual union all select 1 as c from dual) t3) " +
                    "where id is not null",
                    Integer.class, sqlParams);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }

    @Override
    public List<Integer> getDepartmentIdsByExcutors(List<Integer> departments, List<TaxType> taxTypes) {
        String sql = String.format("select distinct department_id from department_form_type dft " +
                "left join form_type ft on dft.form_type_id = ft.id " +
                "where " +
                "ft.tax_type in (:tt) " +
                "and %s ", SqlUtils.transformToSqlInStatement("performer_dep_id", departments));

        MapSqlParameterSource parameterMap = new MapSqlParameterSource();
        List<String> types = new ArrayList<String>();
        for (TaxType type : taxTypes) {
            types.add(String.valueOf(type.getCode()));
        }
        parameterMap.addValue("tt", types);
        return getNamedParameterJdbcTemplate().queryForList(sql, parameterMap, Integer.class);
    }

    @Override
    public List<Integer> getDepartmentIdsByExecutors(List<Integer> departments) {
        String sql = String.format("select distinct department_id from department_form_type dft " +
                "left join form_type ft on dft.form_type_id = ft.id " +
                "where %s ", SqlUtils.transformToSqlInStatement("performer_dep_id", departments));

        return getJdbcTemplate().queryForList(sql, Integer.class);
    }

    @Override
    public List<Integer> getDepartmentsByName(String departmentName) {
        try {
            MapSqlParameterSource names = new MapSqlParameterSource();
            names.addValue("depName", "%" + departmentName.toLowerCase() + "%");
            return getNamedParameterJdbcTemplate().queryForList(
                    "select id from department where lower(name) like :depName",
                    names,
                    Integer.class
            );
        } catch (DataAccessException e){
            logger.error("Ошибка при поиске подразделений по имени.", e);
            throw new DaoException("Ошибка при поиске подразделений по имени.", e);
        }
    }

    @Override
    public String getReportDepartmentName(int departmentId) {
        Department reportDepartment = getDepartment(departmentId);
        if (reportDepartment != null) {
            // рекурсивно проходим по родителям пока не упремся в корень, ТБ или никуда
            Integer parentId = reportDepartment.getParentId();
            Department parentDepartment = reportDepartment;
            while (parentDepartment != null && parentDepartment.getType() != DepartmentType.ROOT_BANK && parentDepartment.getType() != DepartmentType.TERR_BANK) {
                parentDepartment = getDepartment(parentId);
                if (parentDepartment != null) {
                    parentId = parentDepartment.getParentId();
                }
            }
            // если уперлись в ТБ, то выводим составное имя
            if (parentDepartment != null && reportDepartment.getType() != DepartmentType.TERR_BANK && parentDepartment.getType() == DepartmentType.TERR_BANK) {
                return parentDepartment.getName() + "/" + reportDepartment.getName();
            } else {
                // иначе только конец
                return reportDepartment.getName();
            }
        }
        return null;
    }
}
