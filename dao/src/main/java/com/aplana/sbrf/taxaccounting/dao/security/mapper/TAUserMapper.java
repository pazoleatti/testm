package com.aplana.sbrf.taxaccounting.dao.security.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

public interface TAUserMapper {
	@Select("select * from sec_user where login = #{login}")
	@Results({
		@Result(property="id"),
		@Result(property="name"),
		@Result(property="departmentId", column="department_id")
	})
	TAUser getUserByLogin(@Param("login") String login);
	
	@Select("select * from sec_user where id = #{userId}")
	TAUser getUserById(@Param("userId") int userId);
	
	@Select("select * from sec_role r where exists (select 1 from sec_user_role ur where ur.role_id = r.id and ur.user_id = #{userId})")
	List<TARole> getRolesByUserId(@Param("userId")int userId);
}
