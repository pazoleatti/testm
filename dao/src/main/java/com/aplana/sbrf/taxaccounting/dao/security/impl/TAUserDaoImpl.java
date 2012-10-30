package com.aplana.sbrf.taxaccounting.dao.security.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.security.mapper.TAUserMapper;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

@Repository
@Transactional(readOnly=true)
public class TAUserDaoImpl implements TAUserDao {
	@Autowired
	private TAUserMapper userMapper;
	
	@Override
	public TAUser getUser(String login) {
		TAUser user = userMapper.getUserByLogin(login);
		if (user != null) {
			user.setRoles(userMapper.getRolesByUserId(user.getId()));
		}
		return user;
	}
}
