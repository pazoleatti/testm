package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAttachFileTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAttachFileTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса для работы со справочником Категории прикрепленных файлов
 */
@Service
public class RefBookAttachFileTypeServiceImpl implements RefBookAttachFileTypeService{
    private RefBookAttachFileTypeDao refBookAttachFileTypeDao;

    public RefBookAttachFileTypeServiceImpl(RefBookAttachFileTypeDao refBookAttachFileTypeDao) {
        this.refBookAttachFileTypeDao = refBookAttachFileTypeDao;
    }

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefBookAttachFileType> fetchAllAttachFileTypes() {
        return refBookAttachFileTypeDao.fetchAll();
    }
}
