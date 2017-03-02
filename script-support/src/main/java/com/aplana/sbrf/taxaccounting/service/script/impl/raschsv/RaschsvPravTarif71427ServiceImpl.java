package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif71427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif71427;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvPravTarif71427Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvPravTarif71427Service")
public class RaschsvPravTarif71427ServiceImpl implements RaschsvPravTarif71427Service {

    @Autowired
    private RaschsvPravTarif71427Dao raschsvPravTarif71427Dao;

    @Override
    public Long insertRaschsvPravTarif71427(RaschsvPravTarif71427 raschsvPravTarif71427) {
        return raschsvPravTarif71427Dao.insertRaschsvPravTarif71427(raschsvPravTarif71427);
    }

    @Override
    public RaschsvPravTarif71427 findRaschsvPravTarif71427(Long declarationDataId) {
        return raschsvPravTarif71427Dao.findRaschsvPravTarif71427(declarationDataId);
    }
}
