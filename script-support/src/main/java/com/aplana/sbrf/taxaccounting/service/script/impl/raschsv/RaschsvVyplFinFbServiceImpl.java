package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvVyplFinFbDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvVyplFinFb;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvVyplFinFbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvVyplFinFbService")
public class RaschsvVyplFinFbServiceImpl implements RaschsvVyplFinFbService {

    @Autowired
    private RaschsvVyplFinFbDao raschsvVyplFinFbDao;

    public Long insertRaschsvVyplFinFb(RaschsvVyplFinFb raschsvVyplFinFb) {
        return raschsvVyplFinFbDao.insertRaschsvVyplFinFb(raschsvVyplFinFb);
    }
}
