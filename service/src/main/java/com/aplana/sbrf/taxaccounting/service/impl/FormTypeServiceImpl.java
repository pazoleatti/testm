package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
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

    @Override
    public int save(FormType formType) {
        return formTypeDao.save(formType);
    }

    @Override
    public void updateFormTypeName(int formTypeId, String newName) {
        formTypeDao.updateFormTypeName(formTypeId, newName);
    }

    @Override
    public FormType get(int formTypeId) {
        return formTypeDao.get(formTypeId);
    }

    @Override
    public void delete(int formTypeId) {
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
}
