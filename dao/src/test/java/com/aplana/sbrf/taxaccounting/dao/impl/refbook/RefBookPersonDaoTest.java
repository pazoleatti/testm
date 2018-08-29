package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


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
        assertThat(personVersions.size(), is(1));
        assertThat(personVersions.get(0).getId(), is (1L));
    }
}
