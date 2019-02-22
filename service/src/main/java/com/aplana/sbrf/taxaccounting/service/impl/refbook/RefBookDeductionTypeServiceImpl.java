package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeductionTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeductionType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeductionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("refBookDeductionTypeService")
public class RefBookDeductionTypeServiceImpl implements RefBookDeductionTypeService {

    @Autowired
    private RefBookDeductionTypeDao refBookDeductionTypeDao;

    @Override
    public List<RefBookDeductionType> findAllByVersion(Date version) {
        return refBookDeductionTypeDao.findAllByVersion(version);
    }
}
