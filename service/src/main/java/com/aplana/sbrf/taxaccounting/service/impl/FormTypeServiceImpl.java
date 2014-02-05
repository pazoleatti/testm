package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormType;
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

    @Override
    public int save(FormType formType) {
        return formTypeDao.save(formType);
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
        //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
        for (Integer id : ids)
            formTypes.add(formTypeDao.get(id));
        return formTypes;
    }
}
