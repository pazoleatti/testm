package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.*;


public class SelectPersonQueryGeneratorTest {

    private RefBookPersonFilter filter;
    private PagingParams pagingParams;
    private SelectPersonQueryGenerator generator;

    @Before
    public void init() {
        filter = new RefBookPersonFilter();
        pagingParams = new PagingParams();
        generator = new SelectPersonQueryGenerator(filter, pagingParams);
    }

    @Test
    public void test_generateFilteredQuery_noFilter() {
        generator = new SelectPersonQueryGenerator(null);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("asnu.id = person.source_id");
    }

    @Test
    public void test_generateFilteredQuery_filterById() {
        filter.setId("D1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("like '%D1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByVip() {
        filter.setVip(true);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and person.vip = 1");
    }

    @Test
    public void test_generateFilteredQuery_filterByNonVip() {
        filter.setVip(false);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and person.vip = 0");
    }

    @Test
    public void test_generateFilteredQuery_filterByName() {
        filter.setFirstName("John");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("like '%JOHN%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByBirthDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JANUARY, 1);
        filter.setBirthDateFrom(calendar.getTime());
        filter.setBirthDateTo(calendar.getTime());

        String query = generator.generateFilteredQuery();

        assertThat(query).contains("birth_date >= date '2000-01-01'");
        assertThat(query).contains("birth_date <= date '2000-01-01'");
    }

    @Test
    public void test_generateFilteredQuery_filterByEmptyDocs() {
        filter.setDocumentNumber("");
        filter.setDocTypeIds(new ArrayList<Long>());

        String query = generator.generateFilteredQuery();

        assertThat(query).endsWith("1 = 1");
    }

    @Test
    public void test_generateFilteredQuery_filterByDocTypes() {
        filter.setDocTypeIds(Arrays.asList(1L, 2L));
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("doc_id in (1, 2)");
    }

    @Test
    public void test_generateFilteredQuery_filterByDocNumber() {
        filter.setDocumentNumber("D-1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("doc_number");
        assertThat(query).contains("like '%D1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByCitizenshipCountry() {
        filter.setCitizenshipCountryIds(Arrays.asList(1L, 2L));
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and citizenship_country.id in (1, 2)");
    }

    @Test
    public void test_generateFilteredQuery_filterByTaxpayerStates() {
        filter.setTaxpayerStateIds(Arrays.asList(1L, 2L));
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and person.taxpayer_state in (1, 2)");
    }

    @Test
    public void test_generateFilteredQuery_filterByAsnu() {
        filter.setSourceSystemIds(Arrays.asList(1L, 2L));
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and person.source_id in (1, 2)");
    }

    @Test
    public void test_generateFilteredQuery_filterByInp() {
        filter.setInp("D-1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("inp like '%D-1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByInn() {
        filter.setInn("D-1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("and person.inn like '%D-1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByInnForeign() {
        filter.setInnForeign("D-1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("and person.inn_foreign like '%D-1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterBySnils() {
        filter.setSnils("D-1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("person.snils");
        assertThat(query).contains("like '%D1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByAddress() {
        filter.setPostalCode("394000");
        filter.setRegion("36");
        filter.setDistrict("Ново-Усманский");
        filter.setLocality("с. Отрадное");
        filter.setCity("Воронеж");
        filter.setStreet("пр. Революции");

        String query = generator.generateFilteredQuery();

        assertThat(query).contains("and postal_code like '%394000%'");
        assertThat(query).contains("and region_code like '%36%'");
        assertThat(query).contains("and district like '%НОВО-УСМАНСКИЙ%'");
        assertThat(query).contains("and locality like '%С. ОТРАДНОЕ%'");
        assertThat(query).contains("and city like '%ВОРОНЕЖ%'");
        assertThat(query).contains("and street like '%ПР. РЕВОЛЮЦИИ%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByForeignAddressCountry() {
        filter.setCountryIds(Arrays.asList(1L, 2L));
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("and address_country.id in (1, 2)");
    }

    @Test
    public void test_generateFilteredQuery_filterByForeignAddress() {
        filter.setForeignAddress("г. Алматы, ул. Пушкина");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("and person.address_foreign like '%Г. АЛМАТЫ, УЛ. ПУШКИНА%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByNotDuplicates() {
        filter.setDuplicates(false);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and person.record_id = person.old_id");
    }

    @Test
    public void test_generateFilteredQuery_filterByDuplicatesOnly() {
        filter.setDuplicates(true);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("and person.record_id <> person.old_id");
    }

    @Test
    public void test_generateFilteredQuery_filterByAllVersions() {
        filter.setAllVersions(true);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("1 = 1");
    }

    @Test
    public void test_generateFilteredQuery_filterByVersions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JANUARY, 1);
        filter.setVersionDate(calendar.getTime());
        filter.setAllVersions(false);

        String query = generator.generateFilteredQuery();

        assertThat(query).endsWith("where start_date <= date '2000-01-01' and (end_date >= date '2000-01-01' or end_date is null)");
    }


    @Test
    public void test_generatePagedAndFilteredQuery_noPagingParams() {
        generator = new SelectPersonQueryGenerator(null, null);
        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("asnu.id = person.source_id");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_defaultOrdersById() {
        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).contains("select /*+ index_asc(person PK_REF_BOOK_PERSON) parallel(person,8) first_rows(1)*/");
        assertThat(query).contains("order by id asc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByFirstName() {
        pagingParams.setNoPaging();
        pagingParams.setProperty("firstName");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by first_name asc, id asc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByFirstNameDesc() {
        pagingParams.setNoPaging();
        pagingParams.setProperty("firstName");
        pagingParams.setDirection("desc");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by first_name desc, id desc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByVip() {
        pagingParams.setNoPaging();
        pagingParams.setProperty("vip");
        pagingParams.setDirection("asc");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by vip desc, id asc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByAddressDesc() {
        pagingParams.setNoPaging();
        pagingParams.setProperty("address");
        pagingParams.setDirection("desc");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by vip asc, postal_code desc, region_code desc, district desc, city desc, " +
                "locality desc, street desc, house desc, building desc, apartment desc, id desc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_pagination() {
        pagingParams.setCount(20);
        pagingParams.setPage(2);
        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).contains("where rownum <= 40");
        assertThat(query).endsWith("where rnum > 20");
    }
}