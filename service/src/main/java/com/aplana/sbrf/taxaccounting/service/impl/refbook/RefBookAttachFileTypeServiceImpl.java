package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAttachFileTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAttachFileTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RefBookAttachFileTypeServiceImpl implements RefBookAttachFileTypeService{
    private RefBookAttachFileTypeDao refBookAttachFileTypeDao;

    public RefBookAttachFileTypeServiceImpl(RefBookAttachFileTypeDao refBookAttachFileTypeDao) {
        this.refBookAttachFileTypeDao = refBookAttachFileTypeDao;
    }

    @Override
    public List<RefBookAttachFileType> fetchAllAttachFileTypes() {
        return refBookAttachFileTypeDao.fetchAll();
    }
}
