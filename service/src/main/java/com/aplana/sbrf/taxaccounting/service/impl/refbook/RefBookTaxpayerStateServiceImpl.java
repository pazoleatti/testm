package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookTaxpayerStateDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookTaxpayerStateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником "Статусы налогоплательщика"
 */
@Service("refBookTaxpayerStateService")
public class RefBookTaxpayerStateServiceImpl implements RefBookTaxpayerStateService {
    final private RefBookTaxpayerStateDao refBookTaxpayerStateDao;

    public RefBookTaxpayerStateServiceImpl(RefBookTaxpayerStateDao refBookTaxpayerStateDao) {
        this.refBookTaxpayerStateDao = refBookTaxpayerStateDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefBookTaxpayerState> findAllActive() {
        return refBookTaxpayerStateDao.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return refBookTaxpayerStateDao.existsByCode(code);
    }
}
