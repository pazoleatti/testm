package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.identification.IdentityPerson;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.BaseWeigthCalculator;
import com.aplana.sbrf.taxaccounting.model.util.WeigthCalculator;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
@ScriptExposed
public interface RefBookPersonService {

    Map<Long, Map<Long, NaturalPerson>> findRefBookPersonByPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version);

    Map<Long, List<PersonData>> findRefBookPersonByPrimary1151111(Long declarationDataId, Long asnuId, Date version);

    NaturalPerson identificatePerson(PersonData personData, List<IdentityPerson> refBookPersonList, int tresholdValue, Logger logger);

    NaturalPerson identificatePerson(PersonData personData, List<IdentityPerson> refBookPersonList, int tresholdValue, WeigthCalculator<IdentityPerson> weigthComporators, Logger logger);

    List<BaseWeigthCalculator> getBaseCalculateList();

}
