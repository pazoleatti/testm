package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookFormType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Тест для справочника "Виды налоговых форм"
 */
@Transactional
@RunWith(SpringRunner.class)
@ContextConfiguration({"RefBookFormTypeDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookFormTypeDaoImplTest {

    @Autowired
    private RefBookFormTypeDao refBookFormTypeDao;

    @Test
    public void fetchAllTest() {
        List<RefBookFormType> refBookFormTypes = refBookFormTypeDao.fetchAll();
        Assert.assertEquals(5, refBookFormTypes.size());
    }

}