package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PersonData;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс DAO для работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
public interface RefBookPersonDao {

    List<PersonData> findPersonByPersonData(PersonData personData, Date version);

    Map<Long, List<PersonData>> findRefBookPersonByPrimaryRnuNdfl(long declarationDataId, long asnuId, Date version);

    Map<Long, List<PersonData>> findRefBookPersonByPrimary1151111(long declarationDataId, long asnuId, Date version);
}
