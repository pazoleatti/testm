package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookAttachFileTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttachFileType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookAttachFileTypeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookAttachFileTypeDaoTest {
    private final static int ATTACH_FILE_TYPE_COUNT = 6;

    @Autowired
    private RefBookAttachFileTypeDao refBookAttachFileTypeDao;

    @Test
    public void getRecordData() {
        List<RefBookAttachFileType> attachFileTypes = refBookAttachFileTypeDao.fetchAll();
        assertTrue(attachFileTypes.size() == ATTACH_FILE_TYPE_COUNT);
    }
}
