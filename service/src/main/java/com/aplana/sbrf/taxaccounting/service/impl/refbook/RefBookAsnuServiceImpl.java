package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAsnuDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by aokunev on 09.08.2017.
 */
@Service
@Transactional
public class RefBookAsnuServiceImpl implements RefBookAsnuService {
    final private RefBookAsnuDao refBookAsnuDao;

    public RefBookAsnuServiceImpl(RefBookAsnuDao refBookAsnuDao) {
        this.refBookAsnuDao = refBookAsnuDao;
    }

    @Override
    public List<RefBookAsnu> fetchAllAsnu() {
        return refBookAsnuDao.fetchAll();
    }
}
