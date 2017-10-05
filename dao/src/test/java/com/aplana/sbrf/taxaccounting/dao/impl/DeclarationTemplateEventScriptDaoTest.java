package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("DeclarationTemplateEventScriptDaoTest.xml")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DeclarationTemplateEventScriptDaoTest {

    @Autowired
    DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao;

    @Test
    public void fetch(){
        List<DeclarationTemplateEventScript> declarationTemplateEventScriptList = declarationTemplateEventScriptDao.fetch(1);
        Assert.assertEquals(2, declarationTemplateEventScriptList.size());
    }

    @Test
    public void getScript() {
        String script = declarationTemplateEventScriptDao.getScript(1);
        Assert.assertEquals("Hello 1 calc", script);
    }

    @Test
    public void findScript() {
        String script = declarationTemplateEventScriptDao.findScript(1, 5);
        Assert.assertEquals("Hello 1 check", script);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateScript() {
        String scriptFixture = "Test Script";
        declarationTemplateEventScriptDao.updateScript(3, scriptFixture);
        String script = declarationTemplateEventScriptDao.getScript(3);
        Assert.assertEquals(scriptFixture, script);
    }

    @Test
    public void checkIfEventScriptPresent() {
        boolean present = declarationTemplateEventScriptDao.checkIfEventScriptPresent(2, 3);
        boolean notPresent = declarationTemplateEventScriptDao.checkIfEventScriptPresent(2, 4);
        Assert.assertTrue(present);
        Assert.assertFalse(notPresent);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void delete() {
        declarationTemplateEventScriptDao.delete(3);
        Assert.assertEquals(0, declarationTemplateEventScriptDao.fetch(2).size());
    }
}
