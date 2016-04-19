package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.model.migration.enums.QuartalCode;
import com.aplana.sbrf.taxaccounting.model.migration.enums.SystemType;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu51Row;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu64Row;
import com.aplana.sbrf.taxaccounting.model.migration.row.RnuCommonRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Генератор фалов XML
 */
public class XmlMigrationGenerator {

    private static final ThreadLocal<SimpleDateFormat> exemplarSDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> fieldSDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static Transformer transformer;

    static {
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerConfigurationException e) {
            throw new ServiceException("TransformerFactory failed. " + e.getLocalizedMessage());
        }
    }

    private static final String NAME = "name";
    private static final String NULL = "null";
    private static final String VALUE = "value";

    public String generateXmlFileToString(Exemplar ex, List<? extends AbstractRnuRow> rnuRows) {
        Document document = createDocument(ex, rnuRows);
        return xmlDocumentToString(document);
    }

    public byte[] generateXmlFileToBytes(Exemplar ex, List<? extends AbstractRnuRow> rnuRows) {
        Document document = createDocument(ex, rnuRows);
        return xmlDocumentToBytes(document);
    }

    private Document createDocument(Exemplar ex, List<? extends AbstractRnuRow> rnuRows) {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new XmlSerializationException(e);
        }

        Element root = document.createElement("form");
        root.setAttribute("xmlns", "http://sberbank.ru/XMLSchemas/sberrnu");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        document.appendChild(root);

        Element exemplar = getExemplarElement(ex, document);
        root.appendChild(exemplar);

        exemplar.appendChild(getTableElement(document, rnuRows));
        return document;
    }

    private String xmlDocumentToString(Document doc) {
        try {
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new XmlSerializationException("Error by converting document to string.", e);
        }
    }

    private byte[] xmlDocumentToBytes(Document doc) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(stream);
            DOMSource source = new DOMSource(doc);

            transformer.transform(source, result);

            return stream.toByteArray();
        } catch (TransformerException e) {
            throw new XmlSerializationException(
                    "Error by converting document to bytes.", e);
        }
    }

    public String getXmlFileName(Exemplar ex) {
        StringBuilder sb = new StringBuilder();

        String type = NalogFormType.getNewXmlCode(ex.getRnuTypeId());
        sb.append(completeStringLength(10, type, null));

        sb.append(ex.getDepCode());

        String systemCodeNew = String.valueOf(SystemType.fromId(ex.getSystemId()).getCodeNew());
        sb.append(completeStringLength(5, systemCodeNew, true)).append(ex.getSubSystemId());

        DateFormat year = new SimpleDateFormat("yyyy");
        Integer month = Integer.valueOf(new SimpleDateFormat("MM").format(ex.getBeginDate()));

        switch (ex.getPeriodityId()) {
            case 1:            //Ежегодно
                sb.append("y12");
                break;
            case 4:             //Ежеквартально
                sb.append(QuartalCode.fromNum(month).getCodeString());
                break;
            case 5:            //Ежемесячно
                sb.append('m').append(year.format(ex.getBeginDate()));
                break;
            case 8:             //ежедневно и по рабочим дням
            case 10:
            default:
        }

        sb.append(year.format(ex.getBeginDate()).substring(2));
        sb.append(".xml");
        return sb.toString();
    }

    /**
     * Дополняет символами '_' до необходимого количества символов
     *
     * @param lengthNeed    длина выходной строки
     * @param str           входная строка
     * @param leftAppending true - добавлять слева, false или null - справа
     * @return дополненная строка
     */
    private String completeStringLength(Integer lengthNeed, String str, Boolean leftAppending) {
        if (str == null || lengthNeed <= str.length()) {
            return str;
        } else {

            StringBuilder sb = new StringBuilder(str);
            if (leftAppending != null && leftAppending) {
                for (int i = 0; i < lengthNeed - str.length(); i++) {
                    sb.insert(0, '_');
                }
            } else {
                for (int i = 0; i < lengthNeed - str.length(); i++) {
                    sb.append('_');
                }
            }
            return sb.toString();
        }
    }

    private Element getExemplarElement(Exemplar ex, Document document) {
        Element element = document.createElement("exemplar");

        element.setAttribute("objdict", NalogFormType.getNewXmlCode(ex.getRnuTypeId()));

        element.setAttribute("tb", ex.getDepCode().substring(0, 2));
        element.setAttribute("branch", ex.getDepCode().substring(2, 6));
        element.setAttribute("subbranch", ex.getDepCode().substring(6, 8));
        element.setAttribute("fld", ex.getDepCode().substring(8, 9));

        element.setAttribute("asystem", String.valueOf(SystemType.fromId(ex.getSystemId()).getCodeNew()));
        element.setAttribute("subasystem", ex.getSubSystemId());
        element.setAttribute("datebegin", exemplarSDF.get().format(ex.getBeginDate()));
        element.setAttribute("dateend", exemplarSDF.get().format(ex.getEndDate()));

        //element.setAttribute("dxx","");  //TODO узнать откуда его брать или нужен ли он вообще
        return element;
    }

    private Element getTableElement(Document doc, List<? extends AbstractRnuRow> rnuRows) {
        Element table = doc.createElement("table");

        Element total = doc.createElement("total");
        Element detail = doc.createElement("detail");

        table.appendChild(detail);
        table.appendChild(total);

        for (AbstractRnuRow row : rnuRows) {
            if (null == row.getTypeRow()) {
                detail.appendChild(getRecordElement(row, doc));
            } else {
                total.appendChild(getRecordElement(row, doc));
            }
        }
        return table;
    }

    private Element getRecordElement(AbstractRnuRow abstractRnuRow, Document doc) {
        if (abstractRnuRow instanceof Rnu64Row) {
            return generateRecordXml((Rnu64Row) abstractRnuRow, doc);
        } else if (abstractRnuRow instanceof RnuCommonRow) {
            return generateRecordXml((RnuCommonRow) abstractRnuRow, doc);
        } else if (abstractRnuRow instanceof Rnu51Row) {
            return generateRecordXml((Rnu51Row) abstractRnuRow, doc);
        } else {
            throw new ServiceException("Ошибка формирования XML файла.");
        }

    }

    private Element generateRecordXml(Rnu64Row row, Document doc) {
        Element record = doc.createElement("record");

        record.appendChild(getFieldElement("NUM", row.getNum(), doc));
        record.appendChild(getFieldElement("DDEAL", formatDate(row.getDealDate()), doc));
        record.appendChild(getFieldElement("PARTDEAL", row.getPartDeal(), doc));
        record.appendChild(getFieldElement("NUMDEAL", row.getNumDeal(), doc, true));
        record.appendChild(getFieldElement("DEFPAPER", row.getDefPaper(), doc, true));
        record.appendChild(getFieldElement("RCOST", row.getCost(), doc));

        return record;
    }

    private Element generateRecordXml(RnuCommonRow row, Document doc) {
        Element record = doc.createElement("record");

        record.appendChild(getFieldElement("NUMDEAL", row.getNumDeal(), doc, true));
        record.appendChild(getFieldElement("DEFPAPER", row.getDefPaper(), doc, true));
        record.appendChild(getFieldElement("CODECURRENCY", row.getCodecurrency(), doc, true));
        record.appendChild(getFieldElement("NOMPAPER", row.getNompaper(), doc));
        record.appendChild(getFieldElement("GETPRICENKD", row.getGetpricenkd(), doc));
        record.appendChild(getFieldElement("SALEPRICENKD", row.getSalepricenkd(), doc));
        record.appendChild(getFieldElement("DREPO1", formatDate(row.getDrepo1()), doc));
        record.appendChild(getFieldElement("DREPO2", formatDate(row.getDrepo2()), doc));
        record.appendChild(getFieldElement("COSTREPO", row.getCostrepo(), doc));
        record.appendChild(getFieldElement("IMPLREPO", row.getImplrepo(), doc));
        record.appendChild(getFieldElement("BANKRATE", row.getBankrate(), doc));
        record.appendChild(getFieldElement("COSTREPO269", row.getCostrepo269(), doc));
        record.appendChild(getFieldElement("COSTREPOTAX", row.getCostrepotax(), doc));

        return record;
    }

    private Element generateRecordXml(Rnu51Row row, Document doc) {
        Element record = doc.createElement("record");

        record.appendChild(getFieldElement("NUM", row.getNum(), doc));
        record.appendChild(getFieldElement("CODEDEAL", row.getCodedeal(), doc));
        record.appendChild(getFieldElement("TYPEPAPER", row.getTypepaper(), doc, true));
        record.appendChild(getFieldElement("DEFPAPER", row.getDefpaper(), doc, true));
        record.appendChild(getFieldElement("DGET", formatDate(row.getDget()), doc));
        record.appendChild(getFieldElement("DIMPL", formatDate(row.getDimpl()), doc));
        record.appendChild(getFieldElement("NUMPAPER", row.getNumpaper(), doc));
        record.appendChild(getFieldElement("RGETPRICE", row.getRgetprice(), doc));
        record.appendChild(getFieldElement("RGETCOST", row.getRgetcost(), doc));
        record.appendChild(getFieldElement("GETMPRICEPERC", row.getGetmpriceperc(), doc));
        record.appendChild(getFieldElement("GETMPRICE", row.getGetmprice(), doc));
        record.appendChild(getFieldElement("GETSALEPRICETAX", row.getGetsalepricetax(), doc));
        record.appendChild(getFieldElement("RSUMEXT", row.getRsumext(), doc));
        record.appendChild(getFieldElement("SALEPRICEPERC", row.getSalepriceperc(), doc));
        record.appendChild(getFieldElement("RSALEPRICE", row.getRsaleprice(), doc));
        record.appendChild(getFieldElement("MARKETPRICEPERC", row.getMarketpriceperc(), doc));
        record.appendChild(getFieldElement("RMARKETPRICE", row.getRmarketprice(), doc));
        record.appendChild(getFieldElement("RSALEPRICETAX", row.getRsalepricetax(), doc));
        record.appendChild(getFieldElement("RCOST", row.getRcost(), doc));
        record.appendChild(getFieldElement("RTOTALCOST", row.getRtotalcost(), doc));
        record.appendChild(getFieldElement("RPROFITCOST", row.getRprofitcost(), doc));
        record.appendChild(getFieldElement("ROVWRPRICE", row.getRovwrprice(), doc));

        return record;
    }

    private Element getFieldElement(String name, Object value, Document doc) {
        return getFieldElement(name, value, doc, false);
    }

    private Element getFieldElement(String name, Object value, Document doc, Boolean isCDATA) {
        Element field = doc.createElement("field");
        Boolean isNull = value == null;
        field.setAttribute(NAME, name);
        field.setAttribute(NULL, isNull.toString());
        if (!isNull) {
            if (isCDATA) {
                field.appendChild(doc.createCDATASection(value.toString()));
            } else {
                field.setAttribute(VALUE, value.toString());
            }
        }
        return field;
    }

    private String formatDate(Date date) {
        return date == null ? null : fieldSDF.get().format(date);
    }

}
