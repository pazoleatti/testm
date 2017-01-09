package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvPrimTarif13422Dao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvPrimTarif13422;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvSvPrimTarif13422Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvSvPrimTarif13422Service")
public class RaschsvSvPrimTarif13422ServiceImpl implements RaschsvSvPrimTarif13422Service {

    @Autowired
    private RaschsvSvPrimTarif13422Dao raschsvSvPrimTarif13422Dao;

    public Long insertRaschsvSvPrimTarif13422(RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422) {
        return raschsvSvPrimTarif13422Dao.insertRaschsvSvPrimTarif13422(raschsvSvPrimTarif13422);
    }
}
