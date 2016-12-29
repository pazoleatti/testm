package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvFileDao;
import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvFile;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvSvVypl;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RaschsvDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RaschsvDaoTest {

    @Autowired
    private RaschsvFileDao raschsvFileDao;

    @Autowired
    private RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    // Идентификатор записи в таблице "Файл обмена"
    private static final Long RASCHSV_FILE_ID = 111L;

    /**
     * Тестирование выборки данных из таблицы "Файл обмена"
     */
    @Test
    public void testGetRaschsvFile() {
        RaschsvFile raschsvFile = raschsvFileDao.get(RASCHSV_FILE_ID);
        assertNotNull(raschsvFile);
    }

    /**
     * Тестирование сохранения данных в таблицу "Файл обмена"
     */
    @Test
    public void testSaveRaschsvFile() {
        RaschsvFile raschsvFile = new RaschsvFile();
        raschsvFile.setIdFile("1");
        assertNotNull(raschsvFileDao.insert(raschsvFile));
    }

    /**
     * Тестирование сохранения данных в таблицу "Персонифицированные сведения о застрахованных лицах"
     */
    @Test
    public void testGetRaschsvPersSvStrahLic() {
        RaschsvPersSvStrahLic raschsvPersSvStrahLic = raschsvPersSvStrahLicDao.get(RASCHSV_FILE_ID);
        assertNotNull(raschsvPersSvStrahLic);
    }

    /**
     * Тестирование сохранения данных в таблицу "Персонифицированные сведения о застрахованных лицах"
     */
    @Test
    public void testInsertRaschsvPersSvStrahLic() {
        List<RaschsvSvVypl> raschsvSvVyplList = new ArrayList<RaschsvSvVypl>();
        RaschsvSvVypl raschsvSvVypl1 = new RaschsvSvVypl();
        raschsvSvVypl1.setSumVyplVs3(1.1);
        raschsvSvVypl1.setVyplOpsVs3(1.1);
        raschsvSvVypl1.setVyplOpsDogVs3(1.1);
        raschsvSvVypl1.setNachislSvVs3(1.1);
        raschsvSvVyplList.add(raschsvSvVypl1);

        RaschsvSvVypl raschsvSvVypl2 = new RaschsvSvVypl();
        raschsvSvVypl2.setSumVyplVs3(2.1);
        raschsvSvVypl2.setVyplOpsVs3(2.1);
        raschsvSvVypl2.setVyplOpsDogVs3(2.1);
        raschsvSvVypl2.setNachislSvVs3(2.1);
        raschsvSvVyplList.add(raschsvSvVypl2);

        RaschsvPersSvStrahLic raschsvPersSvStrahLic1 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic1.setNomKorr(1);
        raschsvPersSvStrahLic1.setNomer(1);
        raschsvPersSvStrahLic1.setRaschsvFileId(RASCHSV_FILE_ID);
        raschsvPersSvStrahLic1.setRaschsvSvVyplList(raschsvSvVyplList);

        RaschsvPersSvStrahLic raschsvPersSvStrahLic2 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic2.setNomKorr(2);
        raschsvPersSvStrahLic2.setNomer(2);
        raschsvPersSvStrahLic2.setRaschsvFileId(RASCHSV_FILE_ID);

        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = new ArrayList<RaschsvPersSvStrahLic>();
        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic1);
        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic2);
        assertNotNull(raschsvPersSvStrahLicDao.insert(raschsvPersSvStrahLicList));
    }
}
