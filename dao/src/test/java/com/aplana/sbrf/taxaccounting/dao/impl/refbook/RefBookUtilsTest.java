package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author auldanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookUtilsTest.xml" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookUtilsTest {

    @Test
    public void checkFillRequiredRefBookAtributesTest(){
        // Атрибуты справочника герои Великой Отечественной войны
        // Имя героя ВОВ
        RefBookAttribute name = new RefBookAttribute();
        name.setAlias("NAME");
        name.setAttributeType(RefBookAttributeType.STRING);
        name.setId(1L);
        name.setName("Имя");
        name.setRequired(true);
        // Возраст героя ВОВ
        RefBookAttribute birthyear = new RefBookAttribute();
        birthyear.setAlias("BIRTHYEAR");
        birthyear.setAttributeType(RefBookAttributeType.NUMBER);
        birthyear.setId(2L);
        birthyear.setName("Год рождения");
        birthyear.setRequired(false);

        List<RefBookAttribute> attributes = new ArrayList();
        attributes.add(name);
        attributes.add(birthyear);

        Map<String, RefBookValue> records1 = new HashMap<String, RefBookValue>();
        records1.put("NAME", new RefBookValue(RefBookAttributeType.STRING, "Виктор Васильевич Талалихин"));
        records1.put("BIRTHYEAR", new RefBookValue(RefBookAttributeType.STRING, "1918"));
        // все заполнено, все ок
        assertTrue(RefBookUtils.checkFillRequiredRefBookAtributes(attributes, records1).isEmpty());

        Map<String, RefBookValue> records2 = new HashMap<String, RefBookValue>();
        records2.put("BIRTHYEAR", new RefBookValue(RefBookAttributeType.STRING, "1918"));
        // не заполнено имя, метод должен вернуть мапу с NAME
        assertTrue(RefBookUtils.checkFillRequiredRefBookAtributes(attributes, records2).size() == 1);
    }

    @Test
    public void checkControlSumInnTest() {
        // длина больше 10, но для проверки используются только первые 10 символов
        assertTrue(RefBookUtils.checkControlSumInn("7723643863"));
        assertTrue(RefBookUtils.checkControlSumInn("500100732259"));
        assertFalse(RefBookUtils.checkControlSumInn("7723643862"));
        assertFalse(RefBookUtils.checkControlSumInn("111"));
        assertFalse(RefBookUtils.checkControlSumInn("abcderfsdf"));
    }

}
