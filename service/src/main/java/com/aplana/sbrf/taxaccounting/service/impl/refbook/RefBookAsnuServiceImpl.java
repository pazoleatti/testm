package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
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
     * Получение доступных значений справочника
     *
     * @param userInfo Информация о пользователей
     * @return Список доступных значений справочника
     */
    @Transactional(readOnly = true)
    public List<RefBookAsnu> fetchAvailableAsnu(TAUserInfo userInfo) {
        if (userInfo.getUser().hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
            return refBookAsnuDao.fetchAll();
        } else {
            return refBookAsnuDao.fetchByIds(userInfo.getUser().getAsnuIds());
        }
    }

}
