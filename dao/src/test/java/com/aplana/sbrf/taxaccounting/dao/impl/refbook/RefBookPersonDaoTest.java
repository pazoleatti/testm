package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookPersonDaoTest {

    @Autowired
    private RefBookPersonDaoImpl refBookPersonDao;

    @Test
    public void test_fetchOriginal() {
        List<RegistryPerson> personVersions = refBookPersonDao.fetchOriginal(3L);
        assertThat(personVersions).hasSize(1);
        assertThat(personVersions.get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void test_generateSortParams_forNullValues() {
        // given
        PagingParams pagingParams = new PagingParams();
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams).isEqualTo("order by id asc");
    }

    @Test
    public void test_generateSortParams_forEmptyValues() {
        // given
        PagingParams pagingParams = new PagingParams("", "");
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams).isEqualTo("order by id asc");
    }

    @Test
    public void test_generateSortParams_forSimpleValue() {
        // given
        PagingParams pagingParams = new PagingParams("firstName", "desc");
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams).isEqualTo("order by first_name desc, id desc");
    }

    @Test
    public void test_generateSortParams_forVip() {
        // given
        PagingParams pagingParams = new PagingParams("vip", "desc");
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams).isEqualTo("order by vip asc, id asc");
    }

    @Test
    public void test_generateSortParams_forPermissiveFieldAsc() {
        // given
        PagingParams pagingParams = new PagingParams("inn", "asc");
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams).isEqualTo("order by vip desc, inn asc, id asc");
    }

    @Test
    public void test_generateSortParams_forPermissiveFieldDesc() {
        // given
        PagingParams pagingParams = new PagingParams("inn", "desc");
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams).isEqualTo("order by vip asc, inn desc, id desc");
    }

    @Test
    public void test_generateSortParams_forAddressAsc() {
        // given
        PagingParams pagingParams = new PagingParams("address", "asc");
        // when
        String sortParams = RefBookPersonDaoImpl.generateSortParams(pagingParams);
        // then
        assertThat(sortParams)
                .isEqualTo("order by vip desc, postal_code asc, region_code asc, district asc, city asc, " +
                        "locality asc, street asc, house asc, building asc, apartment asc, address_id asc, id asc");
    }
}
