package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
@Service
@Transactional
public class DeclarationTypeServiceImpl implements DeclarationTypeService {

    @Autowired
    private DeclarationTypeDao declarationTypeDao;
	@Autowired
	private ReportPeriodDao reportPeriodDao;

    @Override
    @Transactional(readOnly = false)
    public int save(DeclarationType type) {
        return declarationTypeDao.save(type);
    }

    @Override
    public DeclarationType get(int typeId) {
        return declarationTypeDao.get(typeId);
    }

    @Override
    public void delete(int typeId) {
        declarationTypeDao.delete(typeId);
    }

    @Override
    public List<DeclarationType> listAll() {
        return declarationTypeDao.listAll();
    }

    @Override
    public List<DeclarationType> getByFilter(TemplateFilter filter) {
        List<Integer> integerList = declarationTypeDao.getByFilter(filter);
        List<DeclarationType> declarationTypes = new ArrayList<DeclarationType>();
        //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
        for (Integer id : integerList)
            declarationTypes.add(declarationTypeDao.get(id));
        return declarationTypes;
    }

	@Override
	public List<DeclarationType> getTypes(int departmentId, int reportPeriod, TaxType taxType) {
		return declarationTypeDao.getTypes(departmentId, reportPeriodDao.get(reportPeriod), taxType);
	}
}
