package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvRashOssZakDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvRashOssZakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvRashOssZakService")
public class RaschsvRashOssZakServiceImpl implements RaschsvRashOssZakService {

    @Autowired
    private RaschsvRashOssZakDao raschsvRashOssZakDao;

    @Override
    public Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak) {
        return raschsvRashOssZakDao.insertRaschsvRashOssZak(raschsvRashOssZak);
    }

    @Override
    public RaschsvRashOssZak findRaschsvRashOssZak(Long declarationDataId) {
        return raschsvRashOssZakDao.findRaschsvRashOssZak(declarationDataId);
    }
}
