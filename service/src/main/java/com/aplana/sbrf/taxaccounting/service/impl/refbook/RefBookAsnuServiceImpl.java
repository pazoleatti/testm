package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником АСНУ
 */
@Service
public class RefBookAsnuServiceImpl implements RefBookAsnuService {
    final private RefBookAsnuDao refBookAsnuDao;

    public RefBookAsnuServiceImpl(RefBookAsnuDao refBookAsnuDao) {
        this.refBookAsnuDao = refBookAsnuDao;
    }

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookAsnu> fetchAllAsnu() {
        return refBookAsnuDao.fetchAll();
    }
}
