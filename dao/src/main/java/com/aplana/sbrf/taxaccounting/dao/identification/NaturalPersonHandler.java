package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public abstract class NaturalPersonHandler implements RowCallbackHandler {

    public static final String PERSON_ID = "PERSON_ID";

    private int rowCount;

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
}
