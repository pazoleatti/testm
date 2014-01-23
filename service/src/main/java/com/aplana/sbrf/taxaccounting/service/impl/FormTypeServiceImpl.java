package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
