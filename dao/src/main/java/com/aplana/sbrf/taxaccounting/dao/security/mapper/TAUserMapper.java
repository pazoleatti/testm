package com.aplana.sbrf.taxaccounting.dao.security.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

/**
 * Mapper-интерфейс для работы с данными по пользователям и их ролями
 * @author dsultanbekov
 */
public interface TAUserMapper {
	/**
	 * Получить объект пользователя по логину
	 * @param login логин пользователя
	 * @return объект с данными пользователя, или null, если пользователя с таким логином не существует
	 */
	@Select("select * from sec_user where login = #{login}")
	@ResultMap("userMap")
	TAUser getUserByLogin(@Param("login") String login);

	/**
	 * Получить объект пользователя по идентификатору
	 * @param userId идентификатор пользователя
	 * @return объект с данными пользователя, или null, если пользователя с таким идентификатором не существует
	 */
	@Select("select * from sec_user where id = #{userId}")
	@ResultMap("userMap")
	TAUser getUserById(@Param("userId") int userId);
	
	/**
	 * Получить список ролей, назначенных пользователю
	 * @param userId идентификтор пользователя
	 * @return список {@link TARole ролей} пользователя
	 */
	@Select("select * from sec_role r where exists (select 1 from sec_user_role ur where ur.role_id = r.id and ur.user_id = #{userId})")
	List<TARole> getRolesByUserId(@Param("userId")int userId);
}
