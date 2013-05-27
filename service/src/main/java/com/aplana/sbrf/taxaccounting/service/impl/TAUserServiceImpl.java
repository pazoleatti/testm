package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import com.aplana.sbrf.taxaccounting.service.TAUserService;

@Service
@Transactional
public class TAUserServiceImpl implements TAUserService {

	@Autowired
	private TAUserDao userDao;
	
	@Autowired
	DepartmentDao departmentDao;
	
	
	@Override
	public TAUser getUser(String login) {
		try {
			int userId = userDao.getUserIdbyLogin(login);
			return userDao.getUser(userId);
		} catch (DaoException e) {
			throw new WSException(WSException.SudirErrorCodes.SUDIR_004, "Ошибка при получении пользователя по логину." + e.toString());
		}
		
	}

	@Override
	public TAUser getUser(int userId) {
		return userDao.getUser(userId);
	}

	@Override
	public void setUserIsActive(String login, boolean isActive) {
		try {
			int userId = userDao.getUserIdbyLogin(login);
			userDao.setUserIsActive(userId, isActive);
		} catch (DaoException e) {
			throw new WSException(WSException.SudirErrorCodes.SUDIR_004,
					"Пользователь с таким логином не найдем в БД.");
		}
		
	}

	@Override
	public void updateUser(TAUser user) {
		if(userDao.getUserIdbyLogin(user.getLogin()) != 0)
			throw new WSException(WSException.SudirErrorCodes.SUDIR_005,
					"Пользователь с таким логином уже существует.");
		for(TARole role : user.getRoles()){
			if(userDao.checkUserRole(role.getAlias()) == 0)
				throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
						"Назначенная пользователю роль " + role.getAlias() + " не зарегестрирована в системе.");
		}
		if(departmentDao.getDepartment(user.getDepartmentId()) == null)
			throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
					"Назначенное пользователю подразделение не присутствует в справочнике «Подразделения» Системы");
		try {
			userDao.updateUser(user);
		} catch (DaoException e) {
			throw new WSException(WSException.SudirErrorCodes.SUDIR_000 ,
					"Ошибка при модификации пользователя." + e.toString());
		}
	}

	@Override
	public int createUser(TAUser user) {
		if(userDao.checkUserLogin(user.getLogin()) != 0)
			throw new WSException(WSException.SudirErrorCodes.SUDIR_005,
					"Пользователь с таким логином уже существует.");
		for(TARole role : user.getRoles()){
			if(userDao.checkUserRole(role.getAlias()) == 0)
				throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
						"Назначенная пользователю роль " + role.getAlias() + " не зарегестрирована в системе.");
		}
		
		if(departmentDao.getDepartment(user.getDepartmentId()) == null)
			throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
					"Назначенное пользователю подразделение не присутствует в справочнике «Подразделения» Системы");
		try {
			return userDao.createUser(user);
		} catch (DaoException e) {
			throw new WSException(WSException.SudirErrorCodes.SUDIR_000 ,
					"Ошибка при создании пользователя." + e.toString());
		}
		
		
	}

	@Override
	public List<TAUser> listAllUsers() {
		List<TAUser> taUserList = new ArrayList<TAUser>();
		for(Integer userId : userDao.getUserIds())
			taUserList.add(userDao.getUser(userId));
		return taUserList;
	}

    @Override
    public List<TAUserFull> lisAllFullActiveUsers() {
        List<TAUserFull> taUserFullList = new ArrayList<TAUserFull>();
        for(Integer userId : userDao.getUserIds()){
            TAUserFull userFull = new TAUserFull();
            TAUser user = userDao.getUser(userId);
            if(user.isActive()){
                userFull.setUser(user);
                userFull.setDepartment(departmentDao.getDepartment(user.getDepartmentId()));
                taUserFullList.add(userFull);
            }
        }

        return taUserFullList;
    }

}
