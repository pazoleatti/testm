package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.EventDao;
import com.aplana.sbrf.taxaccounting.model.TARole;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * User: avanteev
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"EventDaoTest.xml"})
@Transactional(readOnly = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EventDaoTest {

    @Autowired
    private EventDao eventDao;

    @Test
    public void testGetEventCodes(){
        Assert.assertEquals(14, eventDao.getEventCodes(TARole.N_ROLE_CONTROL_UNP, Arrays.asList(501, 701),"10_", "9__").size());
        Assert.assertEquals(64, eventDao.getEventCodes(TARole.N_ROLE_CONTROL_UNP, null).size());
    }
}
