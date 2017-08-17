package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
public class RefBookDepartmentDataServiceImpl implements RefBookDepartmentDataService {
    private RefBookDepartmentDataDao refBookDepartmentDataDao;

    public RefBookDepartmentDataServiceImpl(RefBookDepartmentDataDao refBookDepartmentDataDao) {
        this.refBookDepartmentDataDao = refBookDepartmentDataDao;
    }


    @Override
    public List<RefBookDepartment> fetchDepartments() {
        return refBookDepartmentDataDao.fetchDepartments();
    }
}
