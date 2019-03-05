package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDocTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDocTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником АСНУ
 */
@Service("refBookDocTypeService")
public class RefBookDocTypeServiceImpl implements RefBookDocTypeService {
    final private RefBookDocTypeDao refBookDocTypeDao;

    public RefBookDocTypeServiceImpl(RefBookDocTypeDao refBookDocTypeDao) {
        this.refBookDocTypeDao = refBookDocTypeDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefBookDocType> findAllActive() {
        return refBookDocTypeDao.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return refBookDocTypeDao.existsByCode(code);
    }
}
