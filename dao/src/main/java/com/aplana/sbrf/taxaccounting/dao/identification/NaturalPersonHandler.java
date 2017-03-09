package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public abstract class NaturalPersonHandler implements RowCallbackHandler {

    /**
     * Идентификатор записи в ПНФ
     */
    public static final String PRIMARY_PERSON_ID = "person_id";

    /**
     * Идентификатор записи в справочнике ФЛ
     */
    public static final String REFBOOK_PERSON_ID = "ref_book_person_id";

    private int rowCount;

    protected Logger logger;

    private Map<Long, Map<Long, NaturalPerson>> result;

    public NaturalPersonHandler() {
        this.result = new HashMap<Long, Map<Long, NaturalPerson>>();
    }

    @Override
    public void processRow(ResultSet rs) throws SQLException {
        processRow(rs, this.rowCount++, this.result);
    }

    public abstract void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException;

    public int getRowCount() {
        return rowCount;
    }

    public Map<Long, Map<Long, NaturalPerson>> getResult() {
        return result;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
