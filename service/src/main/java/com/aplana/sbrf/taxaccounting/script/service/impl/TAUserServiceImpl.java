package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.script.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service("taUserServiceScript")
public class TAUserServiceImpl implements TAUserService {

    @Autowired
    TAUserDao userDao;

    @Override
    public TAUserInfo getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            TAUserInfo taUserInfo = new TAUserInfo();
            taUserInfo.setUser(userDao.getUser(userDao.getUserIdByLogin(((User) auth.getPrincipal()).getUsername())));
            return taUserInfo;
        }else {
            return null;
        }
    }
}
