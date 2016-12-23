package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Andrey Drunk
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NdflPersonDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NdflPersonDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private NdflPersonDao ndflPersonDao;

    @Test
    public void testGet() {
        NdflPerson ndflPerson = ndflPersonDao.get(1);
        assertNotNull(ndflPerson);
    }

    public void findAllTest(){
       List<NdflPerson> result = ndflPersonDao.findAll();
       assertEquals(1, result.size());
    }

    @Test
    public void testGoodSave() {

        NdflPerson goodNdflPerson = createGoodNdflPerson();
        Long id = ndflPersonDao.save(goodNdflPerson);

        Assert.assertNotNull(id);

        NdflPerson ndflPerson = ndflPersonDao.get(id);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(goodNdflPerson, ndflPerson));
    }

    @Test(expected=DaoException.class)
    public void testBadSave()  {
        NdflPerson person = createGoodNdflPerson();
        person.setInp(null);
        Long id = ndflPersonDao.save(person);
    }


    public static Date toDate(String dateStr) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }




    private NdflPerson createGoodNdflPerson() {

        NdflPerson person = new NdflPerson();
        person.setId(null);
        person.setDeclarationDataId(1L);
        person.setInp("000-000-000-00");
        person.setSnils("123-321-111-11");
        person.setLastName("Иванов");
        person.setFirstName("Иван");
        person.setMiddleName("Иванович");
        person.setBirthDay(toDate("01.01.1980"));
        person.setCitizenship("643");

        person.setInnNp("123456789123");
        person.setInnForeign("");
        person.setIdDocType("010203");
        person.setIdDocNumber("2002 123456");
        person.setStatus("11");
        person.setPostIndex("394000");
        person.setRegionCode("77");
        person.setArea("MSK");
        person.setCity("");

        person.setLocality("");
        person.setStreet(null);
        person.setHouse("");
        person.setBuilding("");
        person.setFlat("");
        person.setCountryCode("643");
        person.setAddress("");
        person.setAdditionalData("");


        //List<NdflPersonIncome> ndflPersonIncomes = new ArrayList<NdflPersonIncome>();
        //ndflPersonIncomes.add(createNdflPersonIncomes(1));
        //ndflPersonIncomes.add(createNdflPersonIncomes(2));
        //ndflPersonIncomes.add(createNdflPersonIncomes(3));
        //person.setNdflPersonIncomes(ndflPersonIncomes);

        return person;
    }


    private NdflPersonIncome createNdflPersonIncomes(int row) {
        NdflPersonIncome personIncome = new NdflPersonIncome();
        personIncome.setRowNum(row);
        return personIncome;
    }


}
