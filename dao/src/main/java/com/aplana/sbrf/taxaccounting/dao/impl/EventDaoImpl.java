package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.EventDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;

/**
 * User: avanteev
 */
@Repository
public class EventDaoImpl extends AbstractDao implements EventDao {

	private static final Log LOG = LogFactory.getLog(EventDaoImpl.class);

    private static final String GET_BY_MASK = "select ev.ID event_code from EVENT ev\n" +
            "LEFT JOIN ROLE_EVENT re on ev.ID = re.EVENT_ID\n" +
            "LEFT JOIN SEC_ROLE sr on re.ROLE_ID = sr.ID\n" +
            "WHERE sr.alias = :userRoleId %s ";

    private  static final String PATTERN_MASK = "ev.id like '%s'";

    @Override
    public Collection<Integer> getEventCodes(final String roleId, final Collection<Integer> notInList, String... mask) {

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userRoleId", roleId);
        params.put("notInList", notInList);
        String odd = "";
        if (mask.length != 0){
            StringBuilder sqlMask = new StringBuilder("AND (");
            for (int i = 0; i<mask.length; i++){
                sqlMask.append(String.format(PATTERN_MASK, mask[i]));
                if (i!=mask.length-1)
                    sqlMask.append(" OR ");
            }
            sqlMask.append(")");
            odd = sqlMask.toString();
        }

        try {
            return getNamedParameterJdbcTemplate().queryForList(
                    String.format(
                            GET_BY_MASK, notInList != null && !notInList.isEmpty() ?
                            "and sr.id not in (:notInList)" :
                            ""
                    ) + odd,
                    params,
                    Integer.class
            );
        } catch (DataAccessException e) {
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }
}
