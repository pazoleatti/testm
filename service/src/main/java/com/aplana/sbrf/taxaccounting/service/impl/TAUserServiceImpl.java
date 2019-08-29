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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    private TAUserInfo systemUserInfo;

    private static final Log LOG = LogFactory.getLog(TAUserServiceImpl.class);

    @PostConstruct
    @SuppressWarnings("unused") // https://jira.codehaus.org/browse/SONARJAVA-117
    private void init() {
        systemUserInfo = new TAUserInfo();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            LOG.error("UnknownHost", e);
        }
        String ipValue = "127.0.0.1";
        if (inetAddress != null) {
            ipValue = inetAddress.getHostAddress();
        }
        systemUserInfo.setIp(ipValue);
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

    @Override
    public List<TAUser> listAllUsers() {
        LOG.info("Получение списка пользователей listAllUsers()");
        List<TAUser> taUserList = new ArrayList<TAUser>();
        for (Integer userId : userDao.getAllUserIds())
            taUserList.add(userDao.getUser(userId));
        return taUserList;
    }

    @Override
    public List<TAUserFull> listAllFullActiveUsers() {
        List<TAUserFull> taUserFullList = new ArrayList<TAUserFull>();
        for (Integer userId : userDao.getAllUserIds()) {
            TAUserFull userFull = new TAUserFull();
            TAUser user = userDao.getUser(userId);
            if (user.isActive()) {
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
        for (Integer userId : userDao.getUserIdsByFilter(filter)) {
            TAUserFull userFull = new TAUserFull();
            TAUser user = userDao.getUser(userId);
            userFull.setUser(user);
            userFull.setDepartment(departmentService.getDepartment(user.getDepartmentId()));
            taUserFullList.add(userFull);
        }
        taUserFullList.setTotalCount(userDao.countUsersByFilter(filter));

        return taUserFullList;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
    public PagingResult<TAUserView> getUsersViewWithFilter(MembersFilterData filter) {
        return userDao.getUsersViewWithFilter(filter);
    }

    @Override
    public TAUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : getUser(((User) auth.getPrincipal()).getUsername());
    }
}
