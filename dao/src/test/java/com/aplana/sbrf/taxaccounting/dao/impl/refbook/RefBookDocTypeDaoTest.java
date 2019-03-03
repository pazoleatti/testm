package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDocTypeDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@ContextConfiguration({"RefBookDocTypeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDocTypeDaoTest {

    @Autowired
    private RefBookDocTypeDao docTypeDao;

    @Test
    public void test_existsByCode_byExisting() {
        boolean result = docTypeDao.existsByCode("21");
        assertThat(result).isTrue();
    }

    @Test
    public void test_existsByCode_byEmpty() {
        boolean result = docTypeDao.existsByCode("");
        assertThat(result).isFalse();
    }

    @Test
    public void test_existsByCode_byNonexistent() {
        boolean result = docTypeDao.existsByCode("100");
        assertThat(result).isFalse();
    }
}
