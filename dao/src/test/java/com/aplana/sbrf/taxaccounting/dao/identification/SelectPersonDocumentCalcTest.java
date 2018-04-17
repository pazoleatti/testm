package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.Country;
import com.aplana.sbrf.taxaccounting.model.identification.DocType;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrey Drunk
 */
public class SelectPersonDocumentCalcTest {

    @Test
    public void extractDocNumberDigitsTest() {

        Assert.assertEquals(new BigInteger("8003635634"), SelectPersonDocumentCalc.extractDocNumberDigits("80 03 635634"));
        Assert.assertEquals(new BigInteger("8003635634"), SelectPersonDocumentCalc.extractDocNumberDigits("80 03 635634 СШ"));
        Assert.assertEquals(new BigInteger("2001123456"), SelectPersonDocumentCalc.extractDocNumberDigits("2001ЯЯ 123456 ЩЩЁЁ"));
        Assert.assertNull(SelectPersonDocumentCalc.extractDocNumberDigits(""));
        Assert.assertNull(SelectPersonDocumentCalc.extractDocNumberDigits(null));

        Assert.assertEquals(Integer.valueOf(635634), SelectPersonDocumentCalc.extractPassportDocumentsDigits("80 03 635634"));
        Assert.assertEquals(Integer.valueOf(635634), SelectPersonDocumentCalc.extractPassportDocumentsDigits("80 03 635634 СШ"));
        Assert.assertEquals(Integer.valueOf(123456), SelectPersonDocumentCalc.extractPassportDocumentsDigits("2001ЯЯ 123456 ЩЩЁЁ"));
        Assert.assertNull(SelectPersonDocumentCalc.extractPassportDocumentsDigits(""));
        Assert.assertNull(SelectPersonDocumentCalc.extractPassportDocumentsDigits(null));

        Assert.assertEquals(Integer.valueOf(3), SelectPersonDocumentCalc.extractSeriesDigits("80 03 635634"));
        Assert.assertEquals(Integer.valueOf(3), SelectPersonDocumentCalc.extractSeriesDigits("80 03 635634 СШ"));
        Assert.assertEquals(Integer.valueOf(1), SelectPersonDocumentCalc.extractSeriesDigits("2001ЯЯ 123456 ЩЩЁЁ"));
        Assert.assertNull(SelectPersonDocumentCalc.extractSeriesDigits(""));
        Assert.assertNull(SelectPersonDocumentCalc.extractSeriesDigits(null));

    }

    @Test
    public void selectIncludeReportDocumentCode21Test() {
        NaturalPerson person = new NaturalPerson();
        person.setCitizenship(new Country(0L, SelectPersonDocumentCalc.RUS_CODE));
        List<PersonDocument> personDocumentList = new ArrayList<>();
        PersonDocument doc1 = new PersonDocument();
        PersonDocument doc2 = new PersonDocument();
        PersonDocument doc3 = new PersonDocument();
        DocType docType = new DocType(0L, SelectPersonDocumentCalc.RUS_PASSPORT_21);
        doc1.setDocType(docType);
        doc1.setDocumentNumber("60 56 183620");
        doc2.setDocType(docType);
        doc2.setDocumentNumber("60 10 183620");
        doc3.setDocType(docType);
        doc3.setDocumentNumber("60 56 283620");
        personDocumentList.addAll(Arrays.asList(doc1, doc2, doc3));
        PersonDocument result = SelectPersonDocumentCalc.selectIncludeReportDocument(person, personDocumentList);
        Assert.assertEquals("60 56 283620", result.getDocumentNumber());
    }
}
