package com.aplana.sbrf.taxaccounting.service.script.dictionary.impl;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryRegionDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("dictionaryRegionService")
public class DictionaryRegionServiceImpl implements DictionaryRegionService {
    @Autowired
    DictionaryRegionDao dictionaryRegionDao;

    @Override
    public Boolean isValidCode(Integer code) {
        List<DictionaryRegion> regions = dictionaryRegionDao.getListRegions();
        Boolean result = false;
        for (DictionaryRegion region : regions) {
            if (region.getCode() != null && region.getCode().equals(code)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public DictionaryRegion getRegionByOkatoOrg(String okato) {
        List<DictionaryRegion> regions = dictionaryRegionDao.getListRegions();
        DictionaryRegion result = null;
        for (DictionaryRegion region : regions) {
            String okatoDefinition = region.getOkatoDefinition();
            if (okatoDefinition == null && okato == null) {
                return region;
            }
            if (okatoDefinition != null && okato != null) {
                if (okatoDefinition.equals(okato.substring(0, okatoDefinition.length()))
                        && (result == null || result.getOkatoDefinition().length() < okatoDefinition.length())) {
                    result = region;
                }
            }
        }
        return result;
    }
}
