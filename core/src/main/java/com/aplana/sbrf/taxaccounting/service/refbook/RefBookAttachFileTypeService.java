package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;

import java.util.List;

/**
 * Сервис для работы со справочником Категории прикрепленных файлов
 */
public interface RefBookAttachFileTypeService {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookAttachFileType> fetchAllAttachFileTypes();
}
