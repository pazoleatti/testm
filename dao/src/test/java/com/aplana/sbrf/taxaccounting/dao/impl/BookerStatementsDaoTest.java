package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsDao;
import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Тест дао для формы "Загрузка бухгалтерской отчётности из xls"
 *
 * @author Stanislav Yasinskiy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("BookerStatementsDaoTest.xml")
@Transactional
public class BookerStatementsDaoTest {

    @Autowired
    BookerStatementsDao bookerStatementsDao;

    List<Income101> list101 = new ArrayList<Income101>();
    List<Income102> list102 = new ArrayList<Income102>();
    static final int REPORT_PERIOD_ID = 1;

    @Before
    public void init() throws FileNotFoundException {
        for (int i = 0; i < 10; i++) {
            Income101 model = new Income101();
            model.setAccount("Account " + i);
            model.setAccountName("AccountName" + i);
            model.setIncomeDebetRemains(10D + i);
            model.setIncomeCreditRemains(20D + i);
            model.setDebetRate(30D + i);
            model.setCreditRate(40D + i);
            model.setOutcomeDebetRemains(50D + i);
            model.setOutcomeCreditRemains(60D + i);
            list101.add(model);

            Income102 model2 = new Income102();
            model2.setItemName("ItemName " + i);
            model2.setOpuCode("OpuCode " + i);
            model2.setTotalSum(10D + i);
            list102.add(model2);
        }
    }

    @Test
    public void testSimple() {
        assertNotNull(bookerStatementsDao);
    }

    @Test
    public void delete101Test() {
        assertEquals(bookerStatementsDao.delete101(REPORT_PERIOD_ID), 2);
        assertEquals(bookerStatementsDao.getIncome101(REPORT_PERIOD_ID).size(), 0);
    }

    @Test
    public void delete102Test() {
        assertEquals(bookerStatementsDao.delete102(REPORT_PERIOD_ID), 3);
        assertEquals(bookerStatementsDao.getIncome102(REPORT_PERIOD_ID).size(), 0);
    }

    @Test
    public void create101Test() {
        bookerStatementsDao.delete101(REPORT_PERIOD_ID);
        bookerStatementsDao.create101(list101, REPORT_PERIOD_ID);
        assertEquals(bookerStatementsDao.getIncome101(REPORT_PERIOD_ID).size(), list101.size());
    }

    @Test
    public void create102Test() {
        bookerStatementsDao.delete102(REPORT_PERIOD_ID);
        bookerStatementsDao.create102(list102, REPORT_PERIOD_ID);
        assertEquals(bookerStatementsDao.getIncome102(REPORT_PERIOD_ID).size(), list102.size());
    }

    @Test
    public void getIncome101Test() {
        List<Income101> income101List = bookerStatementsDao.getIncome101(REPORT_PERIOD_ID);
        assertEquals(income101List.get(0).getIncomeDebetRemains(), 3, 1e-5);
        assertTrue(bookerStatementsDao.getIncome101(REPORT_PERIOD_ID).size() == 2);
    }

    @Test
    public void getIncome102Test() {
        assertEquals(bookerStatementsDao.getIncome102(2).get(0).getTotalSum(), new Double(555));
        assertTrue(bookerStatementsDao.getIncome102(REPORT_PERIOD_ID).size() == 3);
    }
}
