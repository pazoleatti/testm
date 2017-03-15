package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvnpPodpisantDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvnpPodpisant;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvSvnpPodpisantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("raschsvSvnpPodpisantService")
public class RaschsvSvnpPodpisantServiceImpl implements RaschsvSvnpPodpisantService {

    @Autowired
    private RaschsvSvnpPodpisantDao raschsvSvnpPodpisantDao;

    @Override
    public Long insertRaschsvSvnpPodpisant(RaschsvSvnpPodpisant raschsvSvnpPodpisant) {
        return raschsvSvnpPodpisantDao.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant);
    }

    @Override
    public RaschsvSvnpPodpisant findRaschsvSvnpPodpisant(Long declarationDataId) {
        return raschsvSvnpPodpisantDao.findRaschsvSvnpPodpisant(declarationDataId);
    }

    @Override
    public List<RaschsvSvnpPodpisant> findRaschsvSvnpPodpisant(List<Long> declarationDataIds) {
        return raschsvSvnpPodpisantDao.findRaschsvSvnpPodpisant(declarationDataIds);
    }
}
