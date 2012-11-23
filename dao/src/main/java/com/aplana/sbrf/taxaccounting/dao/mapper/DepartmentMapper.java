package com.aplana.sbrf.taxaccounting.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.dao.mapper.typehandler.DepartmentTypeHandler;
import com.aplana.sbrf.taxaccounting.model.Department;

public interface DepartmentMapper {
	/**
	 * Получить информацию по подразделению
	 * @param id идентификатор подразделения
	 * @return объект, представляющий подразделение или null, если такого подразделения нет
	 */	
	@Select("select * from department where id = #{departmentId}")
	@Results({
		@Result(property="id"),
		@Result(property="name"),
		@Result(property="parentId", column="parent_id"),
		@Result(property="type", typeHandler=DepartmentTypeHandler.class)
	})
	Department get(@Param("departmentId")int id);

	/**
	 * Получить список дочерних подразделений по коду подзаделения
	 * @param parentDepartmentId идентификатор родительского подразделения
	 * @return список объектов, представляющих дочерние подразделения, если таковых нет, то будет возвращён пустой список
	 */
	@Select("select * from department where parent_id = #{parentDepartmentId}")
	@Results({
		@Result(property="id"),
		@Result(property="name"),
		@Result(property="parentId", column="parent_id"),
		@Result(property="type", typeHandler=DepartmentTypeHandler.class)
	})
	List<Department> getChildren(@Param("parentDepartmentId")int parentDepartmentId);

    /**
     * Получить список всех департаментов
     * @return список департаментов
     */
    @Select("select * from department")
	@Results({
		@Result(property="id"),
		@Result(property="name"),
		@Result(property="parentId", column="parent_id"),
		@Result(property="type", typeHandler=DepartmentTypeHandler.class)
	})
    List<Department> getAll();
}