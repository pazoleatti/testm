package com.aplana.sbrf.taxaccounting.dao.security.mapper;

import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

public interface TAUserMapper {
	@Select("select * from user where login = #{login}")
	TAUser getUserByLogin(@Param("login") String login);
	
	@Select("select * from role r where exists (select 1 from user_role ur where ur.role_id = r.id and ur.user_id = #{userId})")
	Set<TARole> getRolesByUserId(@Param("userId")int userId);
}
