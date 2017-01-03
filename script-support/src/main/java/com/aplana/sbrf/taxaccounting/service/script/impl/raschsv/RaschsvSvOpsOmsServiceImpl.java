package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvOpsOmsDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvOpsOms;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvSvOpsOmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("raschsvSvOpsOmsService")
public class RaschsvSvOpsOmsServiceImpl implements RaschsvSvOpsOmsService {

    @Autowired
    private RaschsvSvOpsOmsDao raschsvSvOpsOmsDao;

    public Integer insertRaschsvSvOpsOms(List<RaschsvSvOpsOms> raschsvSvOpsOmsList) {
        return raschsvSvOpsOmsDao.insertRaschsvSvOpsOms(raschsvSvOpsOmsList);
    }
}
