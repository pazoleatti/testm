package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
        person.setCitizenship(new RefBookCountry(0L, SelectPersonDocumentCalc.RUS_CODE));
        List<IdDoc> personDocumentList = new ArrayList<>();
        IdDoc doc1 = new IdDoc();
        IdDoc doc2 = new IdDoc();
        IdDoc doc3 = new IdDoc();
        RefBookDocType docType = new RefBookDocType(0L, SelectPersonDocumentCalc.RUS_PASSPORT_21);
        doc1.setDocType(docType);
        doc1.setDocumentNumber("60 56 183620");
        doc2.setDocType(docType);
        doc2.setDocumentNumber("60 10 183620");
        doc3.setDocType(docType);
        doc3.setDocumentNumber("60 56 283620");
        personDocumentList.addAll(Arrays.asList(doc1, doc2, doc3));
        IdDoc result = SelectPersonDocumentCalc.selectIncludeReportDocument(person, personDocumentList);
        Assert.assertEquals("60 56 283620", result.getDocumentNumber());
    }

    @Test
    public void testSeriesComparator() {
        SelectPersonDocumentCalc.SeriesComparator comparator = new SelectPersonDocumentCalc.SeriesComparator();
        List<Integer> fixture = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            fixture.add(i);
        }
        Collections.shuffle(fixture, new Random(0L));
        Collections.sort(fixture, comparator);
        Assertions.assertThat(fixture.get(0)).isEqualTo(97);
        Assertions.assertThat(fixture.get(fixture.size() - 1)).isEqualTo(96);
        Assertions.assertThat(fixture).containsSequence(97, 98, 99, 0, 1, 2);
        Assertions.assertThat(fixture).containsSequence(94, 95, 96);

    }
}
