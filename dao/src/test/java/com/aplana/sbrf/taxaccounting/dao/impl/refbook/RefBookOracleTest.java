package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckResult;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore("Включать только локально, со включенным тестом не коммитить!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"../ForOracleTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookOracleTest {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    RefBookDao refBookDao;

    @Before
    public void init() {
        String script = "classpath:data/RefBookOracle.sql";
        Resource resource = ctx.getResource(script);
        JdbcTestUtils.executeSqlScript((JdbcTemplate) namedParameterJdbcTemplate.getJdbcOperations(), resource, true);
    }

    @Test
    public void getInactiveRecordsInPeriodTest() throws ParseException {
        Map<Long, CheckResult> result = refBookDao.getInactiveRecordsInPeriod(Arrays.asList(18L, 19L, 20L, 22L, 24L, 25L, 28L),
                sdf.parse("01.01.2016"), sdf.parse("31.12.2016"));
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals(null, result.get(18L));
        assertEquals(CheckResult.NOT_CROSS, result.get(19L));
        assertEquals(CheckResult.NOT_CROSS, result.get(20L));
        assertEquals(null, result.get(22L));
        assertEquals(CheckResult.NOT_EXISTS, result.get(24L));
        assertEquals(CheckResult.NOT_LAST, result.get(25L));
        assertEquals(CheckResult.NOT_EXISTS, result.get(28L));

        //Проверяем результат только с несуществующими записями
        result = refBookDao.getInactiveRecordsInPeriod(Arrays.asList(12345L),
                sdf.parse("01.01.2016"), sdf.parse("31.12.2016"));
        assertNotNull(result);
        assertTrue(!result.isEmpty());
        assertEquals(CheckResult.NOT_EXISTS, result.get(12345L));
    }
}
