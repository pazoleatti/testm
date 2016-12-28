package com.aplana.sbrf.taxaccounting.dao.ndfl;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;

import java.util.List;

/**
 * DAO интерфейс для работы с формой РНУ-НДФЛ
 *
 * @author Andrey Drunk
 */
public interface NdflPersonDao {

    /**
     * Найти NdflPerson по идентификатору, метод используется для тестирования
     */
    NdflPerson get(long ndflPersonId);

    Long save(NdflPerson ndflPerson);

    void delete(Long ndflPersonId);

    /**
     * Найти все данные о доходах физ лица привязанные к декларации
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPerson> findNdflPerson(long declarationDataId);

    List<NdflPersonIncome> findIncomes(long ndflPersonId);

    List<NdflPersonDeduction> findDeductions(long ndflPersonId);

    List<NdflPersonPrepayment> findPrepayments(long ndflPersonId);

}
