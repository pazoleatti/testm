package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.service.script.FiasRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Andrey Drunk
 */
@Service("fiasRefBookService")
public class FiasRefBookServiceImpl implements FiasRefBookService {

    @Autowired
    FiasRefBookDao fiasRefBookDao;

    @Override
    public Map<Long, Long> checkAddressByFias(Long declarationDataId) {
        return fiasRefBookDao.checkAddressByFias(declarationDataId);
    }

    @Override
    public Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationDataId) {
        return fiasRefBookDao.checkExistsAddressByFias(declarationDataId);
    }

}
