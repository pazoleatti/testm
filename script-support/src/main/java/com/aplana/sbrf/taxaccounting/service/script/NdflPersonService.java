package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * @author Andrey Drunk
 */
@ScriptExposed
public interface NdflPersonService {

    public Long save(NdflPerson ndflPerson);

}
