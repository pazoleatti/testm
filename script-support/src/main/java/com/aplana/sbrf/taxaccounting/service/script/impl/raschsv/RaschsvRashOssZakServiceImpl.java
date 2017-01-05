package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvRashOssZakDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvRashOssZak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvRashOssZakService")
public class RaschsvRashOssZakServiceImpl {

    @Autowired
    private RaschsvRashOssZakDao raschsvRashOssZakDao;

    public Long insertRaschsvRashOssZak(RaschsvRashOssZak raschsvRashOssZak) {
        return raschsvRashOssZakDao.insertRaschsvRashOssZak(raschsvRashOssZak);
    }
}
