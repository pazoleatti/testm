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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    /**
     * Добавление записи в таблицу ОбязПлатСВ
     * @return
     */
    private Long createRaschsvObyazPlatSv() {
        RaschsvObyazPlatSv raschsvObyazPlatSv = new RaschsvObyazPlatSv();
        raschsvObyazPlatSv.setDeclarationDataId(1L);
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
        List<RaschsvSvVypl> raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
        List<RaschsvSvVyplMt> raschsvSvVyplMtList = new ArrayList<RaschsvSvVyplMt>();
        List<RaschsvVyplSvDopMt> raschsvVyplSvDopMtList = new ArrayList<RaschsvVyplSvDopMt>();
        List<RaschsvVyplSvDop> raschsvVyplSvDopList = new ArrayList<RaschsvVyplSvDop>();

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
        raschsvVyplSvDopList.add(raschsvVyplSvDop);

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
        RaschsvSvVypl raschsvSvVypl1 = new RaschsvSvVypl();
        raschsvSvVypl1.setSumVyplVs3(1.1);
        raschsvSvVypl1.setVyplOpsVs3(1.1);
        raschsvSvVypl1.setVyplOpsDogVs3(1.1);
        raschsvSvVypl1.setNachislSvVs3(1.1);
        raschsvSvVypl1.setRaschsvSvVyplMtList(raschsvSvVyplMtList);
        raschsvSvVyplList.add(raschsvSvVypl1);

        // Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица
        RaschsvSvVypl raschsvSvVypl2 = new RaschsvSvVypl();
        raschsvSvVypl2.setSumVyplVs3(2.1);
        raschsvSvVypl2.setVyplOpsVs3(2.1);
        raschsvSvVypl2.setVyplOpsDogVs3(2.1);
        raschsvSvVypl2.setNachislSvVs3(2.1);
        raschsvSvVyplList.add(raschsvSvVypl2);

        // Персонифицированные сведения о застрахованных лицах
        RaschsvPersSvStrahLic raschsvPersSvStrahLic1 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic1.setDeclarationDataId(1L);
        raschsvPersSvStrahLic1.setNomKorr(1);
        raschsvPersSvStrahLic1.setRaschsvSvVyplList(raschsvSvVyplList);
        raschsvPersSvStrahLic1.setRaschsvVyplSvDopList(raschsvVyplSvDopList);

        // Персонифицированные сведения о застрахованных лицах
        RaschsvPersSvStrahLic raschsvPersSvStrahLic2 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic2.setDeclarationDataId(1L);
        raschsvPersSvStrahLic2.setNomKorr(2);

        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic1);
        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic2);
        assertEquals(raschsvPersSvStrahLicDao.insertPersSvStrahLic(raschsvPersSvStrahLicList).intValue(), raschsvPersSvStrahLicList.size());
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
}
