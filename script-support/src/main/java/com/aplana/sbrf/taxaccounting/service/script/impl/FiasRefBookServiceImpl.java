package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo;
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
    public Map<Long, FiasCheckInfo> checkAddressByFias(Long declarationDataId) {
        return fiasRefBookDao.checkAddressByFias(declarationDataId, 0);
    }

    @Override
    public Map<Long, FiasCheckInfo> checkAddressByFias(Long declarationDataId, int p_check_type) {
        return fiasRefBookDao.checkAddressByFias(declarationDataId, p_check_type);
    }

    @Override
    public Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationDataId) {
        return fiasRefBookDao.checkExistsAddressByFias(declarationDataId, 0);
    }

    @Override
    public Map<Long, CheckAddressResult> checkExistsAddressByFias(Long declarationDataId, int p_check_type) {
        return fiasRefBookDao.checkExistsAddressByFias(declarationDataId, p_check_type);
    }

    @Override
    public void refreshViews() {
        fiasRefBookDao.refreshViews();
    }
}