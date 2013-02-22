package com.aplana.sbrf.taxaccounting.service.script.dictionary.impl;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryRegionDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("declarationService")
public class DictionaryRegionServiceImpl implements DictionaryRegionService {
    @Autowired
    DictionaryRegionDao dictionaryRegionDao;

    @Override
    public Boolean isValidCodeAndName(Integer code, String name) {
        List<DictionaryRegion> regions = dictionaryRegionDao.getListRegions();
        Boolean result = false;
        for (DictionaryRegion region : regions) {
            if (region.getCode().equals(code) && region.getName().equals(name)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public DictionaryRegion getRegionByOkadoOrg(String okado) {
        List<DictionaryRegion> regions = dictionaryRegionDao.getListRegions();
        DictionaryRegion result = null;
        for (DictionaryRegion region : regions) {
            if (okado.substring(0, region.getOkatoDefinition().length()).equals(region.getOkatoDefinition())
                    && (result == null || result.getOkatoDefinition().length() < region.getOkatoDefinition().length())) {
                result = region;
            }
        }
        return result;
    }
}
