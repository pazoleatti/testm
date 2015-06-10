package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.SourceClientData;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("SourceServiceTest.xml")
public class SourceServiceTest {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    SourceService sourceService;

    @Test
    public void deleteSourcesTest() throws ParseException {
        SourceClientData sourceClientData = new SourceClientData();
        sourceClientData.setDeclaration(true);
        sourceClientData.setDestinationDepartmentId(1);
        sourceClientData.setPeriodStart(SDF.parse("01.01.2014"));
        sourceClientData.setPeriodEnd(SDF.parse("31.12.2014"));
        final SourcePair pair = new SourcePair(1l, 1l);
        pair.setDestinationType("Декларации");
        pair.setSourceType("Форма");
        pair.setSourceKind("Первичная");
        sourceClientData.setSourcePairs(new ArrayList<SourcePair>(){{add(pair);}});
        sourceClientData.setSourceObjects(new ArrayList<SourceObject>() {{
            add(new SourceObject(pair, SDF.parse("01.01.2014"), SDF.parse("31.12.2014")));
        }});

        Logger logger = new Logger();
        sourceService.deleteSources(logger, sourceClientData);
        assertEquals(1, logger.getEntries().size());
        assertEquals("Удалено назначение \"Первичная: Форма\" в роли источника декларации \"Декларации\" в периоде 01.01.2014 - 31.12.2014.", logger.getEntries().get(0).getMessage());
    }
}
