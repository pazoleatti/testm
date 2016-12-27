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

    NdflPerson get(long ndflPersonData);

    public Long save(NdflPerson ndflPerson);

    public void delete(Long id);

    public List<NdflPerson> findAll();

    public List<NdflPerson> findNdflPersonByDeclarationDataId(long declarationDataId);

    public List<NdflPersonIncome> findNdflPersonIncomesByNdfPersonId(long ndflPersonId);

    public List<NdflPersonDeduction> findNdflPersonDeductionByNdfPersonId(long ndflPersonId);

    public List<NdflPersonPrepayment> findNdflPersonPrepaymentByNdfPersonId(long ndflPersonId);


}
