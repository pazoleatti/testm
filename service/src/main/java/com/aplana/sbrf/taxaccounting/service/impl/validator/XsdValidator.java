package com.aplana.sbrf.taxaccounting.service.impl.validator;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XsdValidator {

    private final List<String> errors = new ArrayList<>();

    public XsdValidator validate(InputStream xml, InputStream xsd) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(xsd));
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            // ignore DTD
            saxParserFactory.setValidating(false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            saxParserFactory.setSchema(schema);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setErrorHandler(new CatchAllErrorHandler());

            try {
                xmlReader.parse(new InputSource(xml));
            } catch (SAXException e) {
                // ошибка в xml, перехватываются в CatchAllErrorHandler
            }
        } catch (IOException | ParserConfigurationException e) {
            throw new ServiceException(e.getMessage(), e);
        } catch (SAXException e) {
            errors.add("Ошибка в xsd: " + e.getMessage());
        }
        return this;
    }

    public List<String> getErrors() {
        return errors;
    }

    private class CatchAllErrorHandler implements ErrorHandler {
        @Override
        public void warning(SAXParseException exception) {
            // nothing
        }

        @Override
        public void error(SAXParseException exception) {
            errors.add(exception.getMessage());
        }

        @Override
        public void fatalError(SAXParseException exception) {
            errors.add(exception.getMessage());
        }
    }
}
