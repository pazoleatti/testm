package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.*;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu60Row;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu64Row;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.service.XmlGenerationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.*;

@Service
@Transactional
public class XmlGenerationServiceImpl implements XmlGenerationService {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    MigrationService migrationService;

    private static final SimpleDateFormat exemplarSDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat fieldSDF = new SimpleDateFormat("dd.MM.yyyy");

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

    @Override
    public String generateXmlFileToString(Exemplar ex) {
        return documentToString(createDocument(ex));
    }

    @Override
    public byte[] generateXmlFileToBytes(Exemplar ex) {
        return documentToBytes(createDocument(ex));
    }

    private Document createDocument(Exemplar ex) {
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

        exemplar.appendChild(getTableElement(ex, document));
        return document;
    }

    private String documentToString(Document doc) {
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

    private byte[] documentToBytes(Document doc) {
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

    @Override
    public String getXmlFileName(Exemplar ex) {
        StringBuilder sb = new StringBuilder();

        String type = NalogFormType.getNewXmlCode(ex.getRnuTypeId());
        sb.append(completeStringLength(10, type));

        sb.append(completeStringLength(11, ex.getDepCode()));

        sb.append(SystemType.fromId(ex.getSystemId()).getCodeNew()).append(ex.getSubSystemId());

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

    private String completeStringLength(Integer lengthNeed, String str) {
        if (str == null || lengthNeed <= str.length()) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);
            for (int i = 0; i < lengthNeed - str.length(); i++) {
                sb.append('_');
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
        element.setAttribute("datebegin", exemplarSDF.format(ex.getBeginDate()));
        element.setAttribute("dateend", exemplarSDF.format(ex.getEndDate()));

        //element.setAttribute("dxx","");  //TODO узнать откуда его брать или нужен ли он вообще
        return element;
    }

    private Element getTableElement(Exemplar exemplar, Document doc) {
        Element table = doc.createElement("table");

        Element total = doc.createElement("total");
        Element detail = doc.createElement("detail");

        table.appendChild(detail);
        table.appendChild(total);

        List<? extends AbstractRnuRow> rnuRows = migrationService.getRnuList(exemplar);

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
        } else if (abstractRnuRow instanceof Rnu60Row) {
            return generateRecordXml((Rnu60Row) abstractRnuRow, doc);
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
        record.appendChild(getFieldElement("RCOST", row.getCost(), doc));

        return record;
    }

    private Element generateRecordXml(Rnu60Row row, Document doc) {
        Element record = doc.createElement("record");

        record.appendChild(getFieldElement("NUM", row.getNum(), doc));
        record.appendChild(getFieldElement("NUMDEAL", row.getNumDeal(), doc, true));
        record.appendChild(getFieldElement("DEFPAPER", row.getDefPaper(), doc, true));
        record.appendChild(getFieldElement("CODECURRENCY", row.getCodecurrency(), doc, true));
        record.appendChild(getFieldElement("DREPO1", formatDate(row.getDrepo1()), doc));
        record.appendChild(getFieldElement("DREPO2", formatDate(row.getDrepo2()), doc));
        record.appendChild(getFieldElement("GETPRICENKD", row.getGetpricenkd(), doc));
        record.appendChild(getFieldElement("SALEPRICENKD", row.getSalepricenkd(), doc));
        record.appendChild(getFieldElement("COSTREPO", row.getCostrepo(), doc));
        record.appendChild(getFieldElement("IMPLREPO", row.getImplrepo(), doc));
        record.appendChild(getFieldElement("BANKRATE", row.getBankrate(), doc));
        record.appendChild(getFieldElement("COSTREPO269", row.getCostrepo269(), doc));
        record.appendChild(getFieldElement("COSTREPOTAX", row.getCostrepotax(), doc));

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
        return date == null ? null : fieldSDF.format(date);
    }

}
