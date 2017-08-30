package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Реализация сервиса для работы со справочником Подразделения
 */
@Service
public class RefBookDepartmentDataServiceImpl implements RefBookDepartmentDataService {
    private RefBookDepartmentDataDao refBookDepartmentDataDao;

    public RefBookDepartmentDataServiceImpl(RefBookDepartmentDataDao refBookDepartmentDataDao) {
        this.refBookDepartmentDataDao = refBookDepartmentDataDao;
    }

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookDepartment> fetchDepartments() {
        return refBookDepartmentDataDao.fetchDepartments();
    }
}
