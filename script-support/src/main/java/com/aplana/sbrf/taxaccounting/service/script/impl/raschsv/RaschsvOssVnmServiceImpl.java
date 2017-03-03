package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvOssVnmDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvOssVnm;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvOssVnmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvOssVnmService")
public class RaschsvOssVnmServiceImpl implements RaschsvOssVnmService {

    @Autowired
    private RaschsvOssVnmDao raschsvOssVnmDao;

    @Override
    public Long insertRaschsvOssVnm(RaschsvOssVnm raschsvOssVnm) {
        return raschsvOssVnmDao.insertRaschsvOssVnm(raschsvOssVnm);
    }

    @Override
    public RaschsvOssVnm findOssVnm(Long declarationDataId) {
        return raschsvOssVnmDao.findOssVnm(declarationDataId);
    }
}
