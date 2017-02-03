package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
@ScriptExposed
public interface RefBookPersonService {

    Long identificatePerson(PersonData personData);

    Long identificatePerson(PersonData personData, int tresholdValue);

    Long identificatePerson(PersonData personData, int tresholdValue, WeigthCalculator<PersonData> weigthComporators);

}
