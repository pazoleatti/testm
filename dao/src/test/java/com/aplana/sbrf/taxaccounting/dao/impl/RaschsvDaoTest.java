package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvObyazPlatSvDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RaschsvDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RaschsvDaoTest {

    @Autowired
    private RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    @Autowired
    private RaschsvObyazPlatSvDao raschsvObyazPlatSvDao;

    /**
     * Тестирование выборки данных из таблицы "Персонифицированные сведения о застрахованных лицах"
     */
    @Test
    public void testGetRaschsvPersSvStrahLic() {
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = raschsvPersSvStrahLicDao.findAll();
        assertNotNull(raschsvPersSvStrahLicList);
    }

    /**
     * Тестирование сохранения данных в таблицу "Сводные данные об обязательствах плательщика страховых взносов"
     */
    @Test
    public void testInsertRaschsvObyazPlatSv() {
        RaschsvObyazPlatSv raschsvObyazPlatSv = new RaschsvObyazPlatSv();
        raschsvObyazPlatSv.setDeclarationDataId(1L);
        raschsvObyazPlatSv.setOktmo("1");

        raschsvObyazPlatSvDao.insertObyazPlatSv(raschsvObyazPlatSv);
        assertFalse(raschsvObyazPlatSvDao.findAll().isEmpty());
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
        raschsvPersSvStrahLicDao.insertPersSvStrahLic(raschsvPersSvStrahLicList);
        assertFalse(raschsvPersSvStrahLicDao.findAll().isEmpty());
    }
}
