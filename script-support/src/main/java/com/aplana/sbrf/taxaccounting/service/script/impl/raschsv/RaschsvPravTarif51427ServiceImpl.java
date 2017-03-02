package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPravTarif51427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPravTarif51427;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvPravTarif51427Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvPravTarif51427Service")
public class RaschsvPravTarif51427ServiceImpl implements RaschsvPravTarif51427Service {

    @Autowired
    RaschsvPravTarif51427Dao raschsvPravTarif51427Dao;

    @Override
    public Long insertRaschsvPravTarif51427(RaschsvPravTarif51427 raschsvPravTarif51427) {
        return raschsvPravTarif51427Dao.insertRaschsvPravTarif51427(raschsvPravTarif51427);
    }

    @Override
    public RaschsvPravTarif51427 findRaschsvPravTarif51427(Long declarationDataId) {
        return raschsvPravTarif51427Dao.findRaschsvPravTarif51427(declarationDataId);
    }
}
