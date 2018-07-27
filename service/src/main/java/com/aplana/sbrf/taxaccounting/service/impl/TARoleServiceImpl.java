package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TARoleServiceImpl implements TARoleService {

    @Autowired
    TARoleDao roleDao;

    @Override
    public List<TARole> getAllSbrfRoles() {
        return roleDao.getAllSbrfRoles();
    }

    @Override
    public List<TARole> getAllNdflRoles() {
        return roleDao.getAllNdflRoles();
    }

    @Override
    public TARole getRoleByAlias(String alias) {
        return roleDao.getRoleByAlias(alias);
    }
}
