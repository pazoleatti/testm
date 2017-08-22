package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником Виды форм
 */
@Service
public class RefBookDeclarationTypeServiceImpl implements RefBookDeclarationTypeService {
    private RefBookDeclarationTypeDao refBookDeclarationTypeDao;

    public RefBookDeclarationTypeServiceImpl(RefBookDeclarationTypeDao refBookDeclarationTypeDao) {
        this.refBookDeclarationTypeDao = refBookDeclarationTypeDao;
    }

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookDeclarationType> fetchAllDeclarationTypes() {
        return refBookDeclarationTypeDao.fetchAll();
    }
}
