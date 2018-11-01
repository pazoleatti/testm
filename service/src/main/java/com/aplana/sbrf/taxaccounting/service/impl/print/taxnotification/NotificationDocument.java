package com.aplana.sbrf.taxaccounting.service.impl.print.taxnotification;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Документ Уведомления о неуплаченном НДФЛ.
 */
public class NotificationDocument {

    private String name;
    private XWPFDocument document;

    public NotificationDocument(String name, XWPFDocument document) {
        this.name = name;
        this.document = document;
    }

    public String getName() {
        return name;
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        return outputStream.toByteArray();
    }
}
