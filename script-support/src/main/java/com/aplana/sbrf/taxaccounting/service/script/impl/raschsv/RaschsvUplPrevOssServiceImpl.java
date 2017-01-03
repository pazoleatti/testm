package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvUplPrevOssDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPrevOss;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvUplPrevOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvUplPrevOssService")
public class RaschsvUplPrevOssServiceImpl implements RaschsvUplPrevOssService {

    @Autowired
    private RaschsvUplPrevOssDao raschsvUplPrevOssDao;

    public Long insertUplPrevOss(RaschsvUplPrevOss raschsvUplPrevOss) {
        return raschsvUplPrevOssDao.insertUplPrevOss(raschsvUplPrevOss);
    }
}
