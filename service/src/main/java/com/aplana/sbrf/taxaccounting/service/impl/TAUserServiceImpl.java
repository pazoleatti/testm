package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.TAUserService;

@Service
@Transactional
public class TAUserServiceImpl implements TAUserService {

	@Autowired
	TAUserDao userDao;
	
	@Override
	public TAUser getUser(String login) {
		return userDao.getUser(login);
	}

	@Override
	public TAUser getUser(int userId) {
		return userDao.getUser(userId);
	}

	@Override
	public List<TARole> listRolesAll() {
		return userDao.listRolesAll();
	}

}
