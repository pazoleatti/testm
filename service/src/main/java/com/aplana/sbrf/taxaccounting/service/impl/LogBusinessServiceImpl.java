package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LogBusinessServiceImpl implements LogBusinessService {

	@Autowired
	private LogBusinessDao logBusinessDao;

	@Override
	public List<LogBusiness> getFormLogsBusiness(int formId) {
		return logBusinessDao.getFormLogsBusiness(formId);
	}

	@Override
	public List<LogBusiness> getDeclarationLogsBusiness(int declarationId) {
		return logBusinessDao.getDeclarationLogsBusiness(declarationId);
	}

	@Override
	public void add(LogBusiness logBusiness) {
		logBusinessDao.add(logBusiness);
	}
}
