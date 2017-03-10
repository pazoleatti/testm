package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import com.aplana.sbrf.taxaccounting.model.identification.PersonIdentifier;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Andrey Drunk
 */
public class NaturalPersonPrimary1151111RowMapper extends NaturalPersonPrimaryRowMapper {


    @Override
    public NaturalPerson mapRow(ResultSet rs, int rowNum) throws SQLException {

        NaturalPerson person = new NaturalPerson();

        person.setPrimaryPersonId(SqlUtils.getLong(rs, "id"));
        person.setId(SqlUtils.getLong(rs, "person_id"));

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
            PersonDocument personDocument = new PersonDocument();
            personDocument.setNaturalPerson(person);
            personDocument.setDocumentNumber(documentNumber);
            personDocument.setDocType(getDocTypeByCode(documentTypeCode));
            person.getPersonDocumentList().add(personDocument);
        }

        //добавлено для 115 макета
        person.setPension(SqlUtils.getInteger(rs, "pension"));
        person.setMedical(SqlUtils.getInteger(rs, "medical"));
        person.setSocial(SqlUtils.getInteger(rs, "social"));
        person.setNum(SqlUtils.getInteger(rs, "num"));
        person.setSex(SqlUtils.getInteger(rs, "sex"));

        return person;
    }
}
