package com.aplana.sbrf.taxaccounting.dao.identification;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

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

}
