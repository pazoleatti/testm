package com.aplana.sbrf.taxaccounting.dao.impl.components;

import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryPersonUpdateQueryBuilderTest {

    @InjectMocks
    private RegistryPersonUpdateQueryBuilder builder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_buildPersonUpdateQuery_nothingToUpdate() {
        //setup
        List<RegistryPerson.UpdatableField> params = new ArrayList<>();
        //execution
        String result = builder.buildPersonUpdateQuery(params);
        //verification
        assertThat(result).isNull();
    }

    @Test
    public void test_buildPersonUpdateQuery() {
        //setup
        ArrayList<RegistryPerson.UpdatableField> params = new ArrayList<>();
        params.add(RegistryPerson.UpdatableField.LAST_NAME);
        params.add(RegistryPerson.UpdatableField.FIRST_NAME);
        params.add(RegistryPerson.UpdatableField.MIDDLE_NAME);
        params.add(RegistryPerson.UpdatableField.BIRTH_DATE);
        params.add(RegistryPerson.UpdatableField.CITIZENSHIP);
        params.add(RegistryPerson.UpdatableField.REPORT_DOC);
        params.add(RegistryPerson.UpdatableField.INN);
        params.add(RegistryPerson.UpdatableField.INN_FOREIGN);
        params.add(RegistryPerson.UpdatableField.SNILS);
        params.add(RegistryPerson.UpdatableField.TAX_PAYER_STATE);
        params.add(RegistryPerson.UpdatableField.SOURCE);
        params.add(RegistryPerson.UpdatableField.VIP);
        //execution
        String result = builder.buildPersonUpdateQuery(params);
        //verification
        assertThat(result).isEqualToIgnoringCase("UPDATE ref_book_person set last_name = :lastName, first_name = :firstName, " +
                "middle_name = :middleName, birth_date = :birthDate, citizenship = :citizenship, " +
                "report_doc = :reportDoc, inn = :inn, inn_foreign = :innForeign, snils = :snils, " +
                "taxpayer_state = :taxPayerState, source_id = :source, vip = :vip where id = :id");
    }

    @Test
    public void test_buildAddressUpdateQuery_nothingToUpdate() {
        //setup
        List<RegistryPerson.UpdatableField> params = new ArrayList<>();
        //execution
        String result = builder.buildAddressUpdateQuery(params);
        //verification
        assertThat(result).isNull();
    }

    @Test
    public void test_buildAddressUpdateQuery() {
        //setup
        ArrayList<RegistryPerson.UpdatableField> params = new ArrayList<>();
        params.add(RegistryPerson.UpdatableField.REGION_CODE);
        params.add(RegistryPerson.UpdatableField.POSTAL_CODE);
        params.add(RegistryPerson.UpdatableField.DISTRICT);
        params.add(RegistryPerson.UpdatableField.CITY);
        params.add(RegistryPerson.UpdatableField.LOCALITY);
        params.add(RegistryPerson.UpdatableField.STREET);
        params.add(RegistryPerson.UpdatableField.HOUSE);
        params.add(RegistryPerson.UpdatableField.BUILD);
        params.add(RegistryPerson.UpdatableField.APPARTMENT);
        params.add(RegistryPerson.UpdatableField.COUNTRY_ID);
        params.add(RegistryPerson.UpdatableField.ADDRESS);
        //execution
        String result = builder.buildAddressUpdateQuery(params);
        //verification
        assertThat(result).isEqualToIgnoringCase("UPDATE ref_book_address set region_code = :REGION_CODE, postal_code = :POSTAL_CODE, " +
                "district = :DISTRICT, city = :CITY, locality = :LOCALITY, street = :STREET, house = :HOUSE, " +
                "build = :BUILD, appartment = :APPARTMENT, country_id = :COUNTRY_ID, address = :ADDRESS where id = :id");
    }
}
