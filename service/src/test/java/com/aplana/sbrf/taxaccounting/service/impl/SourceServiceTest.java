package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.source.ConsolidatedInstance;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("SourceServiceTest.xml")
public class SourceServiceTest {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    SourceService sourceService;
    @Autowired
    SourceDao sourceDao;

    @Test
    public void deleteSourcesTest() throws ParseException {
        SourceClientData sourceClientData = new SourceClientData();
        sourceClientData.setDeclaration(true);
        sourceClientData.setDestinationDepartmentId(1);
        sourceClientData.setPeriodStart(SDF.parse("01.01.2014"));
        sourceClientData.setPeriodEnd(SDF.parse("31.12.2014"));
        final SourcePair pair = new SourcePair(1l, 1l);
        pair.setDestinationType("Налоговые формы");
        pair.setSourceType("Форма");
        pair.setSourceKind("Первичная");
        pair.setDestinationDepartmentName("Банк-приемник");
        pair.setSourceDepartmentName("Банк-источник");
        sourceClientData.setSourcePairs(new ArrayList<SourcePair>(){{add(pair);}});
        sourceClientData.setSourceObjects(new ArrayList<SourceObject>() {{
            add(new SourceObject(pair, SDF.parse("01.01.2014"), SDF.parse("31.12.2014")));
        }});

        Logger logger = new Logger();
        sourceService.deleteSources(logger, sourceClientData);
        assertEquals(1, logger.getEntries().size());
        assertEquals("Удалено назначение \"Банк-источник, Первичная: Форма\" в роли источника налоговой формы \"Банк-приемник, Налоговые формы\" в периоде 01.01.2014 - 31.12.2014.", logger.getEntries().get(0).getMessage());
    }
}
