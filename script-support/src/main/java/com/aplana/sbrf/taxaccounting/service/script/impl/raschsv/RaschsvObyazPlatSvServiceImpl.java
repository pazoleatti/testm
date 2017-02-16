package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvKolLicTipDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvObyazPlatSvDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvSvSum1TipDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvObyazPlatSv;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvObyazPlatSvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvObyazPlatSvService")
public class RaschsvObyazPlatSvServiceImpl implements RaschsvObyazPlatSvService {

    @Autowired
    private RaschsvObyazPlatSvDao raschsvObyazPlatSvDao;

    @Autowired
    private RaschsvKolLicTipDao raschsvKolLicTipDao;

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    public Long insertObyazPlatSv(RaschsvObyazPlatSv raschsvObyazPlatSv) {
        return raschsvObyazPlatSvDao.insertObyazPlatSv(raschsvObyazPlatSv);
    }

    public RaschsvObyazPlatSv findObyazPlatSv(Long declarationDataId) {
        return raschsvObyazPlatSvDao.findObyazPlatSv(declarationDataId);
    }

    @Override
    public void deleteFromLinkedTable(Long declarationDataId) {
        raschsvKolLicTipDao.deleteRaschsvKolLicTipByDeclarationDataId(declarationDataId);
        raschsvSvSum1TipDao.deleteRaschsvSvSum1TipByDeclarationDataId(declarationDataId);
    }
}
