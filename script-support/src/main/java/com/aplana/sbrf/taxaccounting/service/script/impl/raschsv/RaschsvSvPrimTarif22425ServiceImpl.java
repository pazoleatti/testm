package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif22425Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif22425;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvSvPrimTarif22425Service")
public class RaschsvSvPrimTarif22425ServiceImpl {

    @Autowired
    private RaschsvSvPrimTarif22425Dao raschsvSvPrimTarif22425Dao;

    public Long insertRaschsvSvPrimTarif22425(RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425) {
        return raschsvSvPrimTarif22425Dao.insertRaschsvSvPrimTarif22425(raschsvSvPrimTarif22425);
    }
}
