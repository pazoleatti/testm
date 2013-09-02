package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.MigrationDao;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu25Row;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu64Row;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.service.RnuGenerationService;
import com.aplana.sbrf.taxaccounting.service.XmlGenerationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MigrationServiceTest {

    private MigrationService migrationService;

    private MigrationDao migrationDao;
    private Exemplar exemplar25;
    private Exemplar exemplar64;


    @Before
    public void init() {
        migrationDao = mock(MigrationDao.class);

        migrationService = new MigrationServiceImpl();

        RnuGenerationService rnuGenerationService = new RnuGenerationServiceImpl();
        XmlGenerationService xmlGenerationService = new XmlGenerationServiceImpl();

        ReflectionTestUtils.setField(migrationService, "migrationDao", migrationDao);
        ReflectionTestUtils.setField(migrationService, "rnuService", rnuGenerationService);
        ReflectionTestUtils.setField(migrationService, "xmlService", xmlGenerationService);

        ReflectionTestUtils.setField(rnuGenerationService, "migrationService", migrationService);
        ReflectionTestUtils.setField(xmlGenerationService, "migrationService", migrationService);

        exemplar25 = new Exemplar();
        exemplar25.setSubSystemId("00");
        exemplar25.setExemplarId(666L);
        exemplar25.setDepCode("0013");
        exemplar25.setBeginDate(new Date());
        exemplar25.setEndDate(new Date());
        exemplar25.setPeriodityId(4);
        exemplar25.setRnuTypeId(25);
        exemplar25.setSystemId(36);
        exemplar25.setTerCode("099");

        exemplar64 = new Exemplar();
        exemplar64.setSubSystemId("01");
        exemplar64.setExemplarId(999L);
        exemplar64.setDepCode("999300020");
        exemplar64.setBeginDate(new Date());
        exemplar64.setEndDate(new Date());
        exemplar64.setPeriodityId(4);
        exemplar64.setRnuTypeId(64);
        exemplar64.setSystemId(43);
        exemplar64.setTerCode("099");
    }


    @Test
    public void getActualExemplarByRnuTypeTest() {
        List<Exemplar> exemplarList = new ArrayList<Exemplar>();
        exemplarList.add(exemplar25);

        when(migrationDao.getActualExemplarByRnuType(25)).thenReturn(exemplarList);
        Assert.assertEquals(1, migrationService.getActualExemplarByRnuType(25).size());
    }

    @Test
    public void getActualExemplarByRnuTypeArrayTest() {
        long[] rnus = {25L, 64L};
        List<Exemplar> exemplarList25 = Arrays.asList(exemplar25);
        List<Exemplar> exemplarList64 = Arrays.asList(exemplar64);

        when(migrationDao.getActualExemplarByRnuType(25)).thenReturn(exemplarList25);
        when(migrationDao.getActualExemplarByRnuType(64)).thenReturn(exemplarList64);
        Assert.assertEquals(2, migrationService.getActualExemplarByRnuType(rnus).size());
    }

    @Test
    public void getRnuListTest() {
        when(migrationDao.getRnu25RowList(exemplar25)).thenReturn(Arrays.asList(new Rnu25Row(), new Rnu25Row(), new Rnu25Row()));
        when(migrationDao.getRnu64RowList(exemplar64)).thenReturn(Arrays.asList(new Rnu64Row(), new Rnu64Row(), new Rnu64Row(), new Rnu64Row()));
        Assert.assertEquals(3, migrationService.getRnuList(exemplar25).size());
        Assert.assertEquals(4, migrationService.getRnuList(exemplar64).size());
    }


    @Test
    public void startMigrationProcessTest() {
        long[] rnus = {25L, 64L};
        List<Exemplar> exemplarList25 = Arrays.asList(exemplar25);
        List<Exemplar> exemplarList64 = Arrays.asList(exemplar64);

        when(migrationDao.getActualExemplarByRnuType(25)).thenReturn(exemplarList25);
        when(migrationDao.getActualExemplarByRnuType(64)).thenReturn(exemplarList64);

        when(migrationDao.getRnu25RowList(exemplar25)).thenReturn(Arrays.asList(new Rnu25Row(), new Rnu25Row(), new Rnu25Row()));
        when(migrationDao.getRnu64RowList(exemplar64)).thenReturn(Arrays.asList(new Rnu64Row(), new Rnu64Row(), new Rnu64Row(), new Rnu64Row()));

        Assert.assertEquals(2, migrationService.getFiles(rnus).size());
    }
}
