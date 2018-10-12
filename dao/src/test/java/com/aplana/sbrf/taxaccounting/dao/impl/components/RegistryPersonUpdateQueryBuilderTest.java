package com.aplana.sbrf.taxaccounting.dao.impl.components;

import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
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
        List<RegistryPersonDTO.UpdatableField> params = new ArrayList<>();
        //execution
        String result = builder.buildPersonUpdateQuery(params);
        //verification
        assertThat(result).isNull();
    }

    @Test
    public void test_buildPersonUpdateQuery() {
        //setup
        ArrayList<RegistryPersonDTO.UpdatableField> params = new ArrayList<>();
        params.add(RegistryPersonDTO.UpdatableField.LAST_NAME);
        params.add(RegistryPersonDTO.UpdatableField.FIRST_NAME);
        params.add(RegistryPersonDTO.UpdatableField.MIDDLE_NAME);
        params.add(RegistryPersonDTO.UpdatableField.BIRTH_DATE);
        params.add(RegistryPersonDTO.UpdatableField.CITIZENSHIP);
        params.add(RegistryPersonDTO.UpdatableField.REPORT_DOC);
        params.add(RegistryPersonDTO.UpdatableField.INN);
        params.add(RegistryPersonDTO.UpdatableField.INN_FOREIGN);
        params.add(RegistryPersonDTO.UpdatableField.SNILS);
        params.add(RegistryPersonDTO.UpdatableField.TAX_PAYER_STATE);
        params.add(RegistryPersonDTO.UpdatableField.SOURCE);
        params.add(RegistryPersonDTO.UpdatableField.VIP);
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
        List<RegistryPersonDTO.UpdatableField> params = new ArrayList<>();
        //execution
        String result = builder.buildAddressUpdateQuery(params);
        //verification
        assertThat(result).isNull();
    }

    @Test
    public void test_buildAddressUpdateQuery() {
        //setup
        ArrayList<RegistryPersonDTO.UpdatableField> params = new ArrayList<>();
        params.add(RegistryPersonDTO.UpdatableField.REGION_CODE);
        params.add(RegistryPersonDTO.UpdatableField.POSTAL_CODE);
        params.add(RegistryPersonDTO.UpdatableField.DISTRICT);
        params.add(RegistryPersonDTO.UpdatableField.CITY);
        params.add(RegistryPersonDTO.UpdatableField.LOCALITY);
        params.add(RegistryPersonDTO.UpdatableField.STREET);
        params.add(RegistryPersonDTO.UpdatableField.HOUSE);
        params.add(RegistryPersonDTO.UpdatableField.BUILD);
        params.add(RegistryPersonDTO.UpdatableField.APPARTMENT);
        params.add(RegistryPersonDTO.UpdatableField.COUNTRY_ID);
        params.add(RegistryPersonDTO.UpdatableField.ADDRESS);
        //execution
        String result = builder.buildAddressUpdateQuery(params);
        //verification
        assertThat(result).isEqualToIgnoringCase("UPDATE ref_book_address set region_code = :REGION_CODE, postal_code = :POSTAL_CODE, " +
                "district = :DISTRICT, city = :CITY, locality = :LOCALITY, street = :STREET, house = :HOUSE, " +
                "build = :BUILD, appartment = :APPARTMENT, country_id = :COUNTRY_ID, address = :ADDRESS where id = :id");
    }
}
