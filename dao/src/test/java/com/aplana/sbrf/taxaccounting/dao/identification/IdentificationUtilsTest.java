package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class IdentificationUtilsTest {

    public static Map<String, RefBookDocType> documentTypeMap =  createDocTypeMap();

    public static Map createDocTypeMap(){
        Map<String, RefBookDocType> map = new HashMap<String, RefBookDocType>();
        map.put("21", createDocType("21", "Паспорт гражданина Российской Федерации", 1));
        map.put("03", createDocType("03", "Свидетельство о рождении", 2));
        map.put("07", createDocType("07", "Военный билет", 3));
        map.put("08", createDocType("08", "Временное удостоверение, выданное взамен военного билета", 4));
        map.put("10", createDocType("10", "Паспорт иностранного гражданина", 5));
        map.put("11", createDocType("11", "Свидетельство о рассмотрении ходатайства о признании лица беженцем на территории Российской Федерации по существу", 6));
        map.put("12", createDocType("12", "Вид на жительство в Российской Федерации", 7));
        map.put("13", createDocType("13", "Удостоверение беженца", 8));
        map.put("14", createDocType("14", "Временное удостоверение личности гражданина Российской Федерации", 9));
        map.put("15", createDocType("15", "Решение на временное проживание в Российской Федерации", 10));
        map.put("18", createDocType("18", "Свидетельство о предоставлении временного убежища на территории Российской Федерации", 11));
        map.put("23", createDocType("23", "Свидетельство о рождении, выданное уполномоченным органом иностранного государства", 12));
        map.put("24", createDocType("24", "Удостоверение личности военнослужащего Российской Федерации", 13));
        map.put("91", createDocType("91", "Иные документы", 14));
        return map;
    }

    @Test
    public void testError(){
        Assert.assertEquals(-1, IdentificationUtils.selectIncludeReportDocumentIndex(null, null));
        Assert.assertEquals(-1, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), new ArrayList<IdDoc>()));
    }

    @Test
    public void testSingleDocuments(){
        List<IdDoc> personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("100000 DDFF", "07"));
        Assert.assertEquals(0, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));
    }

    /**
     * 1. Если ЗСФЛ."Гражданство" = RUS
     */
    @Test
    public void testRussianCitizenshipDocuments(){

        //по номеру серии
        List<IdDoc> personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "21"));
        personDocumentList.add(createPersonDocument("80 01 735634", "21"));
        personDocumentList.add(createPersonDocument("80 08 535634", "21"));
        Assert.assertEquals(2, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));

        //по 6 значному номеру паспорта
        personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "21"));
        personDocumentList.add(createPersonDocument("80 03 735634", "21"));
        personDocumentList.add(createPersonDocument("80 03 535634", "21"));
        Assert.assertEquals(1, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));


        //по номеру временного уд.
        personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "14"));
        personDocumentList.add(createPersonDocument("80 03 735634", "14"));
        personDocumentList.add(createPersonDocument("80 03 535634", "14"));
        Assert.assertEquals(1, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));

        //с минимальным приоритетом
        personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "01"));
        personDocumentList.add(createPersonDocument("80 03 735634", "11"));

        Assert.assertEquals(1, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));

        personDocumentList.add(createPersonDocument("80 03 535634", "08"));
        personDocumentList.add(createPersonDocument("80 03 935634", "08"));
        personDocumentList.add(createPersonDocument("80 03 735634", "08"));
        personDocumentList.add(createPersonDocument("80 03 535634", "01"));
        personDocumentList.add(createPersonDocument("80 03 935634", "91"));

        Assert.assertEquals(2, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));

        personDocumentList.add(createPersonDocument("80 03 735634", "03"));

        Assert.assertEquals(7, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("643", "5"), personDocumentList));

    }


    /**
     * 2. Если ЗСФЛ."Гражданство" ≠ RUS и не пуст и ЗСФЛ."Статус налогоплательщика"  ≠  5
     */
    @Test
    public void testNonRussianDocuments(){
        List<IdDoc> personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "08"));
        personDocumentList.add(createPersonDocument("80 01 735634", "21"));
        personDocumentList.add(createPersonDocument("90 08 535634", "21"));
        Assert.assertEquals(1, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("500", "1"), personDocumentList));
    }

    /**
     *  3. Если ЗСФЛ."Гражданство" ≠ RUS и ЗСФЛ."Статус налогоплательщика" = 5
     */
    @Test
    public void testNonRussianTaxpayerDocuments(){
        List<IdDoc> personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "08"));
        personDocumentList.add(createPersonDocument("80 01 735634", "91"));
        personDocumentList.add(createPersonDocument("90 08 535634", "21"));
        Assert.assertEquals(2, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("500", "5"), personDocumentList));
    }

    /**
     * 4. Если ЗСФЛ."Гражданство" пуст
     */
    @Test
    public void testEmptyCitezenship(){
        List<IdDoc> personDocumentList = new ArrayList<IdDoc>();
        personDocumentList.add(createPersonDocument("80 03 635634", "08"));
        personDocumentList.add(createPersonDocument("80 01 735634", "91"));
        personDocumentList.add(createPersonDocument("90 08 535634", "21"));
        Assert.assertEquals(2, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson(null, "5"), personDocumentList));
        Assert.assertEquals(2, IdentificationUtils.selectIncludeReportDocumentIndex(createNaturalPerson("", "5"), personDocumentList));
    }

    public NaturalPerson createNaturalPerson(String citizenshipCode, String taxpayerStatusCode){

        NaturalPerson person = new NaturalPerson();

        if (citizenshipCode != null){
            RefBookCountry citizenship = new RefBookCountry();
            citizenship.setCode(citizenshipCode);
            person.setCitizenship(citizenship);
        }


        RefBookTaxpayerState taxpayerStatus = new RefBookTaxpayerState();
        taxpayerStatus.setCode(taxpayerStatusCode);
        person.setTaxPayerState(taxpayerStatus);

        return person;
    }

    public static RefBookDocType createDocType(String code, String name, Integer prior){
        RefBookDocType docType = new RefBookDocType();
        docType.setCode(code);
        docType.setName(name);
        docType.setPriority(prior);
        return docType;
    }


    public static IdDoc createPersonDocument(String number, String typeCode){
        IdDoc document = new IdDoc();
        document.setDocumentNumber(number);
        document.setDocType(documentTypeMap.get(typeCode));
        return document;
    }

}
