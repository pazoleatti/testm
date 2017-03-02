package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif31427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif31427;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvPravTarif31427Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvPravTarif31427Service")
public class RaschsvPravTarif31427ServiceImpl implements RaschsvPravTarif31427Service {

    @Autowired
    private RaschsvPravTarif31427Dao raschsvPravTarif31427Dao;

    @Override
    public Long insertRaschsvPravTarif31427(RaschsvPravTarif31427 raschsvPravTarif31427) {
        return raschsvPravTarif31427Dao.insertRaschsvPravTarif31427(raschsvPravTarif31427);
    }

    @Override
    public RaschsvPravTarif31427 findRaschsvPravTarif31427(Long declarationDataId) {
        return raschsvPravTarif31427Dao.findRaschsvPravTarif31427(declarationDataId);
    }
}
