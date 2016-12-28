package com.aplana.sbrf.taxaccounting.dao.raschsv;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvFile;

/**
 * DAO-интерфейс для работы с таблицей "Файл обмена"
 */
public interface RaschsvFileDao {

    RaschsvFile get(long raschsvFileId);

    Integer insert(RaschsvFile raschsvFile);
}
