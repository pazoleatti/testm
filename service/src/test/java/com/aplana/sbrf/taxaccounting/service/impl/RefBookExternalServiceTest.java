package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dmitriy Levykin
 */
public class RefBookExternalServiceTest {
    RefBookExternalService refBookExternalService = new RefBookExternalServiceImpl();

    @Test
    public void isNSIFileTest() {
         // ЦАС НСИ
        for (String name : new String[]{"OKA99VVV.RR", "payments.OKATO.9999.VVV.RR", "OKAdd777.99",
                "payments.OKATO.f00f.h0h.00", "RNU00VVV.RR", "generaluse.AS_RNU.VVV.RR", "bookkeeping.Bookkeeping.VVV.RR"}) {
            Assert.assertTrue("File \"" + name + "\" is NSI file!", refBookExternalService.isNSIFile(name));
        }
        // Не ЦАС НСИ
        for (String name : new String[]{null, "OKA99VVVV.RR", "OKA99VVV..RR", "OKA.", "OKA99VVVV.",
                "payments.OKATO.9999......RR", "payments.OKATO.99999VVV.RR", "OKAdd77799", "payments.OKATO.f00f.h0h.000",
                "RNU.", "generaluse.AS_RNU.", "bookkeeping.Bookkeeping.", "bookkeeping.Bookkeeping.000000"}) {
            Assert.assertFalse("File \"" + name + "\" is not NSI file!", refBookExternalService.isNSIFile(name));
        }
    }

    @Test
    public void isDiasoftFileTest() {
        // Diasoft Custody
        for (String name : new String[]{"DS240512.nsi", "ds240512.NSI", "DS000000.nsi", "DS999999.nsi"}) {
            Assert.assertTrue("File \"" + name + "\" is Diasoft Custody file!", refBookExternalService.isDiasoftFile(name));
        }
        // Не Diasoft Custody
        for (String name : new String[]{null, "/", "DS2405121.nsi", "DS240512nsi", "DS240512.nsi0", "DS240512.nsi.", "DS240512..nsi"}) {
            Assert.assertFalse("File \"" + name + "\" is not Diasoft Custody file!", refBookExternalService.isDiasoftFile(name));
        }
    }
}
