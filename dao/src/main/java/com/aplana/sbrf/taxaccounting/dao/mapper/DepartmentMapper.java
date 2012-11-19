package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.model.Department;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DepartmentMapper {
	/**
	 * Получить информацию по подразделению
	 * @param id идентификатор подразделения
	 * @return объект, представляющий подразделение или null, если такого подразделения нет
	 */	
	@Select("select * from department where id = #{departmentId}")
	Department get(@Param("departmentId")int id);

	/**
	 * Получить список дочерних подразделений по коду подзаделения
	 * @param parentDepartmentId идентификатор родительского подразделения
	 * @return список объектов, представляющих дочерние подразделения, если таковых нет, то будет возвращён пустой список
	 */
	@Select("select * from department where parent_id = #{parentDepartmentId}")
	List<Department> getChildren(@Param("parentDepartmentId")int parentDepartmentId);

    /**
     * Получить список всех департаментов
     * @return список департаментов
     */
    @Select("select * from department")
    List<Department> getAll();
}