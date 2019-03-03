package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookCountryDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookCountryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником АСНУ
 */
@Service("refBookCountryService")
public class RefBookCountryServiceImpl implements RefBookCountryService {
    final private RefBookCountryDao refBookCountryDao;

    public RefBookCountryServiceImpl(RefBookCountryDao refBookCountryDao) {
        this.refBookCountryDao = refBookCountryDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefBookCountry> findAllActive() {
        return refBookCountryDao.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return refBookCountryDao.existsByCode(code);
    }
}
