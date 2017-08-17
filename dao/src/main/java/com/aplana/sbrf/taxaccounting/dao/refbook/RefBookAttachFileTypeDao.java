package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;

import java.util.List;

/**
 * Дао для работы со справочником Категории прикрепленных файлов
 */
public interface RefBookAttachFileTypeDao {

    List<RefBookAttachFileType> fetchAll();
}
