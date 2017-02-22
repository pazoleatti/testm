package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.List;

/**
 * Сервис работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
@ScriptExposed
public interface RefBookPersonService {

    Long identificatePerson(PersonData personData, int tresholdValue, Date version, Logger logger);

    Long identificatePerson(PersonData personData, int tresholdValue, WeigthCalculator<PersonData> weigthComporators, Date version, Logger logger);

    List<BaseWeigthCalculator> getBaseCalculateList();
}
