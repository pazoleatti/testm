package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflSumByDate;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflSumByRate;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * @author Andrey Drunk
 */
@ScriptExposed
public interface NdflPersonService {

    /**
     * Создает новую запись о доходах ФЛ привязанную к ПНФ
     * @param ndflPerson фл
     * @return
     */
    Long save(NdflPerson ndflPerson);

    /**
     * Получить запись с данными о доходах
     * @param ndflPersonId
     * @return
     */
    NdflPerson get(Long ndflPersonId);

    /**
     * Найти все данные о доходах физ лица привязанные к декларации
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPerson> findNdflPerson(long declarationDataId);

    /**
     * Найти все данные о доходах физ лица в разрезе ставок
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    List<NdflSumByRate> findNdflSumByRate(long declarationDataId);

    /**
     * Найти все данные о доходах физ лица в разрезе дат
     * @param declarationDataId - идентификатор декларации
     * @return
     */
    List<NdflSumByDate> findNdflSumByDate(long declarationDataId);
}
