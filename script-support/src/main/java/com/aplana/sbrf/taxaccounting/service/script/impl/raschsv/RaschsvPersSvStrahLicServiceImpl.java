package com.aplana.sbrf.taxaccounting.service.script.impl.raschsv;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.service.script.raschsv.RaschsvPersSvStrahLicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("raschsvPersSvStrahLicService")
public class RaschsvPersSvStrahLicServiceImpl implements RaschsvPersSvStrahLicService {

    @Autowired
    private RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    @Override
    public RaschsvPersSvStrahLic get(long id) {
        return raschsvPersSvStrahLicDao.get(id);
    }

    @Override
    public Integer insertPersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLic) {
        return raschsvPersSvStrahLicDao.insertPersSvStrahLic(raschsvPersSvStrahLic);
    }

    @Override
    public Integer updatePersSvStrahLic(List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicListList) {
        return raschsvPersSvStrahLicDao.updatePersSvStrahLic(raschsvPersSvStrahLicListList);
    }

    @Override
    public RaschsvPersSvStrahLic findPersonByInn(Long declarationDataId, String innfl) {
        return raschsvPersSvStrahLicDao.findPersonByInn(declarationDataId, innfl);
    }

    @Override
    public List<RaschsvPersSvStrahLic> findPersons(Long declarationDataId) {
        return raschsvPersSvStrahLicDao.findPersons(declarationDataId);
    }

    @Override
    public List<RaschsvPersSvStrahLic> findPersonBySubreportParams(Long declarationDataId, Map<String, Object> params) {
        return raschsvPersSvStrahLicDao.findPersonBySubreportParams(declarationDataId, params);
    }
}
