package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Date;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TemplateChanges.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TemplateChangesDaoTest {

    @Autowired
    private TemplateChangesDao templateChangesDao;

    @Autowired
    private TAUserDao taUserDao;

    @Test
    public void testAdd(){
        TemplateChanges templateChanges = new TemplateChanges();
        templateChanges.setAuthor(taUserDao.getUser(1));
        templateChanges.setFormTemplateId(2);
        templateChanges.setEventDate(new Date());
        templateChanges.setEvent(FormDataEvent.TEMPLATE_ACTIVATED);
        int newEventId = templateChangesDao.add(templateChanges);
        Assert.assertEquals(newEventId, templateChangesDao.getByFormTemplateId(2, VersionHistorySearchOrdering.DATE, false).get(0).getId());
    }

    @Test
    public void testGetByFormTemplateId(){
        Assert.assertEquals(1, templateChangesDao.getByFormTemplateId(1, VersionHistorySearchOrdering.DATE, false).get(0).getId());
    }

    @Test
    public void testGetByTypeId(){
        Assert.assertEquals(1, templateChangesDao.getByFormTypeIds(1, VersionHistorySearchOrdering.DATE, false).size());
        Assert.assertEquals(2, templateChangesDao.getByDeclarationTypeId(1, VersionHistorySearchOrdering.DATE, false).size());
    }

    @Test
    public void testGetByFTIds(){
        Assert.assertEquals(1, templateChangesDao.getByFormTemplateIds(Arrays.asList(1), VersionHistorySearchOrdering.DATE, false).size());
    }

    @Test
    public void testGetIdsByFTIds(){
        Assert.assertEquals(1, templateChangesDao.getIdsByTemplateIds(Arrays.asList(1), null, VersionHistorySearchOrdering.DATE, false).size());
    }

    @Test
    public void testDelete(){
        templateChangesDao.delete(Arrays.asList(1));
        Assert.assertEquals(0, templateChangesDao.getByFormTemplateId(1, VersionHistorySearchOrdering.DATE, false).size());
    }
}
