package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service("taUserService")
@Transactional
public class TAUserServiceImpl implements TAUserService {

	@Autowired
	private TAUserDao userDao;
	
	@Autowired
	DepartmentDao departmentDao;

    private final Log logger = LogFactory.getLog(getClass());

	/**
	 * Проверяет, есть ли пользователь с таким логином.
	 *
	 * @param login проверяемый логин пользователя
	 * @return true если пользователь с таким логином есть, false если нет
	 */
	@Override
	public boolean existsUser(String login) {
		if (login != null) {
			return userDao.existsUser(login.toLowerCase());
		} else {
			return false;
		}
	}

	@Override
	public TAUser getUser(String login) {
        logger.info("Получение информации пользователя по логину " + login + " getUser()");
		try {
			int userId = userDao.getUserIdByLogin(login.toLowerCase());
			return userDao.getUser(userId);
		} catch (DaoException e) {
            logger.error("Ошибка при получении пользователя по логину" + login, e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_004, "Ошибка при получении пользователя по логину." + e.getLocalizedMessage());
		}
	}

	@Override
	public TAUser getUser(int userId) {
		return userDao.getUser(userId);
	}

	@Override
	public void setUserIsActive(String login, boolean isActive) {
		try {
            logger.info("Обновленее активности пользователя по логину " + login + " setUserIsActive()");
			int userId = userDao.getUserIdByLogin(login.toLowerCase());
			userDao.setUserIsActive(userId, isActive?1:0);
		}catch (EmptyResultDataAccessException e){
            throw new WSException(WSException.SudirErrorCodes.SUDIR_004,
                    "Пользователя с login = " + login + " не существует.");
        }catch (DaoException e) {
            logger.error("Ошибка при обновлении пользователя по логину " + login + " setUserIsActive().", e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_001,
					"Ошибка при модификации пользователя." + e.getLocalizedMessage());
		}
	}

	@Override
	public void updateUser(TAUser user) {
        if(userDao.checkUserLogin(user.getLogin().toLowerCase()) == 0)
            throw new WSException(WSException.SudirErrorCodes.SUDIR_004,
                    "Пользователя с login = " + user.getLogin() + " не существует.");
		for(TARole role : user.getRoles()){
			if(userDao.checkUserRole(role.getAlias()) == 0)
				throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
						"Назначенная пользователю роль " + role.getAlias() + " не зарегестрирована в системе.");
		}
		if(departmentDao.getDepartment(user.getDepartmentId()) == null)
			throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
					"Назначенное пользователю " + user.getLogin() + " подразделение не присутствует в справочнике «Подразделения» Системы");
		try {
            logger.info("Обновленее информации пользователя " + user.getLogin() + " updateUser()");
            user.setId(userDao.getUserIdByLogin(user.getLogin().toLowerCase()));
			userDao.updateUser(user);
		} catch (DaoException e) {
            logger.error("Ошибка при обновлении информации пользователя " + user.getLogin() + " updateUser().", e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_000 ,
					"Ошибка при модификации пользователя " + user.getLogin() + "." + e.getLocalizedMessage());
		}
	}

	@Override
	public int createUser(TAUser user) {
		if(userDao.checkUserLogin(user.getLogin().toLowerCase()) != 0)
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
            logger.info("Созданее  пользователя " + user.getLogin() + " createUser()");
			return userDao.createUser(user);
		} catch (DaoException e) {
            logger.error("Ошибка создания пользователя " + user.getLogin() + " createUser().", e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_000 ,
					"Ошибка при создании пользователя." + e.getLocalizedMessage());
		}
		
		
	}

	@Override
	public List<TAUser> listAllUsers() {
        logger.info("Полученее списка пользоаателей  listAllUsers()");
		List<TAUser> taUserList = new ArrayList<TAUser>();
		for(Integer userId : userDao.getUserIds())
			taUserList.add(userDao.getUser(userId));
		return taUserList;
	}

    @Override
    public List<TAUserFull> listAllFullActiveUsers() {
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

	@Override
    @Deprecated
	public PagingResult<TAUserFull> getByFilter(MembersFilterData filter) {
		PagingResult<TAUserFull> taUserFullList = new PagingResult<TAUserFull>();
		for(Integer userId : userDao.getByFilter(filter)) {
			TAUserFull userFull = new TAUserFull();
			TAUser user = userDao.getUser(userId);
			userFull.setUser(user);
			userFull.setDepartment(departmentDao.getDepartment(user.getDepartmentId()));
			taUserFullList.add(userFull);
		}
		taUserFullList.setTotalCount(userDao.count(filter));

		return taUserFullList;
	}

    @Override
    public PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter) {
        return userDao.getUsersByFilter(filter);
    }

    @Override
	public List<Department> getDepartmentHierarchy(int department) {
		return getHierarchy(departmentDao.getDepartment(department));
	}

	List<Department> getHierarchy(Department department) {

		List<Department> departments = new LinkedList<Department>();
		if (department.getType() != DepartmentType.ROOT_BANK) {
			departments.add(0, department);
		}
		if (department.getParentId() != null) {
			departments.addAll(0, getHierarchy(departmentDao.getDepartment(department.getParentId())));
		}
		return departments;
	}
}
