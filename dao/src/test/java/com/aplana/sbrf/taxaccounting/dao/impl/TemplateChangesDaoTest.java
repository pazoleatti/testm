package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.TemplateChangesDao;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.TemplateChangesEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"TemplateChanges.xml"})
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
        templateChanges.setEvent(TemplateChangesEvent.ACTIVATED);
        int newEventId = templateChangesDao.add(templateChanges);
        Assert.assertEquals(newEventId, templateChangesDao.getByFormTemplateId(2).get(0).getId());
    }

    @Test
    public void testGetByFormTemplateId(){
        Assert.assertEquals(1, templateChangesDao.getByFormTemplateId(1).get(0).getId());
    }
}
