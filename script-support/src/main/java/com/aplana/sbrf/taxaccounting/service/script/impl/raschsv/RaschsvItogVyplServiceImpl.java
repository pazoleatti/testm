package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvItogVyplDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVypl;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvItogVyplDop;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvItogVyplService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service("raschsvItogVyplService")
public class RaschsvItogVyplServiceImpl implements RaschsvItogVyplService {

    @Autowired
    private RaschsvItogVyplDao raschsvItogVyplDao;

    @Override
    public Long insertItogStrahLic(RaschsvItogStrahLic raschsvItogStrahLic) {
        return raschsvItogVyplDao.insertItogStrahLic(raschsvItogStrahLic);
    }

    @Override
    public int[] insertItogVypl(Collection<RaschsvItogVypl> raschsvItogVypls) {
        return raschsvItogVyplDao.insertItogVypl(raschsvItogVypls);
    }

    @Override
    public int[] insertItogVyplDop(Collection<RaschsvItogVyplDop> raschsvItogVyplDops) {
        return raschsvItogVyplDao.insertItogVyplDop(raschsvItogVyplDops);
    }
}
