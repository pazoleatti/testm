package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * @author dsultanbekov
 */
@Service
public class DeclarationAccessServiceImpl implements DeclarationAccessService {
	@Override
	public boolean canRead(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}

	@Override
	public boolean canCreate(DeclarationTemplate declarationTemplate, int departmentId, int reportPeriodId) {
		// TODO: добавить реализацию
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
