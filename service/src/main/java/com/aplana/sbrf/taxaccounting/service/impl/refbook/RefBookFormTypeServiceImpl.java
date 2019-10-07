package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookFormTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RefBookFormTypeServiceImpl implements RefBookFormTypeService {

    @Autowired
    private RefBookFormTypeDao refBookFormTypeDao;

    @Override
    @Transactional(readOnly = true)
    public List<RefBookFormType> fetchAll() {
        return refBookFormTypeDao.fetchAll();
    }

    @Override
    public RefBookFormType findOne(int id) {
        return refBookFormTypeDao.findOne(id);
    }
}
