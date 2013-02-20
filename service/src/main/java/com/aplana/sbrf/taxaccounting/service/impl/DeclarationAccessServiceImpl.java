package com.aplana.sbrf.taxaccounting.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * @author dsultanbekov
 */
@Service
public class DeclarationAccessServiceImpl implements DeclarationAccessService {
	@Autowired
	TAUserDao userDao;
	
	@Override
	public boolean canRead(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}

	@Override
	public boolean canCreate(int userId, int declarationTemplateId, int departmentId, int reportPeriodId) {
		TAUser user = userDao.getUser(userId);
		return true;
	}

	@Override
	public boolean canAccept(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}

	@Override
	public boolean canReject(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}
}
