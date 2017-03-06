package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimary1151111RowMapper extends NaturalPersonPrimaryRowMapper {


    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaturalPerson person = new NaturalPerson();

        person.setPersonId(SqlUtils.getLong(rs, "id"));
        person.setRefBookPersonId(SqlUtils.getLong(rs, "person_id"));
        person.setRecordId(SqlUtils.getLong(rs, "record_id"));

        person.setSnils(rs.getString("snils"));
        person.setLastName(rs.getString("last_name"));
        person.setFirstName(rs.getString("first_name"));
        person.setMiddleName(rs.getString("middle_name"));
        person.setBirthDate(rs.getDate("birth_day"));

        person.setCitizenship(getCountryByCode(rs.getString("citizenship")));
        person.setInn(rs.getString("inn_np"));
        person.setInnForeign(rs.getString("inn_foreign"));


        String documentTypeCode = rs.getString("id_doc_type");
        String documentNumber = rs.getString("id_doc_number");

        if (documentNumber != null && documentTypeCode != null) {
            PersonDocument document = new PersonDocument();
            document.setDocumentNumber(documentNumber);
            document.setDocType(getDocTypeByCode(documentTypeCode));
        }

        person.setTaxPayerStatus(getTaxpayerStatusByCode(rs.getString("status")));

        //rs.getString("additional_data")

        return person;
    }
}
