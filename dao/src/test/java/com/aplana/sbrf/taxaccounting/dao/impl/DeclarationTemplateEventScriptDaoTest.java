package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("DeclarationTemplateEventScriptDaoTest.xml")
@Transactional
@Ignore // Временно отключил пока не добавлена decl_template_event_script в create_main.sql
public class DeclarationTemplateEventScriptDaoTest {

    @Autowired
    DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao;

    @Test
    public void fetch(){

    }
}
