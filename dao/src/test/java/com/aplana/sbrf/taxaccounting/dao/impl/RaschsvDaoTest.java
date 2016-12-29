package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvPersSvStrahLicDao;
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
import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RaschsvDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RaschsvDaoTest {

    @Autowired
    private RaschsvPersSvStrahLicDao raschsvPersSvStrahLicDao;

    /**
     * Тестирование сохранения данных в таблицу "Персонифицированные сведения о застрахованных лицах"
     */
    @Test
    public void testGetRaschsvPersSvStrahLic() {
        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = raschsvPersSvStrahLicDao.findAll();
        assertNotNull(raschsvPersSvStrahLicList);
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
        raschsvPersSvStrahLic1.setRaschsvSvVyplList(raschsvSvVyplList);

        RaschsvPersSvStrahLic raschsvPersSvStrahLic2 = new RaschsvPersSvStrahLic();
        raschsvPersSvStrahLic2.setNomKorr(2);
        raschsvPersSvStrahLic2.setNomer(2);

        List<RaschsvPersSvStrahLic> raschsvPersSvStrahLicList = new ArrayList<RaschsvPersSvStrahLic>();
        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic1);
        raschsvPersSvStrahLicList.add(raschsvPersSvStrahLic2);
        raschsvPersSvStrahLicDao.insert(raschsvPersSvStrahLicList);
        assertFalse(raschsvPersSvStrahLicDao.findAll().isEmpty());
    }
}
