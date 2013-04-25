package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.TAUserService;

@Service
@Transactional
public class TAUserServiceImpl implements TAUserService {

	@Autowired
	private TAUserDao userDao;
	
	
	@Override
	public TAUser getUser(String login) {
		int userId = userDao.getUsreIdbyLogin(login);
		return userDao.getUser(userId);
	}

	@Override
	public TAUser getUser(int userId) {
		return userDao.getUser(userId);
	}

	@Override
	public void setUserIsActive(String login, boolean isActive) {
		int userId = userDao.getUsreIdbyLogin(login);
		userDao.setUserIsActive(userId, isActive);
	}

	@Override
	public void updateUser(TAUser user) {
		userDao.updateUser(user);
	}

	@Override
	public int createUser(TAUser user) {
		int userId = userDao.createUser(user);
		return userId;
	}

	@Override
	public List<TAUser> listAllUsers() {
		List<TAUser> taUserList = new ArrayList<TAUser>();
		for(Integer userId : userDao.getUserIds())
			taUserList.add(userDao.getUser(userId));
		return taUserList;
	}

}
