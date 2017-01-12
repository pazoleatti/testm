package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvPersSvStrahLicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("raschsvPersSvStrahLicService")
public class RaschsvPersSvStrahLicServiceImpl implements RaschsvPersSvStrahLicService {

    @Autowired
    private RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    @Override
    public Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLic) {
        return raschsvPersSvStrahLicDao.insertPersSvStrahLic(raschsvPersSvStrahLic);
    }

    @Override
    public List<RaschsvPersSvStrahLic> findPersonsByInn(Long declarationDataId, String innfl) {
        return raschsvPersSvStrahLicDao.findPersonsByInn(declarationDataId, innfl);
    }

    @Override
    public List<RaschsvPersSvStrahLic> findPersons(Long declarationDataId) {
        return raschsvPersSvStrahLicDao.findPersons(declarationDataId);
    }
}
