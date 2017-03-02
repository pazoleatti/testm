package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif22425Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif22425;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvSvPrimTarif22425Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvSvPrimTarif22425Service")
public class RaschsvSvPrimTarif22425ServiceImpl implements RaschsvSvPrimTarif22425Service {

    @Autowired
    private RaschsvSvPrimTarif22425Dao raschsvSvPrimTarif22425Dao;

    @Override
    public Long insertRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425) {
        return raschsvSvPrimTarif22425Dao.insertRaschsvSvPrimTarif22425(raschsvSvPrimTarif22425);
    }

    @Override
    public RaschsvSvPrimTarif22425 findRaschsvSvPrimTarif22425(Long declarationDataId) {
        return raschsvSvPrimTarif22425Dao.findRaschsvSvPrimTarif22425(declarationDataId);
    }
}
