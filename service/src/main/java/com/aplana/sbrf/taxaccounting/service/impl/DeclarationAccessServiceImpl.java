package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DeclarationAccessService;

/**
 * Реализация сервиса для проверки прав на доступ к декларациям
 * @author dsultanbekov
 */
public class DeclarationAccessServiceImpl implements DeclarationAccessService {
	@Override
	public boolean canRead(int userId, long declarationId) {
		// TODO: добавить реализацию
		return true;
	}

	@Override
	public boolean canCreate(TaxType taxType, int departmentId, int reportPeriodId) {
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
