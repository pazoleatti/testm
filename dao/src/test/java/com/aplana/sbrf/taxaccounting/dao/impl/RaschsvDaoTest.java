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
     * Тестирование сохранения данных в таблицу "Сведения по суммам (тип 1)"
     */
    @Test
    public void testInsertRaschsvSvSum1Tip() {
        RaschsvSvSum1Tip raschsvSvSum1Tip = new RaschsvSvSum1Tip();
        raschsvSvSum1Tip.setSumVsegoPer(1.1);
        raschsvSvSum1Tip.setSumVsegoPosl3m(1.1);
        raschsvSvSum1Tip.setSum1mPosl3m(1.1);
        raschsvSvSum1Tip.setSum2mPosl3m(1.1);
        raschsvSvSum1Tip.setSum3mPosl3m(1.1);

        assertNotNull(raschsvSvSum1TipDao.insertRaschsvSvSum1Tip(raschsvSvSum1Tip));
    }

    /**
     * Тестирование сохранения данных в таблицу "Сведения по количеству физических лиц"
     */
    @Test
    public void testInsertRaschsvKolLicTip() {
        RaschsvKolLicTip raschsvKolLicTip = new RaschsvKolLicTip();
        raschsvKolLicTip.setKolVsegoPer(1);
        raschsvKolLicTip.setKolVsegoPosl3m(1);
        raschsvKolLicTip.setKol1mPosl3m(1);
        raschsvKolLicTip.setKol2mPosl3m(1);
        raschsvKolLicTip.setKol3mPosl3m(1);

        assertNotNull(raschsvKolLicTipDao.insertRaschsvKolLicTip(raschsvKolLicTip));
    }

    /**
     * Тестирование сохранения данных в таблицу "Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование"
     */
    @Test
    public void testInsertRaschsvSvOpsOms() {
        Long raschsvObyazPlatSvId = createRaschsvObyazPlatSv();
        List<RaschsvSvOpsOms> raschsvSvOpsOmsList = new ArrayList<RaschsvSvOpsOms>();

        RaschsvSvOpsOms raschsvSvOpsOms1 = new RaschsvSvOpsOms();
        raschsvSvOpsOms1.setRaschsvObyazPlatSvId(raschsvObyazPlatSvId);
        raschsvSvOpsOms1.setTarifPlat("1");
        raschsvSvOpsOmsList.add(raschsvSvOpsOms1);

        RaschsvSvOpsOms raschsvSvOpsOms2 = new RaschsvSvOpsOms();
        raschsvSvOpsOms2.setRaschsvObyazPlatSvId(raschsvObyazPlatSvId);
        raschsvSvOpsOms2.setTarifPlat("2");
        raschsvSvOpsOmsList.add(raschsvSvOpsOms2);

        assertEquals(raschsvSvOpsOmsDao.insertRaschsvSvOpsOms(raschsvSvOpsOmsList).intValue(), raschsvSvOpsOmsList.size());
    }
}
