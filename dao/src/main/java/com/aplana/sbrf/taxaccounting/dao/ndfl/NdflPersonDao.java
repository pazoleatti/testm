package com.aplana.sbrf.taxaccounting.dao.ndfl;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment;

import java.util.Date;
import java.util.List;

/**
 * DAO интерфейс для работы с формой РНУ-НДФЛ
 *
 * @author Andrey Drunk
 */
public interface NdflPersonDao {

    /**
     * Найти NdflPerson по идентификатору, возвращает объект с заполненными данными о доходах
     */
    NdflPerson get(long ndflPersonId);


    Long save(NdflPerson ndflPerson);

    /**
     * Удалить данные по указанному ФЛ из декларации, каскадное удаление
     * @param ndflPersonId
     */
    void delete(Long ndflPersonId);

    /**
     * Найти все данные НДВЛ физ лица привязанные к декларации
     * @param declarationDataId идентификатор декларации
     */
    List<NdflPerson> findPerson(long declarationDataId);

    /**
     * Найти данные о доходах ФЛ
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonIncome> findIncomes(long ndflPersonId);

    /**
     * Найти данные о доходах ФЛ по идентификатору декларации
     * @param declarationDataId
     * @param startDate - начало периода для "Дата удержания налога" и "Дата платежного поручения"
     * @param endDate - окончание периода для "Дата удержания налога" и "Дата платежного поручения"
     * @return
     */
    List<NdflPersonIncome> findIncomesByPeriodAndDeclarationDataId(long declarationDataId, Date startDate, Date endDate);

    /**
     * Данные об авансах ФЛ по идентификатору декларации
     * @param declarationDataId
     * @return
     */
    List<NdflPersonPrepayment> findPrepaymentsByDeclarationDataId(long declarationDataId);

    /**
     * Найти данный о вычетах
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonDeduction> findDeductions(long ndflPersonId);

    /**
     * Найти данные о авансовых платежах
     * @param ndflPersonId
     * @return
     */
    List<NdflPersonPrepayment> findPrepayments(long ndflPersonId);

}
