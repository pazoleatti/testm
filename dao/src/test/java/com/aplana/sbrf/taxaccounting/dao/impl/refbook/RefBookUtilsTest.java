package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;


/**
 * @author auldanov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "RefBookUtilsTest.xml" })
public class RefBookUtilsTest {

    private RefBookUtils refBookUtils = new RefBookUtils();

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
        assertTrue(refBookUtils.checkFillRequiredRefBookAtributes(attributes, records1).size() == 0);

        Map<String, RefBookValue> records2 = new HashMap<String, RefBookValue>();
        records2.put("BIRTHYEAR", new RefBookValue(RefBookAttributeType.STRING, "1918"));
        // не заполнено имя, метод должен вернуть мапу с NAME
        assertTrue(refBookUtils.checkFillRequiredRefBookAtributes(attributes, records2).size() == 1);
    }
}
