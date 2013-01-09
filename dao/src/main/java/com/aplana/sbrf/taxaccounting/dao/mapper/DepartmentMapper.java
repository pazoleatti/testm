package com.aplana.sbrf.taxaccounting.dao.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.model.Department;

public interface DepartmentMapper {
	/**
	 * Получить информацию по подразделению
	 * @param id идентификатор подразделения
	 * @return объект, представляющий подразделение или null, если такого подразделения нет
	 */	
	@Select("select * from department where id = #{departmentId}")
	@ResultMap("departmentMap")
	Department get(@Param("departmentId")int id);

	/**
	 * Получить список дочерних подразделений по коду подзаделения
	 * @param parentDepartmentId идентификатор родительского подразделения
	 * @return список объектов, представляющих дочерние подразделения, если таковых нет, то будет возвращён пустой список
	 */
	@Select("select * from department where parent_id = #{parentDepartmentId}")
	@ResultMap("departmentMap")
	List<Department> getChildren(@Param("parentDepartmentId")int parentDepartmentId);

    /**
     * Получить список всех департаментов
     * @return список департаментов
     */
    @Select("select * from department")
    @ResultMap("departmentMap")
    List<Department> getAll();
    
    @Select("select form_type_id from department_form_type where department_id = #{departmentId}")
    Set<Integer> getDepartmentFormTypes(@Param("departmentId") int departmentId);
}