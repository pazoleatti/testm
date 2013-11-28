package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TARoleServiceImpl implements TARoleService {
	@Autowired
	TARoleDao taRoleDao;

	@Override
	public List<TARole> getAll() {
		List<TARole> roles = new ArrayList<TARole>();
		for (Integer id : taRoleDao.getAll()) {
			roles.add(taRoleDao.getRole(id));
		}
		return roles;
	}
}
