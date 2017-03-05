package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.identity.NaturalPerson;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс DAO для работы со справочником физлиц
 *
 * @author Andrey Drunk
 */
public interface RefBookPersonDao {

    Map<Long, List<PersonData>> findRefBookPersonByPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version);

    Map<Long, List<PersonData>> findRefBookPersonByPrimary1151111(Long declarationDataId, Long asnuId, Date version);

    Map<Long, List<PersonData>> findRefBookPersonByPrimaryRnuNdflUnion(Long declarationDataId, Long asnuId, Date version);

    Map<Long, Map<Long, NaturalPerson>> findRefBookPersonByPrimaryRnuNdflFunction(Long declarationDataId, Long asnuId, Date version);

}
