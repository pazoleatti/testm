package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Date;

/**
 * Сервис для работы со справочником ОКТМО
 *
 * @author dloshkarev
 */
@ScriptExposed
public interface RefBookOktmoService {
    /**
     * Получает все записи справочника ОКТМО
     *
     * @param filter         Параметр фильтрации названию и коду
     * @param pagingParams Параметры пейджинга
     * @return
     */
    PagingResult<RefBookOktmo> fetchAll(String filter, PagingParams pagingParams);

    /**
     * Получает запись из справочника ОКТМО по ее коду.
     * @param code код записи
     * @param version дата, за которую надо получить запись из справочника
     * @return
     */
    RefBookOktmo fetchByCode(String code, Date version);
}
