package com.aplana.sbrf.taxaccounting.dao.security.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.security.mapper.TAUserMapper;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

@Repository
@Transactional(readOnly=true)
public class TAUserDaoImpl implements TAUserDao {
	private final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private TAUserMapper userMapper;
	
	@Override
	public TAUser getUser(String login) {
		TAUser user = userMapper.getUserByLogin(login);
		if (user == null) {
			throw new DaoException("Пользователь с логином \"" + login + "\" не найден в БД");
		}
		initUser(user);
		return user;		
	}

	@Override
	public TAUser getUser(int userId) {
		TAUser user = userMapper.getUserById(userId);
		if (user == null) {
			throw new DaoException("Пользователь с id = " + userId + " не найден в БД");
		}
		initUser(user);		
		return user;
	}
	
	private void initUser(TAUser user) {
		if (user != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("User found, login = " + user.getLogin() + ", id = " + user.getId());
			}
			List<TARole> userRoles = userMapper.getRolesByUserId(user.getId()); 
			user.setRoles(userRoles);
		}
	}
}
