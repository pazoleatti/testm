package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.identity.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import oracle.jdbc.OracleTypes;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * @author Andrey Drunk
 */
@Ignore("Включать только локально, со включенным тестом не коммитить!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ForOracleTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RefBookPersonDaoOracleTest {

    @Autowired
    RefBookSimpleDao refBookSimpleDao;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    NdflPersonDao ndflPersonDao;

    @Autowired
    RefBookPersonDao refBookPersonDao;


    private void printResult(long time, int size){
        System.out.println("Fetched "+size+" rows  in "+(System.currentTimeMillis() - time) +" ms");
    }

    //14873
    //Для ФЛ Номер: 0765540960: Петров Матвей Юрьевич код: 21, 80 04 505050 сходных записей найдено: 2 [ИНП: 0765540960: Борисова Марфа Юрьевна 88 08 010203 (0,42)][ИНП: 0765540960: Петров Матвей Юрьевич 80 04 505050 (1,00)]. Выбрана запись: [ИНП: 0765540960: Петров Матвей Юрьевич 80 04 505050 (1,00)]
    private  static final Long decl_data_id = 15161L; //вставка
    //private  static final Long decl_data_id = 14873L; //обновление

    @Test
    public void testFindNdflPerson() {
        long time = System.currentTimeMillis();
        Map<Long, List<PersonData>> result =  refBookPersonDao.findRefBookPersonByPrimaryRnuNdfl(decl_data_id, 1L, new Date());
        printResult(time, result.size());
    }

    @Test
    public void testFindNdflPersonFunc() {
        long time = System.currentTimeMillis();
        Map<Long, Map<Long, NaturalPerson>> naturalPersonList = refBookPersonDao.findRefBookPersonByPrimaryRnuNdflFunction(decl_data_id, 1L, new Date());
        printResult(time, naturalPersonList.size());
    }

    @Test
    public void testFindNdflPeron() {
        long time = System.currentTimeMillis();
        List<NdflPerson> result =  ndflPersonDao.findPerson(14730L);
        printResult(time, result.size());
    }
}
