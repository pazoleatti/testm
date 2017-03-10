package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonPrimaryRnuRowMapper;
import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    @Autowired
    RefBookPersonDao refBookPersonDao;

    @Autowired
    NdflPersonDao ndflPersonDao;


    private void printResult(long time, int size) {
        System.out.println("Fetched " + size + " rows  in " + (System.currentTimeMillis() - time) + " ms");
    }

    //14873
    //Для ФЛ Номер: 0765540960: Петров Матвей Юрьевич код: 21, 80 04 505050 сходных записей найдено: 2 [ИНП: 0765540960: Борисова Марфа Юрьевна 88 08 010203 (0,42)][ИНП: 0765540960: Петров Матвей Юрьевич 80 04 505050 (1,00)]. Выбрана запись: [ИНП: 0765540960: Петров Матвей Юрьевич 80 04 505050 (1,00)]
    private static final Long decl_data_id = 15161L; //вставка
    //private  static final Long decl_data_id = 14873L; //обновление

    @Test
    public void testFindNdflPerson() {
        long time = System.currentTimeMillis();
        Map<Long, List<PersonData>> result = refBookPersonDao.findRefBookPersonByPrimaryRnuNdfl(decl_data_id, 1L, new Date());
        printResult(time, result.size());
    }

    @Test
    public void testFindNdflPersonFunc() {

        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(decl_data_id, new NaturalPersonPrimaryRnuRowMapper());

        System.out.println("Всего записей в ПНФ: decl_data_id=" + decl_data_id + ", size=" + result.size());

        long time = System.currentTimeMillis();

        Date version = new Date();

        refBookPersonDao.fillRecordVersions(version);

        int size = 0;

        List<NaturalPerson> insertRecords = refBookPersonDao.findPersonForInsertFromPrimaryRnuNdfl(decl_data_id, 1L, version, new NaturalPersonPrimaryRnuRowMapper());

        size += insertRecords.size();

        System.out.println("   insertRecords=" + insertRecords);

        PersonDocument doc = insertRecords.get(0).getPersonDocument();

        System.out.println("   insertRecords=" + doc);

        Map<Long, Map<Long, NaturalPerson>> updateRecords = refBookPersonDao.findPersonForUpdateFromPrimaryRnuNdfl(decl_data_id, 1L, version, new NaturalPersonRefbookHandler());

        size += updateRecords.size();

        System.out.println("   updateRecords=" + updateRecords);


        Map<Long, Map<Long, NaturalPerson>> checkRecords = refBookPersonDao.findPersonForCheckFromPrimaryRnuNdfl(decl_data_id, 1L, version, new NaturalPersonRefbookHandler());

        size += checkRecords.size();

        System.out.println("   checkRecords=" + checkRecords);

        List<NaturalPerson> result2 = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(decl_data_id, new NaturalPersonPrimaryRnuRowMapper());

        //System.out.println("Всего записей в ПНФ: decl_data_id=" + decl_data_id + ", size=" + result2.size());

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
        List<NdflPerson> result = ndflPersonDao.findPerson(14730L);
        printResult(time, result.size());
    }

    @Test
    public void testFindNaturalPersonPrimaryData() {
        long time = System.currentTimeMillis();

        NaturalPersonPrimaryRnuRowMapper rowMapper = new NaturalPersonPrimaryRnuRowMapper();
        rowMapper.setAsnuId(1L);

        Long declarationDataId = 14873L;
        //Long declarationDataId = 14730L;

        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFromNdfl(declarationDataId, rowMapper);
        printResult(time, result.size());
    }


    //1151111
    private static final Long decl_data115_id = 15143L; //вставка

    @Test
    public void testFindNaturalPersonPrimaryData1151111() {
        long time = System.currentTimeMillis();

        NaturalPersonPrimaryRnuRowMapper rowMapper = new NaturalPersonPrimaryRnuRowMapper();


        Long declarationDataId = 15297L;
        //Long declarationDataId = 14730L;

        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFrom1151111(declarationDataId, rowMapper);
        printResult(time, result.size());
    }

    @Test
    public void testFind115PersonFunc() {

        List<NaturalPerson> result = refBookPersonDao.findNaturalPersonPrimaryDataFrom1151111(decl_data115_id, new NaturalPersonPrimaryRnuRowMapper());

        System.out.println("Всего записей в ПНФ: decl_data_id=" + decl_data115_id + ", size=" + result.size());

        long time = System.currentTimeMillis();

        Date version = new Date();

        refBookPersonDao.fillRecordVersions(version);

        int size = 0;

        List<NaturalPerson> insertRecords = refBookPersonDao.findPersonForInsertFromPrimary1151111(decl_data115_id, 1L, version, new NaturalPersonPrimaryRnuRowMapper());

        size += insertRecords.size();

        System.out.println("   insertRecords=" + insertRecords);


        Map<Long, Map<Long, NaturalPerson>> updateRecords = refBookPersonDao.findPersonForUpdateFromPrimary1151111(decl_data115_id, 1L, version, new NaturalPersonRefbookHandler());

        size += updateRecords.size();

        System.out.println("   updateRecords=" + updateRecords);


        Map<Long, Map<Long, NaturalPerson>> checkRecords = refBookPersonDao.findPersonForCheckFromPrimary1151111(decl_data115_id, 1L, version, new NaturalPersonRefbookHandler());

        size += checkRecords.size();

        System.out.println("   checkRecords=" + checkRecords);

        List<NaturalPerson> result2 = refBookPersonDao.findNaturalPersonPrimaryDataFrom1151111(decl_data115_id, new NaturalPersonPrimaryRnuRowMapper());

        //System.out.println("Всего записей в ПНФ: decl_data_id=" + decl_data_id + ", size=" + result2.size());

        printResult(time, size);

    }


}
