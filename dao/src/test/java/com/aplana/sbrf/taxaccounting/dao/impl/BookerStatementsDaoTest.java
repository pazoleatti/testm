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

    @Before
    public void init() throws FileNotFoundException {
        for (int i = 1; i < 10; i++) {
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
    public void test101() {
        bookerStatementsDao.create101(list101, 1);
        //TODO
    }

    @Test
    public void test102() {
        bookerStatementsDao.create102(list102, 1);
        //TODO
    }

}
