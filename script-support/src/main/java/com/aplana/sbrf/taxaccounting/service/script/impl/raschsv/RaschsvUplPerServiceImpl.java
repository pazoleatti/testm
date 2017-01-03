package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvUplPerDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvUplPer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("raschsvUplPerService")
public class RaschsvUplPerServiceImpl {

    @Autowired
    private RaschsvUplPerDao raschsvUplPerDao;

    public Integer insertUplPer(List<RaschsvUplPer> raschsvUplPerList) {
        return raschsvUplPerDao.insertUplPer(raschsvUplPerList);
    }
}
