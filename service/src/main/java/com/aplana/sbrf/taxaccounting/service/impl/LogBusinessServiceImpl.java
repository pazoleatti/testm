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
	public List<LogBusiness> getFormLogsBusiness(long formId) {
		return logBusinessDao.getFormLogsBusiness(formId);
	}

	@Override
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId) {
		return logBusinessDao.getDeclarationLogsBusiness(declarationId);
	}
}
