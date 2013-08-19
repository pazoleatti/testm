package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.Exemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.*;
import com.aplana.sbrf.taxaccounting.model.migration.row.AbstractRnuRow;
import com.aplana.sbrf.taxaccounting.model.migration.row.Rnu64Row;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.service.XmlGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class XmlGenerationServiceImpl implements XmlGenerationService {

    @Autowired
    MigrationService migrationService;

    private static final SimpleDateFormat exemplarSDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat fieldSDF = new SimpleDateFormat("dd.MM.yyyy");

    private static final String NAME = "name";
    private static final String NULL = "null";
    private static final String VALUE = "value";

    @Override
    public String generateXmlFile(Exemplar ex) {

        Document document;

        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new XmlSerializationException(e);
        }

        Element root = document.createElement("form");
        root.setAttribute("xmlns", "http://sberrnu");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        document.appendChild(root);

        Element exemplar = getExemplarElement(ex, document);
        root.appendChild(exemplar);

        exemplar.appendChild(getTableElement(ex, document));

        return documentToString(document);
    }

    private String documentToString(Document doc) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            return writer.toString();
        } catch (TransformerException e) {
            throw new XmlSerializationException(
                    "Error by converting document to string.", e);
        }
    }

    @Override
    public String getXmlFileName(Exemplar ex) {
        //TODO
        return null;
    }

    private Element getExemplarElement(Exemplar ex, Document document) {
        Element element = document.createElement("exemplar");

        element.setAttribute("objdict", NalogFormType.getNewXmlCode(ex.getRnuTypeId()));

        element.setAttribute("tb", ex.getDepCode().substring(0, 2));
        element.setAttribute("branch", ex.getDepCode().substring(2, 6));
        element.setAttribute("subbranch", ex.getDepCode().substring(6, 8));
        element.setAttribute("fld", ex.getDepCode().substring(8, 9));

        element.setAttribute("dep_id", String.valueOf(
                DeparmanetCode.getNewDepCode(
                        ex.getDepCode(),
                        SystemType.getNewCodeByOldCode(ex.getSystemId()),
                        ex.getSubSystemId()))
        );     //дополнительный атрибут на всякий случай

        element.setAttribute("asystem", String.valueOf(SystemType.getNewCodeByOldCode(ex.getSystemId())));
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
