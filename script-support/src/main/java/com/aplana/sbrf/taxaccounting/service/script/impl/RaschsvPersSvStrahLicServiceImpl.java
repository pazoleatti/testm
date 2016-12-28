package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.service.script.RaschsvPersSvStrahLicService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class RaschsvPersSvStrahLicServiceImpl implements RaschsvPersSvStrahLicService {

    @Autowired
    RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    public Integer insert(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLic) {
        return raschsvPersSvStrahLicDao.insert(raschsvPersSvStrahLic);
    }
}
