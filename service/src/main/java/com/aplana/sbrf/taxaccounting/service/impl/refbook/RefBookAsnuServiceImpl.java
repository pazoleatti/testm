package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса для работы со справочником АСНУ
 */
@Service("refBookAsnuService")
public class RefBookAsnuServiceImpl implements RefBookAsnuService {
    final private RefBookAsnuDao refBookAsnuDao;

    public RefBookAsnuServiceImpl(RefBookAsnuDao refBookAsnuDao) {
        this.refBookAsnuDao = refBookAsnuDao;
    }

    @Transactional(readOnly = true)
    public List<RefBookAsnu> fetchAvailableAsnu(TAUserInfo userInfo) {
        if (userInfo.getUser().hasRoles(TaxType.NDFL, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_OPER_NOTICE)) {
            return refBookAsnuDao.findAll();
        } else {
            return userInfo.getUser().getAsnuIds().isEmpty() ? new ArrayList<RefBookAsnu>() : refBookAsnuDao.findAllByIdIn(userInfo.getUser().getAsnuIds());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefBookAsnu> fetchByIds(List<Long> ids) {
        return refBookAsnuDao.findAllByIdIn(ids);
    }


    @Override
    @Transactional(readOnly = true)
    public RefBookAsnu fetchById(Long id) {
        return refBookAsnuDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RefBookAsnu fetchByName(String name) {
        return refBookAsnuDao.findByName(StringUtils.cleanString(name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefBookAsnu> findAll() {
        return refBookAsnuDao.findAll();
    }
}