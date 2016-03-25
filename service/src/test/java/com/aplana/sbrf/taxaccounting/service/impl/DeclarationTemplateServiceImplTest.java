package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeclarationTemplateServiceImplTest {

    @Test
    public void validateDeclarationTemplate1() {
        DeclarationTemplateService declarationTemplateService = new DeclarationTemplateServiceImpl();

        Logger logger = new Logger();

        DeclarationTemplate declarationTemplate = new DeclarationTemplate();
        List<DeclarationSubreport> subreports = new ArrayList<DeclarationSubreport>();
        DeclarationSubreport subreport1 = new DeclarationSubreport();
        subreport1.setOrder(1);
        subreport1.setAlias("alias1");
        DeclarationSubreport subreport2 = new DeclarationSubreport();
        subreport2.setOrder(2);
        subreport2.setName("name2");
        DeclarationSubreport subreport3 = new DeclarationSubreport();
        subreport3.setOrder(3);
        DeclarationSubreport subreport4 = new DeclarationSubreport();
        subreport4.setOrder(4);
        subreport4.setName("name4");
        subreport4.setAlias("alias4");
        DeclarationSubreport subreport5 = new DeclarationSubreport();
        subreport5.setOrder(5);
        subreport5.setName("name4");
        subreport5.setAlias("alias4");
        DeclarationSubreport subreport6 = new DeclarationSubreport();
        subreport6.setOrder(6);
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 100; i++)
            stringBuilder.append("1234567890");
        subreport6.setAlias(stringBuilder.toString());
        stringBuilder.append("1234567890");
        subreport6.setName(stringBuilder.toString());

        subreports.add(subreport1);
        subreports.add(subreport2);
        subreports.add(subreport3);
        subreports.add(subreport4);
        subreports.add(subreport5);
        subreports.add(subreport6);
        declarationTemplate.setSubreports(subreports);

        declarationTemplateService.validateDeclarationTemplate(declarationTemplate, logger);

        Assert.assertEquals(logger.getEntries().get(0).getMessage(), "Отчет №\"1\". Поле \"Наименование\" обязательно для заполнения.");
        Assert.assertEquals(logger.getEntries().get(1).getMessage(), "Отчет №\"2\". Поле \"Псевдоним\" обязательно для заполнения.");
        Assert.assertEquals(logger.getEntries().get(2).getMessage(), "Отчет №\"3\". Поле \"Псевдоним\" обязательно для заполнения.");
        Assert.assertEquals(logger.getEntries().get(3).getMessage(), "Отчет №\"3\". Поле \"Наименование\" обязательно для заполнения.");
        Assert.assertEquals(logger.getEntries().get(4).getMessage(), "Отчет №\"5\". Нарушено требование к уникальности, уже существует отчет с псевдонимом \"alias4\" в данной версии макета!");
        Assert.assertEquals(logger.getEntries().get(5).getMessage(), "Отчет №\"6\". Значение для псевдонима отчета слишком велико (фактическое: 1000, максимальное: 128)");
        Assert.assertEquals(logger.getEntries().get(6).getMessage(), "Отчет №\"6\". Значение для имени отчета слишком велико (фактическое: 1010, максимальное: 1000)");
    }
}
