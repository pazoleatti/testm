package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.AddressObject;
import com.aplana.sbrf.taxaccounting.service.script.FiasRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Andrey Drunk
 */
@Service("fiasRefBookService")
public class FiasRefBookServiceImpl implements FiasRefBookService {

    @Autowired
    FiasRefBookDao fiasRefBookDao;

    @Override
    public List<AddressObject> findAddress(String regionCode, String area, String city, String locality, String street) {
        return fiasRefBookDao.findAddress(regionCode, area, city, locality, street);
    }

    @Override
    public AddressObject findRegionByCode(String regionCode) {
        return fiasRefBookDao.findRegionByCode(regionCode);
    }
}
