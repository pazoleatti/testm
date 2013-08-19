package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import org.junit.Before;

import java.util.Date;

public class MigrationServiceTest {
    private MigrationServiceImpl migrationService;
    private Exemplar exemplar;

    @Before
    public void tearUp() {
        migrationService = new MigrationServiceImpl();
        exemplar = new Exemplar();
        exemplar.setSubSystemId("00");
        exemplar.setExemplarId(116747L);
        exemplar.setDepCode("0013");
        exemplar.setBeginDate(new Date());
        exemplar.setEndDate(new Date());
        exemplar.setPeriodityId(4);
        exemplar.setRnuTypeId(25);
        exemplar.setSystemId(36);
        exemplar.setTerCode("099");
    }

}
