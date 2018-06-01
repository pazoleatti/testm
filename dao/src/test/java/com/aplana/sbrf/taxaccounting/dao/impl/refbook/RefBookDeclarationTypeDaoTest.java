package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookDeclarationTypeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDeclarationTypeDaoTest {
    private final static int DECLARATION_TYPES_COUNT = 5;
    private final static String DECLARATION_TYPE_NAME_2 = "Вид налоговой формы 2";
    private final static String DECLARATION_TYPE_NAME_3 = "Вид налоговой формы 3";

    @Autowired
    private RefBookDeclarationTypeDao declarationTypeDao;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    //Проверка получения всех значений справочника
    @Test
    public void testFetchAll() {
        List<RefBookDeclarationType> declarationTypes = declarationTypeDao.fetchAll();
        assertTrue(declarationTypes.size() == DECLARATION_TYPES_COUNT);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_2));
        assertTrue(declarationTypes.get(1).getName().equals(DECLARATION_TYPE_NAME_3));
    }

    //Проверка получения значений по виду налоговой формы, подразделению и началу отчетного периода
    @Test
    public void testFetchDeclarationTypes() throws ParseException {
        List<RefBookDeclarationType> declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 1, dateFormat.parse("2014-01-01"));
        assertTrue(declarationTypes.size() == 2);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_2));
        assertTrue(declarationTypes.get(1).getName().equals(DECLARATION_TYPE_NAME_3));

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 1, dateFormat.parse("2013-01-01"));
        assertTrue(declarationTypes.size() == 1);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_2));

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 1, dateFormat.parse("2012-01-01"));
        assertTrue(declarationTypes.size() == 0);

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 2, dateFormat.parse("2014-01-01"));
        assertTrue(declarationTypes.size() == 1);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_2));

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 2, dateFormat.parse("2013-01-01"));
        assertTrue(declarationTypes.size() == 1);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_2));

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 2, dateFormat.parse("2012-01-01"));
        assertTrue(declarationTypes.size() == 0);

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 3, dateFormat.parse("2014-01-01"));
        assertTrue(declarationTypes.size() == 1);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_3));

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 3, dateFormat.parse("2013-01-01"));
        assertTrue(declarationTypes.size() == 0);

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 3, dateFormat.parse("2012-01-01"));
        assertTrue(declarationTypes.size() == 0);

        declarationTypes = declarationTypeDao.fetchDeclarationTypes((long) 2, 4, dateFormat.parse("2014-01-01"));
        assertTrue(declarationTypes.size() == 0);
    }
}
