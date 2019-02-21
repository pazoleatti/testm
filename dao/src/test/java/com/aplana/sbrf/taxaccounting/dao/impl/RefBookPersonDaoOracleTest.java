package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper;
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    RefBookPersonDao refBookPersonDao;

    @Autowired
    NdflPersonDao ndflPersonDao;


    private void printResult(long time, int size) {
        System.out.println("Fetched " + size + " rows  in " + (System.currentTimeMillis() - time) + " ms");
    }

    //14873
    //private static final Long decl_data_id = 148731L; //вставка
    //private static final Long decl_data_id = 14873L; //обновление
    private static final Long decl_data_id = 15491L; //дубликаты


    private Map<Long, Map<String, RefBookValue>> getRefBookByRecordVersionWhere(long refBookId, String whereClause, Date version) {
        RefBook refBook = refBookDao.get(refBookId);
        Map<Long, Map<String, RefBookValue>> refBookMap = refBookSimpleDao.getRecordDataVersionWhere(refBook, whereClause, version);
        if (refBookMap == null || refBookMap.size() == 0) {
            return Collections.emptyMap();
        }
        return refBookMap;
    }


    @Test
    public void testFindNaturalPersonPrimaryDataFromNdfl() {
        long time = System.currentTimeMillis();
        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(decl_data_id, new NaturalPersonPrimaryRnuRowMapper());
        printResult(time, result.size());
    }

    @Test
    public void testFindNdflPersonFunc() {

        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(decl_data_id, new NaturalPersonPrimaryRnuRowMapper());

        long time = System.currentTimeMillis();

        Date version = new Date();

        refBookPersonDao.fillRecordVersions();

        int size = 0;

        Map<Long, Map<Long, NaturalPerson>> updateRecords = refBookPersonDao.findPersonForUpdateFromPrimaryRnuNdfl(decl_data_id, new NaturalPersonRefbookHandler());

        size += updateRecords.size();

        System.out.println("   updateRecords=" + updateRecords);

        Map<Long, Map<Long, NaturalPerson>> checkRecords = refBookPersonDao.findPersonForCheckFromPrimaryRnuNdfl(decl_data_id, new NaturalPersonRefbookHandler());

        size += checkRecords.size();

        System.out.println("   checkRecords=" + checkRecords);

        List<NaturalPerson> result2 = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(decl_data_id, new NaturalPersonPrimaryRnuRowMapper());

        printResult(time, size);

    }


    public void insertPersonRecords(List<? extends IdentityObject> identityObjectList) {
        List<RefBookRecord> recordList = new ArrayList<RefBookRecord>();
        for (IdentityObject identityObject : identityObjectList) {
            System.out.println("identityObject=" + identityObject);
        }
    }


    @Test
    public void testFindNdflPeron() {
        long time = System.currentTimeMillis();
        List<NdflPerson> result = ndflPersonDao.findAllByDeclarationId(14730L);
        printResult(time, result.size());
    }

    @Test
    public void testFindNaturalPersonPrimaryData() {
        long time = System.currentTimeMillis();

        NaturalPersonPrimaryRnuRowMapper rowMapper = new NaturalPersonPrimaryRnuRowMapper();
        RefBookAsnu asnu = new RefBookAsnu();
        asnu.setId(1L);
        rowMapper.setAsnu(asnu);

        Long declarationDataId = 14873L;
        //Long declarationDataId = 14730L;

        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(declarationDataId, rowMapper);
        printResult(time, result.size());
    }
}
