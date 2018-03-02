package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Дао для октмо
 *
 * @author auldanov, dloshkarev
 */
public interface RefBookOktmoDao {
    /**
     * Получает все записи справочника ОКТМО
     *
     * @param filter         Параметр фильтрации названию и коду
     * @param pagingParams Параметры пейджинга
     * @return
     */
    PagingResult<RefBookOktmo> fetchAll(String filter, PagingParams pagingParams);

    /**
     * Получает запись из справочника ОКТМО по ее коду
     * @param code код записи
     * @param version дата, за которую надо получить запись из справочника
     * @return
     */
    RefBookOktmo fetchByCode(String code, Date version);
}
