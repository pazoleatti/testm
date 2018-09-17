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
        assertThat(query).endsWith("where person.status = 0");
    }

    @Test
    public void test_generateFilteredQuery_filterById() {
        filter.setId("D1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("like '%D1%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByName() {
        filter.setFirstName("John");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("like '%john%'");
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
        filter.setDocumentTypes(new ArrayList<Long>());

        String query = generator.generateFilteredQuery();

        assertThat(query).endsWith("where person.status = 0");
    }

    @Test
    public void test_generateFilteredQuery_filterByDocTypes() {
        filter.setDocumentTypes(Arrays.asList(1L, 2L));
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("doc_id in (1, 2)");
    }

    @Test
    public void test_generateFilteredQuery_filterByDocNumber() {
        filter.setDocumentNumber("D-1");
        String query = generator.generateFilteredQuery();
        assertThat(query).contains("doc_number");
        assertThat(query).contains("like '%d1%'");
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

        assertThat(query).contains("and lower(postal_code) like '%394000%'");
        assertThat(query).contains("and lower(region_code) like '%36%'");
        assertThat(query).contains("and lower(district) like '%ново-усманский%'");
        assertThat(query).contains("and lower(locality) like '%с. отрадное%'");
        assertThat(query).contains("and lower(city) like '%воронеж%'");
        assertThat(query).contains("and lower(street) like '%пр. революции%'");
    }

    @Test
    public void test_generateFilteredQuery_filterByAllVersions() {
        filter.setAllVersions(true);
        String query = generator.generateFilteredQuery();
        assertThat(query).endsWith("where person.status = 0");
    }

    @Test
    public void test_generateFilteredQuery_filterByVersions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JANUARY, 1);
        filter.setVersionDate(calendar.getTime());
        filter.setAllVersions(false);

        String query = generator.generateFilteredQuery();

        assertThat(query).endsWith("where version <= date '2000-01-01' and (version_to >= date '2000-01-01' or version_to is null)");
    }


    @Test
    public void test_generatePagedAndFilteredQuery_noPagingParams() {
        generator = new SelectPersonQueryGenerator(null, null);
        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("where person.status = 0");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_defaultOrdersById() {
        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).contains("order by id asc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByFirstName() {
        pagingParams.setWithoutPaging();
        pagingParams.setProperty("firstName");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by first_name asc, id asc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByFirstNameDesc() {
        pagingParams.setWithoutPaging();
        pagingParams.setProperty("firstName");
        pagingParams.setDirection("desc");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by first_name desc, id desc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByVip() {
        pagingParams.setWithoutPaging();
        pagingParams.setProperty("vip");
        pagingParams.setDirection("asc");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by vip desc, id asc");
    }

    @Test
    public void test_generatePagedAndFilteredQuery_orderByAddressDesc() {
        pagingParams.setWithoutPaging();
        pagingParams.setProperty("address");
        pagingParams.setDirection("desc");

        String query = generator.generatePagedAndFilteredQuery();
        assertThat(query).endsWith("order by vip asc, postal_code desc, region_code desc, district desc, city desc, " +
                "locality desc, street desc, house desc, building desc, apartment desc, address_id desc, id desc");
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