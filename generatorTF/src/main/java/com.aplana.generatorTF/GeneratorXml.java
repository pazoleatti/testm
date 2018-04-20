package com.aplana.generatorTF;

import com.aplana.generatorTF.data.InfoPartTag;
import com.aplana.generatorTF.data.OperationInfoTag;

import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

import static com.aplana.generatorTF.Dictionary.*;
import static com.aplana.generatorTF.Main.printStream;
import static com.aplana.generatorTF.Utils.*;

/**
 * Класс для генерации ТФ (xml) РНУ НДФЛ
 */
class GeneratorXml {
    private static final String ENCODING = "Windows-1251";

    private static final String TAG_FILE = "Файл";

    private static final String TAG_SERVICE_PART = "СлЧасть";

    private static final String TAG_INFO_PART = "ИнфЧасть";

    private static final String TAG_INCOME = "ПолучДох";

    private static final String TAG_OPER_INFO = "СведОпер";

    private static final String TAG_INCOME_TAX = "СведДохНал";

    private static final String TAG_DEDUCTION = "СведВыч";

    private static final String TAG_PREPAYMENT = "СведАванс";

    private static final String ATTR_ID_FILE = "ИдФайл";

    private static final String ATTR_INP = "ИНП";

    private static final String ATTR_SNILS = "СНИЛС";

    private static final String ATTR_NAME_1 = "ИмяФЛ";

    private static final String ATTR_NAME_2 = "ОтчФЛ";

    private static final String ATTR_SURNAME = "ФамФЛ";

    private static final String ATTR_PERS_CODE = "УдЛичнФЛКод";

    private static final String ATTR_PERS_CODE_VAL = "21";

    private static final String ATTR_PERS_NUM = "УдЛичнФЛНом";

    private static final String ATTR_BIRTHDAY = "ДатаРожд";

    private static final String ATTR_INN = "ИННФЛ";

    private static final XMLInputFactory inputFactory;

    private static final XMLOutputFactory outputFactory;

    private static final Set<String> EMPTY_TAGS;

    private static final Set<String> NOT_EMPTY_TAGS;

    static {
        inputFactory = XMLInputFactory.newInstance();
        outputFactory = XMLOutputFactory.newInstance();
        EMPTY_TAGS = new HashSet<String>(Arrays.asList(TAG_SERVICE_PART, TAG_INCOME, TAG_INCOME_TAX, TAG_DEDUCTION));
        NOT_EMPTY_TAGS = new HashSet<String>(Arrays.asList(TAG_FILE, TAG_INFO_PART, TAG_OPER_INFO));
    }

    /**
     * Генерация ТФ в формате XML
     *
     * @param sourceFile   Исходный файл
     * @param destFilePath Путь к результирующему файлу
     * @param countTF      Количество блоков ИнфЧасть, которые нужно сгенерировать
     */
    public void generateXML(File sourceFile, String destFilePath, int countTF) {
        try {
            Random random = new Random(System.currentTimeMillis());
            File destFile = new File(destFilePath);

            if (destFilePath.length() > 59) {
                destFilePath = destFilePath.substring(destFilePath.lastIndexOf("\\") + 1, destFilePath.length() - 4);
            }

            printStream.println("Generated new objects count = " + countTF);

            XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileInputStream(sourceFile), ENCODING);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(destFile), ENCODING);
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDTD("\n");

            Stack<Boolean> emptyElements = new Stack<Boolean>();

            boolean generationPerformed = false;
            boolean infoPartStructCreated = false;
            InfoPartTag infoPart = null;
            OperationInfoTag lastOperationInfoTag = null;

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
                                    break;
                                case TAG_INFO_PART:
                                    if (!infoPartStructCreated) {
                                        infoPart = new InfoPartTag(attributes);
                                        infoPartStructCreated = true;
                                    }
                                    break;
                                case TAG_INCOME:
                                    if (infoPartStructCreated) {
                                        infoPart.setIncomeTagAttributes(attributes);
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_INCOME + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_OPER_INFO:
                                    if (infoPartStructCreated) {
                                        lastOperationInfoTag = new OperationInfoTag(attributes);
                                        infoPart.addOperationInfoTag(lastOperationInfoTag);
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_OPER_INFO + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_INCOME_TAX:
                                    if (infoPartStructCreated) {
                                        if (lastOperationInfoTag != null) {
                                            lastOperationInfoTag.addIncomeTaxInfoTagAttributes(attributes);
                                        } else {
                                            throw new RuntimeException("Error while processing tag " + TAG_INCOME_TAX + ". Tag not found: " + TAG_OPER_INFO);
                                        }
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_INCOME_TAX + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_DEDUCTION:
                                    if (infoPartStructCreated) {
                                        if (lastOperationInfoTag != null) {
                                            lastOperationInfoTag.addDeductionInfoTagAttributes(attributes);
                                        } else {
                                            throw new RuntimeException("Error while processing tag " + TAG_DEDUCTION + ". Tag not found: " + TAG_OPER_INFO);
                                        }
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_DEDUCTION + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                                case TAG_PREPAYMENT:
                                    if (infoPartStructCreated) {
                                        if (lastOperationInfoTag != null) {
                                            lastOperationInfoTag.addPrepaymentInfoTagAttributes(attributes);
                                        } else {
                                            throw new RuntimeException("Error while processing tag " + TAG_PREPAYMENT + ". Tag not found: " + TAG_OPER_INFO);
                                        }
                                    } else {
                                        throw new RuntimeException("Error while processing tag " + TAG_PREPAYMENT + ". Tag not found: " + TAG_INFO_PART);
                                    }
                                    break;
                            }

                            if(elementName.equals(TAG_FILE) || elementName.equals(TAG_SERVICE_PART)) {
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
     * Сгенерировать в XML блоки ИнфЧасть на основе сохраненной структцры ИнфЧасть из исходного файла
     *
     * @param infoPartTag   Структура ИнфЧасть, прочитанная из исходного файла
     * @param writer        Объект, выполняющий запись в XML-файл
     * @param random        Генератор случайных чисел
     * @param infoPartLevel Уровень вложенности результирующеих блоков ИнфЧасть
     * @param count         Количество генерируемых блоков
     * @throws XMLStreamException
     */
    private void generateInfoPart(InfoPartTag infoPartTag, XMLStreamWriter writer, Random random, int infoPartLevel, int count) throws XMLStreamException {
        for (int i = 0; i < count; i++) {
            writeElement(writer, TAG_INFO_PART, infoPartLevel, false, infoPartTag.getInfoPartTagAttributes());
            generateFLData(infoPartTag.getIncomeTagAttributes(), random);
            writeElement(writer, TAG_INCOME, infoPartLevel + 1, true, infoPartTag.getIncomeTagAttributes());
            for (OperationInfoTag operInfo : infoPartTag.getOperationInfoTags()) {
                writeElement(writer, TAG_OPER_INFO, infoPartLevel + 2, false, operInfo.getOperationInfoTagAttributes());
                for (Map<String, String> incomeTaxInfoTagAttributes : operInfo.getIncomeTaxInfoTagsAttributesList()) {
                    writeElement(writer, TAG_INCOME_TAX, infoPartLevel + 3, true, incomeTaxInfoTagAttributes);
                }
                for (Map<String, String> deductionInfoTagAttributes : operInfo.getDeductionInfoTagsAttributesList()) {
                    writeElement(writer, TAG_DEDUCTION, infoPartLevel + 3, true, deductionInfoTagAttributes);
                }
                for (Map<String, String> prepaymentInfoTagAttributes : operInfo.getPrepaymentInfoTagsAttributesList()) {
                    writeElement(writer, TAG_PREPAYMENT, infoPartLevel + 3, true, prepaymentInfoTagAttributes);
                }
                writeEndElement(writer, infoPartLevel + 2);
            }
            writeEndElement(writer, infoPartLevel);
        }
    }

    /**
     * Сгенерировать данные для ТФ по ФЛ и записать их в атрибуты тега ПолучДох
     *
     * @param incomeAttributes Атрибуты тега ПолучДох
     * @param random           Генератор случайных чисел
     */
    private void generateFLData(Map<String, String> incomeAttributes, Random random) {
        String inp = String.valueOf(1000000000 + random.nextInt(2000000000));
        incomeAttributes.put(ATTR_INP, inp.length() == 10 ? inp : inp.substring(inp.length() - 10, inp.length()));
        incomeAttributes.put(ATTR_SNILS, generateSnils(random));
        incomeAttributes.put(ATTR_SURNAME, lastnameDictionary.get(random.nextInt(lastnameDictionary.size())));
        incomeAttributes.put(ATTR_NAME_1, firstnameDictionary.get(random.nextInt(firstnameDictionary.size())));
        incomeAttributes.put(ATTR_NAME_2, middlenameDictionary.get(random.nextInt(middlenameDictionary.size())));
        incomeAttributes.put(ATTR_BIRTHDAY, generateDate(random));
        incomeAttributes.put(ATTR_INN, generateInn(random));
        if (incomeAttributes.get(ATTR_PERS_CODE).equals(ATTR_PERS_CODE_VAL)) {
            incomeAttributes.put(ATTR_PERS_NUM, generateNumberDul(random));
        }
    }

    /**
     * Определить количество уже существующих блоков ИнфЧасть в XML-файле
     *
     * @param sourceFile XML-файл
     * @return Количество блоков ИнфЧасть
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    private int countInfoParts(File sourceFile) throws XMLStreamException, FileNotFoundException {
        int infoPartsCount = 0;
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileInputStream(sourceFile), ENCODING);
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getLocalName().equals(TAG_INFO_PART)) {
                    infoPartsCount++;
                }
            }
        }
        reader.close();
        return infoPartsCount;
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
}
