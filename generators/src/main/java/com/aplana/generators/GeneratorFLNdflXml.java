package com.aplana.generators;

import com.aplana.generators.data.FL;
import com.aplana.generators.data.InfoPartFLTag;

import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.generators.Main.printStream;

public class GeneratorFLNdflXml {

    private static final String ENCODING = "Windows-1251";

    private static final String TAG_FILE = "Файл";

    private static final String TAG_SERVICE_PART = "СлЧасть";

    private static final String TAG_INFO_PART = "ИнфЧасть";

    private static final String TAG_FL_DATA = "АнкетДаннФЛ";

    private static final String TAG_FL_DOC = "УдЛичнФЛ";

    private static final String TAG_SYSTEM_SOURCE = "СисИсточ";

    private static final String TAG_INCOME_SOURCE = "СведИстДох";

    private static final String ATTR_ID_FL = "ИдФЛ";

    private static final String ATTR_ID_FILE = "ИдФайл";

    private static final String ATTR_UPLOAD_DATE = "ВрВыгр";

    private static final String ATTR_TB_NAME = "ТБ";

    private static final String ATTR_NAME_1 = "ИмяФЛ";

    private static final String ATTR_NAME_2 = "ОтчФЛ";

    private static final String ATTR_SURNAME = "ФамФЛ";

    private static final String ATTR_BIRTHDAY = "ДатаРожд";

    private static final String ATTR_NATIONAL = "Гражд";

    private static final String ATTR_INN = "ИННФЛ";

    private static final String ATTR_STATUS_FL = "СтатусФЛ";

    private static final String ATTR_REGION_CODE = "КодРегион";

    private static final String ATTR_INDEX = "Индекс";

    private static final String ATTR_REGION = "Район";

    private static final String ATTR_TOWN = "НаселПункт";

    private static final String ATTR_CITY = "Город";

    private static final String ATTR_STREET = "Улица";

    private static final String ATTR_HOUSE = "Дом";

    private static final String ATTR_HOUSING = "Корпус";

    private static final String ATTR_FLAT = "Кварт";

    private static final String ATTR_COUNTRY_INO = "КодСтрИно";

    private static final String ATTR_ADВRESS_INO = "АдресИно";

    private static final String ATTR_DUL_KIND = "УдЛичнФЛВид";

    private static final String ATTR_DUL_NUM = "УдЛичнФЛНом";

    private static final String ATTR_DUL_IS_REPORTING = "УдЛичнФЛГл";

    private static final String ATTR_SYST_SOURS_NAME = "СисИсточНам";

    private static final String ATTR_SYST_SOURSE_INP = "СисИсточИНП";

    private static final String ATTR_KPP = "КПП";

    private static final String ATTR_OKTMO = "ОКТМО";

    private static final XMLInputFactory inputFactory;

    private static final XMLOutputFactory outputFactory;

    private static final Set<String> EMPTY_TAGS;

    private static final Set<String> NOT_EMPTY_TAGS;

    private static final String TIME_ZONE_MOSCOW = "Europe/Moscow";

    static {
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        EMPTY_TAGS = new HashSet<String>(Arrays.asList(TAG_SERVICE_PART, TAG_FL_DATA, TAG_FL_DOC, TAG_SYSTEM_SOURCE, TAG_INCOME_SOURCE));
        NOT_EMPTY_TAGS = new HashSet<String>(Arrays.asList(TAG_FILE, TAG_INFO_PART));
    }


    /**
     * Генерация ТФ в формате XML
     *
     * @param sourceFile   Исходный файл
     * @param destFilePath Путь к результирующему файлу
     * @param countTF      Количество блоков ИнфЧасть, которые нужно сгенерировать
     */
    public void generateXML(File sourceFile, String destFilePath, int countTF) {

        XMLStreamReader reader;
        XMLStreamWriter writer;

        try {
            Random random = new Random(System.currentTimeMillis());
            File destFile = new File(destFilePath);

            destFilePath = destFilePath.substring(destFilePath.lastIndexOf("\\") + 1, destFilePath.length() - 4);

            printStream.println("Generated new objects count = " + countTF);

            reader = inputFactory.createXMLStreamReader(new FileInputStream(sourceFile), ENCODING);
            writer = outputFactory.createXMLStreamWriter(new FileOutputStream(destFile), ENCODING);
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDTD("\n");

            Stack<Boolean> emptyElements = new Stack<Boolean>();

            boolean generationPerformed = false;
            boolean infoPartStructCreated = false;
            InfoPartFLTag infoPart = null;

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_DOCUMENT: {
                        break;
                    }
                    case XMLStreamConstants.START_ELEMENT: {
                        String elementName = reader.getLocalName();
                        Map<String, String> namespaces = collectNamespaces(reader);
                        Map<String, String> attributes = collectAttributes(reader);

                        if (EMPTY_TAGS.contains(elementName) || NOT_EMPTY_TAGS.contains(elementName)) {
                            emptyElements.push(EMPTY_TAGS.contains(elementName));

                            switch (elementName) {
                                case TAG_SERVICE_PART:
                                    attributes.put(ATTR_ID_FILE, destFilePath);
                                    attributes.put(ATTR_UPLOAD_DATE, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_MOSCOW)).getTime()));
                                    break;
                                case TAG_INFO_PART:
                                    if (!infoPartStructCreated) {
                                        infoPart = new InfoPartFLTag(attributes);
                                        infoPartStructCreated = true;
                                    }
                                    break;
                                case TAG_FL_DATA:
                                    if (infoPartStructCreated) {
                                        infoPart.setFlDataTagAttributes(attributes);
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_FL_DATA + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_FL_DOC:
                                    if (infoPartStructCreated) {
                                        infoPart.setFlDocTagAttributes(attributes);
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_FL_DOC + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_SYSTEM_SOURCE:
                                    if (infoPartStructCreated) {
                                        infoPart.setSystemSourceTagAttributes(attributes);
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_SYSTEM_SOURCE + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_INCOME_SOURCE:
                                    if (infoPartStructCreated) {
                                        infoPart.setIncomeSourceTagAttributes(attributes);
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_INCOME_SOURCE + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                            }

                            if (elementName.equals(TAG_FILE) || elementName.equals(TAG_SERVICE_PART)) {
                                writeElement(writer, elementName, emptyElements.size() - 1, emptyElements.peek(), attributes, namespaces);
                            }
                        }

                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        emptyElements.pop();
                        if (reader.getLocalName().equals(TAG_INFO_PART) && !generationPerformed) {
                            generateInfoPart(infoPart, writer, random, emptyElements.size(), countTF);
                            generationPerformed = true;
                        }
                        break;
                    }
                    case XMLStreamConstants.END_DOCUMENT: {
                        break;
                    }
                    case XMLStreamConstants.COMMENT: {
                        String text = reader.getText().replace("<!--", "").replace("-->", "");
                        writer.writeComment(text);
                        writer.writeDTD("\n");
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }

            writer.writeEndDocument();
            reader.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Записать тег
     *
     * @param writer      Объект, выполняющий запись в XML-файл
     * @param elementName Имя тего
     * @param level       Уровень вложенности тега
     * @param empty       Есть ли у тега содержимое (если нет, то тег будет непарным)
     * @param attributes  Атрибуты тега
     * @param namespaces  Пространства имен, объявленные у тега
     * @throws XMLStreamException
     */
    private void writeElement(XMLStreamWriter writer, String elementName, int level, boolean empty, Map<String, String> attributes, Map<String, String> namespaces) throws XMLStreamException {
        for (int i = 0; i < level; i++) {
            writer.writeDTD("\t");
        }
        if (empty) {
            writer.writeEmptyElement(elementName);
        } else {
            writer.writeStartElement(elementName);
        }
        for (Map.Entry<String, String> namespace : namespaces.entrySet()) {
            writer.writeNamespace(namespace.getKey(), namespace.getValue());
        }
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            writer.writeAttribute(attribute.getKey(), attribute.getValue());
        }
        writer.writeDTD("\n");
    }

    /**
     * Записать тег. Если у тега есть вложенные теги, записывается открывающий тег, если нет, записывается одиночный тег
     *
     * @param writer      Объект, выполняющий запись в XML-файл
     * @param elementName Имя тега
     * @param level       Уровень вложенности тега
     * @param empty       Есть ли у тега вложенные теги (если нет, то тег будет одиночным)
     * @param attributes  Атрибуты тега
     * @throws XMLStreamException
     */
    private void writeElement(XMLStreamWriter writer, String elementName, int level, boolean empty, Map<String, String> attributes) throws XMLStreamException {
        writeElement(writer, elementName, level, empty, attributes, Collections.EMPTY_MAP);
    }

    /**
     * Записать закрывающий тег
     *
     * @param writer Объект, выполняющий запись в XML-файл
     * @param level  Уровень вложенности тега
     * @throws XMLStreamException
     */
    private static void writeEndElement(XMLStreamWriter writer, int level) throws XMLStreamException {
        for (int i = 0; i < level; i++) {
            writer.writeDTD("\t");
        }
        writer.writeEndElement();
        writer.writeDTD("\n");
    }

    /**
     * Сохранить все пространства имен, объявленные в текущем теге
     *
     * @param reader Объект, выполняющий чтение данных
     * @return Пары префикс-URI
     */
    private static Map<String, String> collectNamespaces(XMLStreamReader reader) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            result.put(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
        }
        return result;
    }

    /**
     * Сохранить все атрибуты текущего тега
     *
     * @param reader Объект, выполняющий чтение данных
     * @return Пары имя-значение
     */
    private static Map<String, String> collectAttributes(XMLStreamReader reader) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            result.put(reader.getAttributeName(i).getLocalPart(), reader.getAttributeValue(i));
        }
        return result;
    }

    /**
     * Сгенерировать в XML блоки ИнфЧасть на основе сохраненной структуры ИнфЧасть из исходного файла
     *
     * @param infoPartTag   Структура ИнфЧасть, прочитанная из исходного файла
     * @param writer        Объект, выполняющий запись в XML-файл
     * @param random        Генератор случайных чисел
     * @param infoPartLevel Уровень вложенности результирующеих блоков ИнфЧасть
     * @param count         Количество генерируемых блоков
     * @throws XMLStreamException
     */
    private void generateInfoPart(InfoPartFLTag infoPartTag, XMLStreamWriter writer, Random random, int infoPartLevel, int count) throws XMLStreamException {
        String sourceIdFL = infoPartTag.getInfoPartTagAttributes().get(ATTR_ID_FL);
        String templateIdFl = sourceIdFL.substring(0, sourceIdFL.length() - 10);
        for (int i = 0; i < count; i++) {
            String idFlEnd = String.valueOf(Math.abs(1000000000 + random.nextInt(2000000000)));
            infoPartTag.getInfoPartTagAttributes().put(ATTR_ID_FL, templateIdFl + idFlEnd);
            writeElement(writer, TAG_INFO_PART, infoPartLevel, false, infoPartTag.getInfoPartTagAttributes());
            generateFLData(infoPartTag.getFlDataTagAttributes(), infoPartTag.getFlDocTagAttributes(), infoPartTag.getSystemSourceTagAttributes());
            writeElement(writer, TAG_FL_DATA, infoPartLevel + 1, true, infoPartTag.getFlDataTagAttributes());
            writeElement(writer, TAG_FL_DOC, infoPartLevel + 1, true, infoPartTag.getFlDocTagAttributes());
            writeElement(writer, TAG_SYSTEM_SOURCE, infoPartLevel + 1, true, infoPartTag.getSystemSourceTagAttributes());
            writeElement(writer, TAG_INCOME_SOURCE, infoPartLevel + 1, true, infoPartTag.getIncomeSourceTagAttributes());
            writeEndElement(writer, infoPartLevel);
        }
    }

    /**
     * Сгенерировать данные для ТФ по ФЛ и записать их в атрибуты тега АнкетДаннФЛ и УдЛичнФЛ
     *
     * @param flDataTagAttributes Атрибуты тега АнкетДаннФЛ
     * @param flDocTagAttributes  Атрибуты тега УдЛичнФЛ
     * @param flSysTagAttributes Атрибуты тега СисИсточ
     */
    private void generateFLData(Map<String, String> flDataTagAttributes, Map<String, String> flDocTagAttributes, Map<String, String> flSysTagAttributes) {
        FL fl = FL.generate();
        flDataTagAttributes.put(ATTR_SURNAME, fl.lastname);
        flDataTagAttributes.put(ATTR_NAME_1, fl.firstname);
        flDataTagAttributes.put(ATTR_NAME_2, fl.middlename);
        flDataTagAttributes.put(ATTR_BIRTHDAY, fl.birthday);
        flDataTagAttributes.put(ATTR_INN, fl.inn);
        flDocTagAttributes.put(ATTR_DUL_NUM, fl.dul);
        flSysTagAttributes.put(ATTR_SYST_SOURSE_INP, fl.inp);
    }
}
