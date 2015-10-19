package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service("taUserService")
@Transactional
public class TAUserServiceImpl implements TAUserService {

	@Autowired
	private TAUserDao userDao;
	
	@Autowired
    DepartmentService departmentService;

	TAUserInfo systemUserInfo;

    private static final Log LOG = LogFactory.getLog(TAUserServiceImpl.class);

	@PostConstruct
    @SuppressWarnings("unused") // https://jira.codehaus.org/browse/SONARJAVA-117
	private void init() {
		systemUserInfo = new TAUserInfo();
		systemUserInfo.setIp("127.0.0.1");
		systemUserInfo.setUser(getUser(TAUser.SYSTEM_USER_ID));
	}

	/**
	 * Проверяет, есть ли пользователь с таким логином.
	 *
	 * @param login проверяемый логин пользователя
	 * @return true если пользователь с таким логином есть, false если нет
	 */
	@Override
	public boolean existsUser(String login) {
        return login != null && userDao.existsUser(login);
	}

	@Override
	public TAUser getUser(String login) {
		try {
			int userId = userDao.getUserIdByLogin(login);
			return userDao.getUser(userId);
		} catch (DaoException e) {
            LOG.error("Ошибка при получении пользователя по логину" + login, e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_004, "Ошибка при получении пользователя по логину." + e.getLocalizedMessage());
		}
	}

	@Override
	public TAUser getUser(int userId) {
		return userDao.getUser(userId);
	}

	@Override
	public TAUserInfo getSystemUserInfo() {
		return systemUserInfo;
	}

    @Transactional(readOnly=false)
	@Override
	public void setUserIsActive(String login, boolean isActive) {
		try {
            LOG.info("Обновленее активности пользователя по логину " + login + " setUserIsActive()");
			int userId = userDao.getUserIdByLogin(login.toLowerCase());
			userDao.setUserIsActive(userId, isActive?1:0);
		}catch (EmptyResultDataAccessException e){
            throw new WSException(WSException.SudirErrorCodes.SUDIR_004,
                    "Пользователя с login = " + login + " не существует.");
        }catch (DaoException e) {
            LOG.error("Ошибка при обновлении пользователя по логину " + login + " setUserIsActive().", e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_001,
					"Ошибка при модификации пользователя." + e.getLocalizedMessage());
		}
	}

    @Transactional(readOnly=false)
	@Override
	public void updateUser(TAUser user) {
        if(!existsUser(user.getLogin()))
            throw new WSException(WSException.SudirErrorCodes.SUDIR_004,
                    "Пользователя с login = " + user.getLogin() + " не существует.");
		for(TARole role : user.getRoles()){
			if(userDao.checkUserRole(role.getAlias()) == 0)
				throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
						"Назначенная пользователю роль " + role.getAlias() + " не зарегестрирована в системе.");
		}
		if(!departmentService.existDepartment(user.getDepartmentId()))
			throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
					"Назначенное пользователю " + user.getLogin() + " подразделение не присутствует в справочнике «Подразделения» Системы");
		try {
            LOG.info("Обновленее информации пользователя " + user.getLogin() + " updateUser()");
            user.setId(userDao.getUserIdByLogin(user.getLogin()));
			userDao.updateUser(user);
		} catch (DaoException e) {
            LOG.error("Ошибка при обновлении информации пользователя " + user.getLogin() + " updateUser().", e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_000 ,
					"Ошибка при модификации пользователя " + user.getLogin() + "." + e.getLocalizedMessage());
		}
	}

    @Transactional(readOnly=false)
	@Override
	public int createUser(TAUser user) {
		if(existsUser(user.getLogin()))
			throw new WSException(WSException.SudirErrorCodes.SUDIR_005,
					"Пользователь с таким логином уже существует.");
		for(TARole role : user.getRoles()){
			if(userDao.checkUserRole(role.getAlias()) == 0)
				throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
						"Назначенная пользователю роль " + role.getAlias() + " не зарегестрирована в системе.");
		}
		
		if(!departmentService.existDepartment(user.getDepartmentId()))
			throw new WSException(WSException.SudirErrorCodes.SUDIR_008,
					"Назначенное пользователю подразделение не присутствует в справочнике «Подразделения» Системы");
		try {
            LOG.info("Создание  пользователя " + user.getLogin() + " createUser()");
			return userDao.createUser(user);
		} catch (DaoException e) {
            LOG.error("Ошибка создания пользователя " + user.getLogin() + " createUser().", e);
			throw new WSException(WSException.SudirErrorCodes.SUDIR_000 ,
					"Ошибка при создании пользователя." + e.getLocalizedMessage());
		}
		
		
	}

	@Override
	public List<TAUser> listAllUsers() {
        LOG.info("Полученее списка пользоаателей  listAllUsers()");
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
                userFull.setDepartment(departmentService.getDepartment(user.getDepartmentId()));
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
			userFull.setDepartment(departmentService.getDepartment(user.getDepartmentId()));
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
		return getHierarchy(departmentService.getDepartment(department));
	}

	List<Department> getHierarchy(Department department) {

		List<Department> departments = new LinkedList<Department>();
		if (department.getType() != DepartmentType.ROOT_BANK) {
			departments.add(0, department);
		}
		if (department.getParentId() != null) {
			departments.addAll(0, getHierarchy(departmentService.getDepartment(department.getParentId())));
		}
		return departments;
	}
}
