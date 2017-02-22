package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PersonData;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс DAO для работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
public interface RefBookPersonDao {

    List<PersonData> findPersonByPersonData(PersonData personData, Date version);

}
