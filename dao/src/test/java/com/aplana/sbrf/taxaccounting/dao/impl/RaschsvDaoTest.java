package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.raschsv.*;
import com.aplana.sbrf.taxaccounting.model.raschsv.*;
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

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RaschsvDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RaschsvDaoTest {

    @Autowired
    private RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    @Autowired
    private RaschsvObyazPlatSvDao raschsvObyazPlatSvDao;

    @Autowired
    private RaschsvUplPerDao raschsvUplPerDao;

    @Autowired
    private RaschsvUplPrevOssDao raschsvUplPrevOssDao;

    @Autowired
    private RaschsvSvSum1TipDao raschsvSvSum1TipDao;

    @Autowired
    private RaschsvKolLicTipDao raschsvKolLicTipDao;

    @Autowired
    private RaschsvSvOpsOmsDao raschsvSvOpsOmsDao;

    @Autowired
    private RaschsvOssVnmDao raschsvOssVnmDao;

    @Autowired
    private RaschsvRashOssZakDao raschsvRashOssZakDao;

    @Autowired
    private RaschsvVyplFinFbDao raschsvVyplFinFbDao;

    @Autowired
    private RaschsvPravTarif31427Dao raschsvPravTarif31427Dao;

    @Autowired
    private RaschsvPravTarif51427Dao raschsvPravTarif51427Dao;

    @Autowired
    private RaschsvPravTarif71427Dao raschsvPravTarif71427Dao;

    @Autowired
    private RaschsvSvPrimTarif91427Dao raschsvSvPrimTarif91427Dao;

    @Autowired
    private RaschsvSvPrimTarif22425Dao raschsvSvPrimTarif22425Dao;

    @Autowired
    private RaschsvSvPrimTarif13422Dao raschsvSvPrimTarif13422Dao;

    @Autowired
    private RaschsvSvnpPodpisantDao raschsvSvnpPodpisantDao;

    // Идентификатор декларации
    private static final Long DECLARATION_ID_EXIST = 1L;
    private static final Long DECLARATION_ID_NOT_EXIST = 999L;

    /**
     * Добавление записи в таблицу ОбязПлатСВ
     * @return
     */
    private Long createRaschsvObyazPlatSv() {
        RaschsvObyazPlatSv raschsvObyazPlatSv = new RaschsvObyazPlatSv();
        raschsvObyazPlatSv.setDeclarationDataId(DECLARATION_ID_EXIST);
        raschsvObyazPlatSv.setOktmo("1");
        return raschsvObyazPlatSvDao.insertObyazPlatSv(raschsvObyazPlatSv).longValue();
    }

    /**
     * Добавление записи в таблицу "Сведения по суммам (тип 1)"
     * @return
     */
    private RaschsvSvSum1Tip createRaschsvSvSum1Tip() {
        RaschsvSvSum1Tip raschsvSvSum1Tip = new RaschsvSvSum1Tip();
        raschsvSvSum1Tip.setSumVsegoPer(1.1);
        raschsvSvSum1Tip.setSumVsegoPosl3m(1.1);
        raschsvSvSum1Tip.setSum1mPosl3m(1.1);
        raschsvSvSum1Tip.setSum2mPosl3m(1.1);
        raschsvSvSum1Tip.setSum3mPosl3m(1.1);
        return raschsvSvSum1Tip;
    }

    /**
     * Добавление записи в таблицу "Сведения по количеству физических лиц"
     * @return
     */
    private RaschsvKolLicTip createRaschsvKolLicTip() {
        RaschsvKolLicTip raschsvKolLicTip = new RaschsvKolLicTip();
        raschsvKolLicTip.setKolVsegoPer(1);
        raschsvKolLicTip.setKolVsegoPosl3m(1);
        raschsvKolLicTip.setKol1mPosl3m(1);
        raschsvKolLicTip.setKol2mPosl3m(1);
        raschsvKolLicTip.setKol3mPosl3m(1);
        return raschsvKolLicTip;
    }

    /**
     * Тестирование сохранения данных в таблицу "Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     */
    @Test
    public void testInsertRaschsvUplPrevOss() {
        RaschsvUplPrevOss raschsvUplPrevOss1 = new RaschsvUplPrevOss();
        raschsvUplPrevOss1.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvUplPrevOss1.setKbk("1");
        raschsvUplPrevOss1.setPrevRashSv1m(1.1);
        raschsvUplPrevOss1.setPrevRashSv2m(1.1);
        raschsvUplPrevOss1.setPrevRashSv3m(1.1);
        raschsvUplPrevOss1.setPrevRashSvPer(1.1);
        raschsvUplPrevOss1.setSumSbUpl1m(2.1);
        raschsvUplPrevOss1.setSumSbUpl2m(2.1);
        raschsvUplPrevOss1.setSumSbUpl3m(2.1);
        raschsvUplPrevOss1.setSumSbUplPer(2.1);

        assertNotNull(raschsvUplPrevOssDao.insertUplPrevOss(raschsvUplPrevOss1));
    }

    /**
     * Тестирование сохранения данных в таблицу "Сумма страховых взносов на пенсионное, медицинское, социальное страхование"
     */
    @Test
    public void testInsertRaschsvUplPer() {
        Long raschsvObyazPlatSvId = createRaschsvObyazPlatSv();
        List<RaschsvUplPer> raschsvUplPerList = new ArrayList<RaschsvUplPer>();

        RaschsvUplPer raschsvUplPer1 = new RaschsvUplPer();
        raschsvUplPer1.setRaschsvObyazPlatSvId(raschsvObyazPlatSvId);
        raschsvUplPer1.setNodeName("УплПерОПС");
        raschsvUplPer1.setKbk("1");
        raschsvUplPer1.setSumSbUplPer(1.1);
        raschsvUplPer1.setSumSbUpl1m(1.1);
        raschsvUplPer1.setSumSbUpl2m(1.1);
        raschsvUplPer1.setSumSbUpl3m(1.1);
        raschsvUplPerList.add(raschsvUplPer1);

        RaschsvUplPer raschsvUplPer2 = new RaschsvUplPer();
        raschsvUplPer2.setRaschsvObyazPlatSvId(raschsvObyazPlatSvId);
        raschsvUplPer2.setNodeName("УплПерОМС");
        raschsvUplPer2.setKbk("2");
        raschsvUplPer2.setSumSbUplPer(2.1);
        raschsvUplPer2.setSumSbUpl1m(2.1);
        raschsvUplPer2.setSumSbUpl2m(2.1);
        raschsvUplPer2.setSumSbUpl3m(2.1);
        raschsvUplPerList.add(raschsvUplPer2);

        assertEquals(raschsvUplPerDao.insertUplPer(raschsvUplPerList).intValue(), raschsvUplPerList.size());
    }

    /**
     * Тестирование сохранения данных в таблицу "Персонифицированные сведения о застрахованных лицах"
     */
    @Test
    public void testInsertRaschsvPersSvStrahLic() {
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = new ArrayList<RaschsvPersSvStrahLic>();
        List<RaschsvSvVyplMt> raschsvSvVyplMtList = new ArrayList<RaschsvSvVyplMt>();
        List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = new ArrayList<RaschsvVyplSvDopMt>();

        // Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа
        RaschsvVyplSvDopMt raschsvVyplSvDopMt = new RaschsvVyplSvDopMt();
        raschsvVyplSvDopMt.setMesyac("01");
        raschsvVyplSvDopMt.setTarif("1");
        raschsvVyplSvDopMt.setVyplSv(1.1);
        raschsvVyplSvDopMt.setNachislSv(1.1);
        raschsvVyplSvDopMtList.add(raschsvVyplSvDopMt);

        // Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу
        RaschsvVyplSvDop raschsvVyplSvDop = new RaschsvVyplSvDop();
        raschsvVyplSvDop.setVyplSvVs3(1.1);
        raschsvVyplSvDop.setNachislSvVs3(1.1);
        raschsvVyplSvDop.setRaschsvVyplSvDopMtList(raschsvVyplSvDopMtList);

        // Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица
        RaschsvSvVyplMt raschsvSvVyplMt1 = new RaschsvSvVyplMt();
        raschsvSvVyplMt1.setMesyac("01");
        raschsvSvVyplMt1.setKodKatLic("1");
        raschsvSvVyplMt1.setSumVypl(1.1);
        raschsvSvVyplMt1.setVyplOps(1.1);
        raschsvSvVyplMt1.setVyplOpsDog(1.1);
        raschsvSvVyplMt1.setNachislSv(1.1);
        raschsvSvVyplMtList.add(raschsvSvVyplMt1);

        // Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица
        RaschsvSvVypl raschsvSvVypl = new RaschsvSvVypl();
        raschsvSvVypl.setSumVyplVs3(1.1);
        raschsvSvVypl.setVyplOpsVs3(1.1);
        raschsvSvVypl.setVyplOpsDogVs3(1.1);
        raschsvSvVypl.setNachislSvVs3(1.1);
        raschsvSvVypl.setRaschsvSvVyplMtList(raschsvSvVyplMtList);

        // Персонифицированные сведения о застрахованных лицах
        RaschsvPersSvStrahLic raschsvPersSvStrahLic1 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic1.setDeclarationDataId(DECLARATION_ID_EXIST);
        raschsvPersSvStrahLic1.setNomKorr(1);
        raschsvPersSvStrahLic1.setRaschsvSvVypl(raschsvSvVypl);
        raschsvPersSvStrahLic1.setRaschsvVyplSvDop(raschsvVyplSvDop);

        // Персонифицированные сведения о застрахованных лицах
        RaschsvPersSvStrahLic raschsvPersSvStrahLic2 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic2.setDeclarationDataId(DECLARATION_ID_EXIST);
        raschsvPersSvStrahLic2.setNomKorr(2);

        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic1);
        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic2);
        assertEquals(raschsvPersSvStrahLicDao.insertPersSvStrahLic(raschsvPersSvStrahLicList).intValue(), raschsvPersSvStrahLicList.size());
    }

    /**
     * Тестирование выборки данных из таблицы ПерсСвСтрахЛиц по ИНН ФЛ и идентификатору декларации
     */
    @Test
    public void testFindPersonsByInn() {
        assertNotNull(raschsvPersSvStrahLicDao.findPersonByInn(DECLARATION_ID_EXIST, "111111111111"));
        assertNull(raschsvPersSvStrahLicDao.findPersonByInn(DECLARATION_ID_EXIST, "222222222222"));
        assertNull(raschsvPersSvStrahLicDao.findPersonByInn(DECLARATION_ID_NOT_EXIST, "111111111111"));
    }

    /**
     * Тестирование выборки данных из таблицы ПерсСвСтрахЛиц по идентификатору декларации
     */
    @Test
    public void testFindPersons() {
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = raschsvPersSvStrahLicDao.findPersons(DECLARATION_ID_EXIST);
        assertFalse(raschsvPersSvStrahLicList.isEmpty());

        for (RaschsvPersSvStrahLic raschsvPersSvStrahLic : raschsvPersSvStrahLicList) {
            RaschsvSvVypl raschsvSvVypl = raschsvPersSvStrahLic.getRaschsvSvVypl();
            assertNotNull(raschsvSvVypl);

            List<RaschsvSvVyplMt> raschsvSvVyplMtList = raschsvSvVypl.getRaschsvSvVyplMtList();
            assertFalse(raschsvSvVyplMtList.isEmpty());
            for (RaschsvSvVyplMt raschsvSvVyplMt : raschsvSvVyplMtList) {
                assertNotNull(raschsvSvVyplMt);
            }

            RaschsvVyplSvDop raschsvVyplSvDop = raschsvPersSvStrahLic.getRaschsvVyplSvDop();
            assertNotNull(raschsvVyplSvDop);

            List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = raschsvVyplSvDop.getRaschsvVyplSvDopMtList();
            assertFalse(raschsvVyplSvDopMtList.isEmpty());
            for (RaschsvVyplSvDopMt raschsvVyplSvDopMt : raschsvVyplSvDopMtList) {
                assertNotNull(raschsvVyplSvDopMt);
            }
        }

        assertTrue(raschsvPersSvStrahLicDao.findPersons(DECLARATION_ID_NOT_EXIST).isEmpty());
    }

    /**
     * Тестирование выборки данных из таблицы ОбязПлатСВ
     */
    @Test
    public void testFindObyazPlatSv() {

        assertNull(raschsvObyazPlatSvDao.findObyazPlatSv(DECLARATION_ID_NOT_EXIST));

        RaschsvObyazPlatSv raschsvObyazPlatSv = raschsvObyazPlatSvDao.findObyazPlatSv(DECLARATION_ID_EXIST);
        assertNotNull(raschsvObyazPlatSv);

        // УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО
        assertFalse(raschsvObyazPlatSv.getRaschsvUplPerList().isEmpty());

        // УплПревОСС
        assertNotNull(raschsvObyazPlatSv.getRaschsvUplPrevOss());

        // РасчСВ_ОПС_ОМС
        List<RaschsvSvOpsOms> raschsvSvOpsOmsList = raschsvObyazPlatSv.getRaschsvSvOpsOmsList();
        assertFalse(raschsvSvOpsOmsList.isEmpty());
        for (RaschsvSvOpsOms raschsvSvOpsOms : raschsvSvOpsOmsList) {
            List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList = raschsvSvOpsOms.getRaschsvSvOpsOmsRaschList();
            assertFalse(raschsvSvOpsOmsRaschList.isEmpty());

            for (RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch : raschsvSvOpsOmsRaschList) {
                List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsRaschSumList();
                assertFalse(raschsvSvOpsOmsRaschSumList.isEmpty());

                for (RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum : raschsvSvOpsOmsRaschSumList) {
                    assertNotNull(raschsvSvOpsOmsRaschSum.getRaschsvSvSum1Tip());
                }

                List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList = raschsvSvOpsOmsRasch.getRaschsvSvOpsOmsRaschKolList();
                assertFalse(raschsvSvOpsOmsRaschKolList.isEmpty());

                for (RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol : raschsvSvOpsOmsRaschKolList) {
                    assertNotNull(raschsvSvOpsOmsRaschKol.getRaschsvKolLicTip());
                }
            }
        }

        // РасчСВ_ОСС.ВНМ
        RaschsvOssVnm raschsvOssVnm = raschsvObyazPlatSv.getRaschsvOssVnm();
        assertNotNull(raschsvOssVnm);
        assertFalse(raschsvOssVnm.getRaschsvUplSvPrevList().isEmpty());
        assertFalse(raschsvOssVnm.getRaschsvOssVnmSumList().isEmpty());
        for (RaschsvOssVnmSum raschsvOssVnmSum : raschsvOssVnm.getRaschsvOssVnmSumList()) {
            assertNotNull(raschsvOssVnmSum.getRaschsvSvSum1Tip());
        }
        assertFalse(raschsvOssVnm.getRaschsvOssVnmKolList().isEmpty());
        for (RaschsvOssVnmKol raschsvOssVnmKol : raschsvOssVnm.getRaschsvOssVnmKolList()) {
            assertNotNull(raschsvOssVnmKol.getRaschsvKolLicTip());
        }

        // РасхОССЗак
        RaschsvRashOssZak raschsvRashOssZak = raschsvObyazPlatSv.getRaschsvRashOssZak();
        assertNotNull(raschsvRashOssZak);
        assertFalse(raschsvRashOssZak.getRaschsvRashOssZakRashList().isEmpty());

        // ВыплФинФБ
        RaschsvVyplFinFb raschsvVyplFinFb = raschsvObyazPlatSv.getRaschsvVyplFinFb();
        assertNotNull(raschsvVyplFinFb);
        assertFalse(raschsvVyplFinFb.getRaschsvVyplPrichinaList().isEmpty());
        for (RaschsvVyplPrichina raschsvVyplPrichina : raschsvVyplFinFb.getRaschsvVyplPrichinaList()) {
            assertFalse(raschsvVyplPrichina.getRaschsvRashVyplList().isEmpty());
        }

        // ПравТариф3.1.427
        assertNotNull(raschsvObyazPlatSv.getRaschsvPravTarif31427());

        // ПравТариф5.1.427
        assertNotNull(raschsvObyazPlatSv.getRaschsvPravTarif51427());

        // ПравТариф7.1.427
        assertNotNull(raschsvObyazPlatSv.getRaschsvPravTarif71427());
    }

    /**
     * Тестирование сохранения данных в таблицу "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     */
    @Test
    public void testInsertRaschsvSvOpsOms() {
        Long raschsvObyazPlatSvId = createRaschsvObyazPlatSv();
        List<RaschsvSvOpsOms> raschsvSvOpsOmsList = new ArrayList<RaschsvSvOpsOms>();
        List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList = new ArrayList<RaschsvSvOpsOmsRaschSum>();
        List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList = new ArrayList<RaschsvSvOpsOmsRaschKol>();
        List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList = new ArrayList<RaschsvSvOpsOmsRasch>();

        // Сведения по суммам (тип 1)
        RaschsvSvSum1Tip raschsvSvSum1Tip = createRaschsvSvSum1Tip();

        // Сведения по количеству физических лиц
        RaschsvKolLicTip raschsvKolLicTip = createRaschsvKolLicTip();

        // Сумма для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование
        RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum1 = new RaschsvSvOpsOmsRaschSum();
        raschsvSvOpsOmsRaschSum1.setNodeName("NodeName");
        raschsvSvOpsOmsRaschSum1.setRaschsvSvSum1Tip(raschsvSvSum1Tip);
        raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum1);

        // Количество для расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование
        RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol1 = new RaschsvSvOpsOmsRaschKol();
        raschsvSvOpsOmsRaschKol1.setNodeName("NodeName");
        raschsvSvOpsOmsRaschKol1.setRaschsvKolLicTip(raschsvKolLicTip);
        raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol1);

        // Вид расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование
        RaschsvSvOpsOmsRasch raschsvSvOpsOmsRasch1 = new RaschsvSvOpsOmsRasch();
        raschsvSvOpsOmsRasch1.setNodeName("NodeName");
        raschsvSvOpsOmsRasch1.setRaschsvSvOpsOmsRaschSumList(raschsvSvOpsOmsRaschSumList);
        raschsvSvOpsOmsRasch1.setRaschsvSvOpsOmsRaschKolList(raschsvSvOpsOmsRaschKolList);
        raschsvSvOpsOmsRaschList.add(raschsvSvOpsOmsRasch1);

        // Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование
        RaschsvSvOpsOms raschsvSvOpsOms1 = new RaschsvSvOpsOms();
        raschsvSvOpsOms1.setRaschsvObyazPlatSvId(raschsvObyazPlatSvId);
        raschsvSvOpsOms1.setTarifPlat("1");
        raschsvSvOpsOms1.setRaschsvSvOpsOmsRaschList(raschsvSvOpsOmsRaschList);
        raschsvSvOpsOmsList.add(raschsvSvOpsOms1);

        RaschsvSvOpsOms raschsvSvOpsOms2 = new RaschsvSvOpsOms();
        raschsvSvOpsOms2.setRaschsvObyazPlatSvId(raschsvObyazPlatSvId);
        raschsvSvOpsOms2.setTarifPlat("2");
        raschsvSvOpsOmsList.add(raschsvSvOpsOms2);

        assertEquals(raschsvSvOpsOmsDao.insertRaschsvSvOpsOms(raschsvSvOpsOmsList).intValue(), raschsvSvOpsOmsList.size());
    }

    /**
     * Тестирование сохранения данных в таблицу "Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством"
     */
    @Test
    public void testInsertRaschsvOssVnm() {
        List<RaschsvUplSvPrev> raschsvUplSvPrevList = new ArrayList<RaschsvUplSvPrev>();
        List<RaschsvOssVnmKol> raschsvOssVnmKolList = new ArrayList<RaschsvOssVnmKol>();
        List<RaschsvOssVnmSum> raschsvOssVnmSumList = new ArrayList<RaschsvOssVnmSum>();

        // Сумма страховых взносов, подлежащая к уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)
        RaschsvUplSvPrev raschsvUplSvPrev1 = new RaschsvUplSvPrev();
        raschsvUplSvPrev1.setNodeName("NodeName");
        raschsvUplSvPrev1.setPriznak("1");
        raschsvUplSvPrev1.setSvSum(1.1);
        raschsvUplSvPrevList.add(raschsvUplSvPrev1);

        // Сведения по количеству физических лиц
        RaschsvKolLicTip raschsvKolLicTip = createRaschsvKolLicTip();
        RaschsvOssVnmKol raschsvOssVnmKol1 = new RaschsvOssVnmKol();
        raschsvOssVnmKol1.setRaschsvKolLicTip(raschsvKolLicTip);
        raschsvOssVnmKol1.setNodeName("NodeName");
        raschsvOssVnmKolList.add(raschsvOssVnmKol1);

        // Сведения по суммам (тип 1)
        RaschsvSvSum1Tip raschsvSvSum1Tip = createRaschsvSvSum1Tip();
        RaschsvOssVnmSum raschsvOssVnmSum1 = new RaschsvOssVnmSum();
        raschsvOssVnmSum1.setRaschsvSvSum1Tip(raschsvSvSum1Tip);
        raschsvOssVnmSum1.setNodeName("NodeName");
        raschsvOssVnmSumList.add(raschsvOssVnmSum1);

        RaschsvOssVnm raschsvOssVnm = new RaschsvOssVnm();
        raschsvOssVnm.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvOssVnm.setRaschsvUplSvPrevList(raschsvUplSvPrevList);
        raschsvOssVnm.setRaschsvOssVnmKolList(raschsvOssVnmKolList);
        raschsvOssVnm.setRaschsvOssVnmSumList(raschsvOssVnmSumList);
        raschsvOssVnm.setPrizVypl("1");

        assertNotNull(raschsvOssVnmDao.insertRaschsvOssVnm(raschsvOssVnm));
    }

    /**
     * Тестирование сохранения данных в таблицу "Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации"
     */
    @Test
    public void testInsertRaschsvRashOssZak() {
        List<RaschsvRashOssZakRash> raschsvRashOssZakRashList = new ArrayList<RaschsvRashOssZakRash>();

        RaschsvRashOssZakRash raschsvRashOssZakRash1 = new RaschsvRashOssZakRash();
        raschsvRashOssZakRash1.setNodeName("NodeName");
        raschsvRashOssZakRash1.setChislSluch(1);
        raschsvRashOssZakRash1.setKolVypl(1);
        raschsvRashOssZakRash1.setPashVsego(1.1);
        raschsvRashOssZakRash1.setRashFinFb(1.1);
        raschsvRashOssZakRashList.add(raschsvRashOssZakRash1);

        RaschsvRashOssZak raschsvRashOssZak = new RaschsvRashOssZak();
        raschsvRashOssZak.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvRashOssZak.setRaschsvRashOssZakRashList(raschsvRashOssZakRashList);

        assertNotNull(raschsvRashOssZakDao.insertRaschsvRashOssZak(raschsvRashOssZak));
    }

    /**
     * Тестирование сохранения данных в таблицу "Выплаты, произведенные за счет средств, финансируемых из федерального бюджета"
     */
    @Test
    public void testInsertRaschsvVyplFinFb() {
        List<RaschsvRashVypl> raschsvRashVyplList = new ArrayList<RaschsvRashVypl>();
        List<RaschsvVyplPrichina> raschsvVyplPrichinaList = new ArrayList<RaschsvVyplPrichina>();

        RaschsvRashVypl raschsvRashVypl1 = new RaschsvRashVypl();
        raschsvRashVypl1.setNodeName("NodeName");
        raschsvRashVypl1.setKolVypl(1);
        raschsvRashVypl1.setChislPoluch(1);
        raschsvRashVypl1.setRashod(1.1);
        raschsvRashVyplList.add(raschsvRashVypl1);

        RaschsvVyplPrichina raschsvVyplPrichina1 = new RaschsvVyplPrichina();
        raschsvVyplPrichina1.setNodeName("NodeName");
        raschsvVyplPrichina1.setSvVnfUhodInv(1.1);
        raschsvVyplPrichina1.setRaschsvRashVyplList(raschsvRashVyplList);
        raschsvVyplPrichinaList.add(raschsvVyplPrichina1);

        RaschsvVyplFinFb raschsvVyplFinFb = new RaschsvVyplFinFb();
        raschsvVyplFinFb.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvVyplFinFb.setRaschsvVyplPrichinaList(raschsvVyplPrichinaList);

        assertNotNull(raschsvVyplFinFbDao.insertRaschsvVyplFinFb(raschsvVyplFinFb));
    }

    /**
     * Тестирование сохранения данных в таблицу "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427"
     */
    @Test
    public void testInsertRaschsvPravTarif31427() {
        RaschsvPravTarif31427 raschsvPravTarif31427 = new RaschsvPravTarif31427();
        raschsvPravTarif31427.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvPravTarif31427.setSrChisl9mpr(1);
        raschsvPravTarif31427.setSrChislPer(1);
        raschsvPravTarif31427.setDoh2489mpr(1L);
        raschsvPravTarif31427.setDoh248Per(1L);
        raschsvPravTarif31427.setDohKr54279mpr(1L);
        raschsvPravTarif31427.setDohKr5427Per(1L);
        raschsvPravTarif31427.setDohDoh54279mpr(1.1);
        raschsvPravTarif31427.setDohDoh5427per(1.1);
        raschsvPravTarif31427.setDataZapAkOrg(new Date());
        raschsvPravTarif31427.setNomZapAkOrg("nom");

        assertNotNull(raschsvPravTarif31427Dao.insertRaschsvPravTarif31427(raschsvPravTarif31427));
    }

    /**
     * Тестирование сохранения данных в таблицу "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427"
     */
    @Test
    public void testInsertRaschsvPravTarif51427() {
        RaschsvPravTarif51427 raschsvPravTarif51427 = new RaschsvPravTarif51427();
        raschsvPravTarif51427.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvPravTarif51427.setDoh346_15vs(1L);
        raschsvPravTarif51427.setDoh6_427(1L);
        raschsvPravTarif51427.setDolDoh6_427(1.1);

        assertNotNull(raschsvPravTarif51427Dao.insertRaschsvPravTarif51427(raschsvPravTarif51427));
    }

    /**
     * Тестирование сохранения данных в таблицу "Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427"
     */
    @Test
    public void testInsertRaschsvPravTarif71427() {
        RaschsvPravTarif71427 raschsvPravTarif71427 = new RaschsvPravTarif71427();
        raschsvPravTarif71427.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvPravTarif71427.setDohVsPred(1L);
        raschsvPravTarif71427.setDohVsPer(1L);
        raschsvPravTarif71427.setDohCelPostPred(1L);
        raschsvPravTarif71427.setDohCelPostPer(1L);
        raschsvPravTarif71427.setDohGrantPred(1L);
        raschsvPravTarif71427.setDohGrantPer(1L);
        raschsvPravTarif71427.setDohEkDeyatPred(1L);
        raschsvPravTarif71427.setDohEkDeyatPer(1L);
        raschsvPravTarif71427.setDolDohPred(1.1);
        raschsvPravTarif71427.setDolDohPer(1.1);

        assertNotNull(raschsvPravTarif71427Dao.insertRaschsvPravTarif71427(raschsvPravTarif71427));
    }

    /**
     * Тестирование сохранения данных в таблицу "Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427"
     */
    @Test
    public void testInsertRaschsvSvPrimTarif91427() {
        // Сведения по суммам (тип 1)
        RaschsvSvSum1Tip raschsvSvSum1Tip = createRaschsvSvSum1Tip();
        RaschsvSvSum1Tip raschsvSvSum2Tip = createRaschsvSvSum1Tip();

        // Итого выплат
        RaschsvVyplatIt427 raschsvVyplatIt427 = new RaschsvVyplatIt427();
        raschsvVyplatIt427.setRaschsvSvSum1Tip(raschsvSvSum1Tip);

        // Сведения о патенте
        List<RaschsvSvedPatent> raschsvSvedPatentList = new ArrayList<RaschsvSvedPatent>();
        RaschsvSvedPatent raschsvSvedPatent1 = new RaschsvSvedPatent();
        raschsvSvedPatent1.setRaschsvSvSum1Tip(raschsvSvSum2Tip);
        raschsvSvedPatent1.setNomPatent("1");
        raschsvSvedPatent1.setVydDeyatPatent("1");
        raschsvSvedPatent1.setDataNachDeyst(new Date());
        raschsvSvedPatent1.setDataKonDeyst(new Date());
        raschsvSvedPatentList.add(raschsvSvedPatent1);

        RaschsvSvPrimTarif91427 raschsvSvPrimTarif91427 = new RaschsvSvPrimTarif91427();
        raschsvSvPrimTarif91427.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvSvPrimTarif91427.setRaschsvVyplatIt427(raschsvVyplatIt427);
        raschsvSvPrimTarif91427.setRaschsvSvedPatentList(raschsvSvedPatentList);

        assertNotNull(raschsvSvPrimTarif91427Dao.insertRaschsvSvPrimTarif91427(raschsvSvPrimTarif91427));
    }

    /**
     * Тестирование сохранения данных в таблицу "Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426) Налогового кодекса Российской Федерации"
     */
    @Test
    public void testInsertRaschsvSvPrimTarif22425() {
        // Сведения по суммам (тип 1)
        RaschsvSvSum1Tip raschsvSvSum1Tip = createRaschsvSvSum1Tip();
        RaschsvSvSum1Tip raschsvSvSum2Tip = createRaschsvSvSum1Tip();

        // Итого выплат
        RaschsvVyplatIt425 raschsvVyplatIt425 = new RaschsvVyplatIt425();
        raschsvVyplatIt425.setRaschsvSvSum1Tip(raschsvSvSum1Tip);

        // Сведения об иностранных гражданах, лицах без гражданства
        List<RaschsvSvInoGrazd> raschsvSvInoGrazdList = new ArrayList<RaschsvSvInoGrazd>();
        RaschsvSvInoGrazd raschsvSvInoGrazd = new RaschsvSvInoGrazd();
        raschsvSvInoGrazd.setRaschsvSvSum1Tip(raschsvSvSum2Tip);
        raschsvSvInoGrazd.setInnfl("Innfl");
        raschsvSvInoGrazd.setSnils("Snils");
        raschsvSvInoGrazd.setGrazd("1");
        raschsvSvInoGrazd.setFamilia("Familia");
        raschsvSvInoGrazd.setImya("Imya");
        raschsvSvInoGrazd.setMiddleName("MiddleName");
        raschsvSvInoGrazdList.add(raschsvSvInoGrazd);

        RaschsvSvPrimTarif22425 raschsvSvPrimTarif22425 = new RaschsvSvPrimTarif22425();
        raschsvSvPrimTarif22425.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvSvPrimTarif22425.setRaschsvVyplatIt425(raschsvVyplatIt425);
        raschsvSvPrimTarif22425.setRaschsvSvInoGrazdList(raschsvSvInoGrazdList);

        assertNotNull(raschsvSvPrimTarif22425Dao.insertRaschsvSvPrimTarif22425(raschsvSvPrimTarif22425));
    }

    /**
     * Тестирование сохранения данных в таблицу "Сведения об обучающихся, необходимые для применения положений подпункта 1 пункта 3 статьи 422"
     */
    @Test
    public void testInsertRaschsvSvPrimTarif13422() {
        // Сведения по суммам (тип 1)
        RaschsvSvSum1Tip raschsvSvSum1Tip = createRaschsvSvSum1Tip();
        RaschsvSvSum1Tip raschsvSvSum2Tip = createRaschsvSvSum1Tip();

        // Итого выплат
        RaschsvVyplatIt422 raschsvVyplatIt422 = new RaschsvVyplatIt422();
        raschsvVyplatIt422.setRaschsvSvSum1Tip(raschsvSvSum1Tip);

        // Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой
        List<RaschsvSvReestrMdo> raschsvSvReestrMdoList = new ArrayList<RaschsvSvReestrMdo>();
        RaschsvSvReestrMdo raschsvSvReestrMdo1 = new RaschsvSvReestrMdo();
        raschsvSvReestrMdo1.setNaimMdo("NaimMdo");
        raschsvSvReestrMdo1.setDataZapis(new Date());
        raschsvSvReestrMdo1.setNomerZapis("NomerZapis");
        raschsvSvReestrMdoList.add(raschsvSvReestrMdo1);

        // Сведения об обучающихся
        List<RaschsvSvedObuch> raschsvSvedObuchList = new ArrayList<RaschsvSvedObuch>();
        RaschsvSvedObuch raschsvSvedObuch1 = new RaschsvSvedObuch();
        raschsvSvedObuch1.setUnikNomer("Uni");
        raschsvSvedObuch1.setFamilia("Familia");
        raschsvSvedObuch1.setImya("Imya");
        raschsvSvedObuch1.setMiddleName("MiddleName");
        raschsvSvedObuch1.setSpravNomer("SpravNomer");
        raschsvSvedObuch1.setSpravData(new Date());
        raschsvSvedObuch1.setSpravNodeName("SpravNodeName");
        raschsvSvedObuch1.setRaschsvSvReestrMdoList(raschsvSvReestrMdoList);
        raschsvSvedObuch1.setRaschsvSvSum1Tip(raschsvSvSum2Tip);
        raschsvSvedObuchList.add(raschsvSvedObuch1);

        RaschsvSvPrimTarif13422 raschsvSvPrimTarif13422 = new RaschsvSvPrimTarif13422();
        raschsvSvPrimTarif13422.setRaschsvObyazPlatSvId(createRaschsvObyazPlatSv());
        raschsvSvPrimTarif13422.setRaschsvVyplatIt422(raschsvVyplatIt422);
        raschsvSvPrimTarif13422.setRaschsvSvedObuchList(raschsvSvedObuchList);

        assertNotNull(raschsvSvPrimTarif13422Dao.insertRaschsvSvPrimTarif13422(raschsvSvPrimTarif13422));
    }

    /**
     * Тестирование сохранения данных в таблицу "Сведения о плательщике страховых взносов и Сведения о лице, подписавшем документ"
     */
    @Test
    public void testInsertRaschsvSvnpPodpisant() {
        RaschsvSvnpPodpisant raschsvSvnpPodpisant = new RaschsvSvnpPodpisant();
        raschsvSvnpPodpisant.setDeclarationDataId(DECLARATION_ID_EXIST);
        raschsvSvnpPodpisant.setSvnpOkved("1");
        raschsvSvnpPodpisant.setSvnpTlph("1");
        raschsvSvnpPodpisant.setSvnpNaimOrg("1");
        raschsvSvnpPodpisant.setSvnpKpp("1");
        raschsvSvnpPodpisant.setSvnpSvReorgForm("1");
        raschsvSvnpPodpisant.setSvnpSvReorgInnyl("1");
        raschsvSvnpPodpisant.setFamilia("1");
        raschsvSvnpPodpisant.setImya("1");
        raschsvSvnpPodpisant.setMiddleName("1");
        raschsvSvnpPodpisant.setPodpisantPrPodp("1");
        raschsvSvnpPodpisant.setPodpisantNaimDoc("1");
        raschsvSvnpPodpisant.setPodpisantNaimOrg("1");

        assertNotNull(raschsvSvnpPodpisantDao.insertRaschsvSvnpPodpisant(raschsvSvnpPodpisant));
    }
}
