package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif91427Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif91427;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvSvPrimTarif91427Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvSvPrimTarif91427Service")
public class RaschsvSvPrimTarif91427ServiceImpl implements RaschsvSvPrimTarif91427Service {

    @Autowired
    private RaschsvSvPrimTarif91427Dao raschsvSvPrimTarif91427Dao;

    public Long insertRaschsvSvPrimTarif91427(RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427) {
        return raschsvSvPrimTarif91427Dao.insertRaschsvSvPrimTarif91427(raschsvSvPrimTarif91427);
    }
}
