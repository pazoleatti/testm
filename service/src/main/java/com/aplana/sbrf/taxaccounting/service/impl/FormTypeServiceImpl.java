package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
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
public class FormTypeServiceImpl implements FormTypeService {

    @Autowired
    private FormTypeDao formTypeDao;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
    @Autowired
    private TemplateChangesService templateChangesService;

    @Override
    public int save(FormType formType) {
        return formTypeDao.save(formType);
    }

    @Override
    public void updateFormType(int formTypeId, String newName, String code) {
        formTypeDao.updateFormType(formTypeId, newName, code);
    }

    @Override
    public FormType get(int formTypeId) {
        return formTypeDao.get(formTypeId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void delete(int formTypeId) {
        List<TemplateChanges> changeses = templateChangesService.getByFormTypeIds(formTypeId, VersionHistorySearchOrdering.DATE, false);
        if (!changeses.isEmpty())
            templateChangesService.delete(CollectionUtils.collect(changeses, new Transformer() {
                @Override
                public Object transform(Object o) {
                    return ((TemplateChanges)o).getId();
                }
            }));
        formTypeDao.delete(formTypeId);
    }

    @Override
    public List<FormType> getByFilter(TemplateFilter filter) {
        List<Integer> ids = formTypeDao.getByFilter(filter);

        List<FormType> formTypes = new ArrayList<FormType>();
        for (Integer id : ids){
            formTypes.add(formTypeDao.get(id));
        }
        return formTypes;
    }

	@Override
	public List<FormType> getFormTypes(int departmentId, int reportPeriod, TaxType taxType, List<FormDataKind> kind) {
		return formTypeDao.getFormTypes(departmentId, reportPeriodDao.get(reportPeriod), taxType, kind);
    }

    @Override
    public FormType getByCode(String code) {
        return formTypeDao.getByCode(code);
    }
}
