package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.FiasRefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckAddressResult;
import com.aplana.sbrf.taxaccounting.model.refbook.FiasCheckInfo;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author Andrey Drunk
 */
@Ignore("Включать только локально, со включенным тестом не коммитить!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ForOracleTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FiasRefBookDaoOracleTest {

    @Autowired
    private FiasRefBookDao fiasRefBookDao;


    @Test
    public void testCheckAddressByFias() {
        Map<Long, FiasCheckInfo> result = fiasRefBookDao.checkAddressByFias(15563L);

        int i = 0;
        for (Long key : result.keySet()) {

            Long v = result.get(key).getFiasId();
            if (v == null) {
                System.out.print(key + ", ");
                i++;
            }
        }

        System.out.println("Address not found: " + i + " of " + result.size());
        System.out.println(result);
    }

    @Test
    public void testExistAddressByFias() {
        Map<Long, CheckAddressResult> result = fiasRefBookDao.checkExistsAddressByFias(15563L);
        System.out.println(result);
    }

}
