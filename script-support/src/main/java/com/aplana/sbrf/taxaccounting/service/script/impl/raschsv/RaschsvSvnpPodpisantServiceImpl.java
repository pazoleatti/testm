package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvnpPodpisantDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvSvnpPodpisantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvSvnpPodpisantService")
public class RaschsvSvnpPodpisantServiceImpl implements RaschsvSvnpPodpisantService {

    @Autowired
    private RaschsvSvnpPodpisantDao raschsvSvnpPodpisantDao;

    public Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant) {
        return raschsvSvnpPodpisantDao.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant);
    }

    public RaschsvSvnpPodpisant findRaschsvSvnpPodpisant(Long declarationDataId) {
        return raschsvSvnpPodpisantDao.findRaschsvSvnpPodpisant(declarationDataId);
    }
}
