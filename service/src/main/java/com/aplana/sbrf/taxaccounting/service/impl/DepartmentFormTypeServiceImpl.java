package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: avanteev
 */
@Service
public class DepartmentFormTypeServiceImpl implements DepartmentFormTypeService {

    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;

    @Override
    public List<DepartmentFormType> getByPerformerId(int performerDepId) {
        List<Long> dftIds = departmentFormTypeDao.getDFTByPerformerId(performerDepId,
                Arrays.asList(TaxType.values()), Arrays.asList(FormDataKind.values()));
        if (dftIds.isEmpty())
            return new ArrayList<DepartmentFormType>(0);
        return departmentFormTypeDao.getByListIds(dftIds);
    }

    @Override
    public List<Long> getIdsByPerformerId(int performerDepId) {
        return departmentFormTypeDao.getDFTByPerformerId(performerDepId,
                Arrays.asList(TaxType.values()), Arrays.asList(FormDataKind.values()));
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return;
        departmentFormTypeDao.delete(ids);
    }
}
