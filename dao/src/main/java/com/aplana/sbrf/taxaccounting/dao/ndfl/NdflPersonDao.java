package com.aplana.sbrf.taxaccounting.dao.ndfl;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;

import java.util.List;

/**
 * DAO интерфейс для работы с формой РНУ-НДФЛ
 * @author Andrey Drunk
 */
public interface NdflPersonDao {

    NdflPerson get(long ndflPersonData);

    public Long save(NdflPerson ndflPerson);

    public void delete(Long id);

    public List<NdflPerson> findNdflPersonByDeclarationDataId(Long declarationDataId);

    public List<NdflPerson> findAll();

}
