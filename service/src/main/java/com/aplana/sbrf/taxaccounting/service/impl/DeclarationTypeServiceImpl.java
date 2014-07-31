package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
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
    @Autowired
    private TemplateChangesService templateChangesService;

    @Override
    @Transactional(readOnly = false)
    public int save(DeclarationType type) {
        return declarationTypeDao.save(type);
    }

    @Override
    public void updateDeclarationTypeName(int typeId, String newName) {
        declarationTypeDao.updateDeclarationTypeName(typeId, newName);
    }

    @Override
    public DeclarationType get(int typeId) {
        return declarationTypeDao.get(typeId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete(int typeId) {
        List<TemplateChanges> changeses = templateChangesService.getByFormTypeIds(typeId, VersionHistorySearchOrdering.DATE, false);
        if (!changeses.isEmpty())
            templateChangesService.delete(CollectionUtils.collect(changeses, new Transformer() {
                @Override
                public Object transform(Object o) {
                    return ((TemplateChanges) o).getId();
                }
            }));
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
