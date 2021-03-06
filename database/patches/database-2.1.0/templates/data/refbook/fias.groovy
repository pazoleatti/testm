package refbook // fias_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.ScriptStatus
import com.aplana.sbrf.taxaccounting.model.ScriptStatusHolder
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.script.service.*
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import java.io.*;
import net.sf.sevenzipjbinding.*;

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.XMLEvent

new Fias(this).run()

class Fias extends AbstractScriptClass {

    IInArchive archive
    ImportFiasDataService importFiasDataService
    ScriptStatusHolder scriptStatusHolder

    private Fias() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    Fias(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("archive")) {
            this.archive = (IInArchive) scriptClass.getProperty("archive")
        }
        if (scriptClass.getBinding().hasVariable("importFiasDataService")) {
            this.importFiasDataService = (ImportFiasDataService) scriptClass.getProperty("importFiasDataService")
        }
        if (scriptClass.getBinding().hasVariable("scriptStatusHolder")) {
            this.scriptStatusHolder = (ScriptStatusHolder) scriptClass.getProperty("scriptStatusHolder")
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.IMPORT_TRANSPORT_FILE:
                importData()
                break
        }
    }

    def BATCH_SIZE_MAX = 1000

    def calcTimeMillis(long time) {
        long currTime = System.currentTimeMillis();
        return " (" + (currTime - time) + " мс)";
    }

    void importData() {

        long time = System.currentTimeMillis();
        logger.info("Начало импорта данных справочника ФИАС");

        // Очистка данных и удаление индексов перед импортом
        try {
            println "Before import FIAS"
            importFiasDataService.beforeImport()
        } catch (Exception e) {
            println "Error before import FIAS"
        }

        logger.info("Предварительная очистка таблиц справочника " + calcTimeMillis(time));

        def itemsMap = createItemsMap(archive);

        //Строим карту Guid адресных объектов, заранее так как будет нужна иерархия
        def addressObjectGuidsMap = buidGuidsMap(getInputStream(archive, itemsMap, "AS_ADDROBJ_"), QName.valueOf('Object'), QName.valueOf('AOGUID'));

        try {
            def FIAS_SOCRBASE_TABLE_NAME = "FIAS_SOCRBASE"; //RefBook.Table.FIAS_SOCRBASE.getTable()
            startImport(getInputStream(archive, itemsMap, "AS_SOCRBASE_"),
                    QName.valueOf('AddressObjectType'),
                    FIAS_SOCRBASE_TABLE_NAME,
                    { generatedId, attr ->
                        addressObjectTypeRowMapper(generatedId, attr)
                    })

            //Начинаем заливать данные из таблиц справочника
            startImport(getInputStream(archive, itemsMap, "AS_ADDROBJ_"),
                    QName.valueOf('Object'),
                    RefBook.Table.FIAS_ADDR_OBJECT.getTable(),
                    { generatedId, attr ->
                        addressObjectRowMapper(generatedId, addressObjectGuidsMap, attr) //здесь для получения id используем подготовленную карту
                    })
        } finally {
        }

        if (logger.containsLevel(LogLevel.ERROR)) {
            scriptStatusHolder.setScriptStatus(ScriptStatus.SKIP)
        } else {
            scriptStatusHolder.setScriptStatus(ScriptStatus.SUCCESS)
        }

        logger.info("Завершение импорта данных справочника ФИАС. Записей загружено: "+addressObjectGuidsMap.size() + calcTimeMillis(time));

    }

    void startImport(fiasInputStream, importedElementName, tableName, rowMapper) {

        println "Start import ${importedElementName} to ${tableName}!"

        def time = System.currentTimeMillis()
        def rowBuffer = new ArrayList<Map<String, ?>>();

        //Используем StAX парсер для импорта
        def xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)

        def reader = xmlFactory.createXMLEventReader(fiasInputStream)
        int i = 1;
        try {
            while (reader.hasNext()) {
                ScriptUtils.checkInterrupted();

                XMLEvent event = reader.nextEvent()

                //Обрабатываем все элементы импорта
                if (event.isStartElement() && event.getName().equals(importedElementName)) {

                    Map attributeMap = getAttributesMap(event)
                    Map rowMap = rowMapper(i, attributeMap)

                    rowBuffer.add(rowMap)
                    i++;
                    //Проверяем если обработали пакет строк размером BATCH_SIZE, то кидаем в сервис и очищаем список
                    if ((i % BATCH_SIZE_MAX) == 0) {
                        importFiasDataService.insertRecords(tableName, rowBuffer)
                        rowBuffer.clear();
                    }

                    if ((i % 100000) == 0) {
                        println "${i} rows of ${importedElementName} process... (" + (System.currentTimeMillis() - time) + " ms)"
                    }

                }
            }
            //Добавляем оставшиеся в списке записи
            importFiasDataService.insertRecords(tableName, rowBuffer);
            rowBuffer.clear();
        } finally {
            reader?.close()
        }

        println "Fias ${importedElementName} (${i-1} rows) import to ${tableName} end (" + (System.currentTimeMillis() - time) + " ms)"
    }

    def createItemsMap(inArchive) {
        def result = [:]
        for (int i = 0; i < inArchive.getNumberOfItems(); i++) {
            result.put(inArchive.getProperty(i, PropID.PATH).toString(), i)
        }
        return result;
    }

    InputStream getInputStream(final IInArchive inArchive, itemsMap, prefix) throws IOException {

        def path = itemsMap.keySet().find { it.startsWith(prefix) }

        if (path == null) {
            throw new ServiceException("В архиве выбранном для загрузки данных ФИАС, отсутствует XML файл с префиксом ${prefix}")
        }


        def i = itemsMap.get(path);

        final PipedInputStream is = new PipedInputStream(320 * 1024);
        final PipedOutputStream out = new PipedOutputStream(is);
        new Thread(new Runnable() {
            public void run() {
                try {
                    inArchive.extractSlow(i, new ISequentialOutStream() {
                        @Override
                        public int write(byte[] bytes) throws SevenZipException {
                            try {
                                out.write(bytes);
                            } catch (IOException e) {
                                throw new SevenZipException(e);
                            }
                            return bytes.length;
                        }
                    });
                } catch (SevenZipException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
        }).start();

        return is;
    }

    Integer getInteger(String val) {
        if (val != null && !val.isEmpty()) {
            return val.toInteger()
        }
        return null
    }

    Integer getInteger(String val, defaultValue) {
        if (val != null && !val.isEmpty()) {
            return val.toInteger()
        }
        return defaultValue
    }

    Integer getLong(String val) {
        if (val != null && !val.isEmpty()) {
            return val.toLong()
        }
        return null
    }

/**
 * Функция возвращает карту атрибут значение
 */
    Map getAttributesMap(event) {
        def result = [:]
        Iterator iterator = event.getAttributes()
        while (iterator.hasNext()) {
            Attribute attribute = (Attribute) iterator.next()
            QName name = attribute.getName()
            String value = attribute.getValue()
            result.put(name, value)
        }
        return result;
    }

    Map operationStatusRowMapper(generatedId, attrMap) {
        def Map recordsMap = new HashMap<String, Object>()
        //меняем OPERSTATID на целочисленный идентификатор
        recordsMap.put('ID', generatedId)
        recordsMap.put('NAME', attrMap.get(QName.valueOf('NAME')))
        return recordsMap;
    }


    Map addressObjectTypeRowMapper(generatedId, attrMap) {
        def Map recordsMap = new HashMap<String, Object>()
        //recordsMap.put('ID', generatedId) //добавляем ID для использования в справочниках
        recordsMap.put('SCNAME', attrMap.get(QName.valueOf('SCNAME')))

        String socrName = attrMap.get(QName.valueOf('SOCRNAME'));

        if (socrName != null && !socrName.isEmpty()){
            recordsMap.put('SOCRNAME', socrName);
        } else {
            recordsMap.put('SOCRNAME', " ");
        }

        recordsMap.put('KOD_T_ST', attrMap.get(QName.valueOf('KOD_T_ST')))
        recordsMap.put('LEV', Integer.parseInt(attrMap.get(QName.valueOf('LEVEL'))))
        return recordsMap;
    }


    Map addressObjectRowMapper(generatedId, addressObjectGuidsMap, attrMap) {
        def Map recordsMap = new HashMap<String, Object>();
        def addressObjectGuid = attrMap.get(QName.valueOf('AOGUID'))
        def parentGuid = attrMap.get(QName.valueOf('PARENTGUID'))

        //меняем AOGUID и PARENTGUID на целочисленный идентификатор
        recordsMap.put('ID', generatedId)
        recordsMap.put('AOID', addressObjectGuidsMap.get(addressObjectGuid))
        recordsMap.put('PARENTGUID', addressObjectGuidsMap.get(parentGuid))
        recordsMap.put('FORMALNAME', attrMap.get(QName.valueOf('FORMALNAME')))
        recordsMap.put('SHORTNAME', attrMap.get(QName.valueOf('SHORTNAME')))
        recordsMap.put('REGIONCODE', attrMap.get(QName.valueOf('REGIONCODE')))
//        recordsMap.put('AUTOCODE', attrMap.get(QName.valueOf('AUTOCODE')))
//        recordsMap.put('AREACODE', attrMap.get(QName.valueOf('AREACODE')))
//        recordsMap.put('CITYCODE', attrMap.get(QName.valueOf('CITYCODE')))
//        recordsMap.put('CTARCODE', attrMap.get(QName.valueOf('CTARCODE')))
//        recordsMap.put('PLACECODE', attrMap.get(QName.valueOf('PLACECODE')))
//        recordsMap.put('PLANCODE', attrMap.get(QName.valueOf('PLANCODE')))
//        recordsMap.put('STREETCODE', attrMap.get(QName.valueOf('STREETCODE')))
//        recordsMap.put('EXTRCODE', attrMap.get(QName.valueOf('EXTRCODE')))
//        recordsMap.put('SEXTCODE', attrMap.get(QName.valueOf('SEXTCODE')))
        recordsMap.put('LIVESTATUS', getInteger(attrMap.get(QName.valueOf('LIVESTATUS'))))
//        recordsMap.put('CENTSTATUS', getInteger(attrMap.get(QName.valueOf('CENTSTATUS'))))
//        recordsMap.put('OPERSTATUS', getInteger(attrMap.get(QName.valueOf('OPERSTATUS'))))
        recordsMap.put('CURRSTATUS', getInteger(attrMap.get(QName.valueOf('CURRSTATUS'))))
        //Поле тип адресации. Хотя поле обязательное в выгрузке его нет, ставим значение 0 - не определено
//    recordsMap.put('DIVTYPE', getInteger(attrMap.get(QName.valueOf('DIVTYPE')), 0))
//    recordsMap.put('OFFNAME', attrMap.get(QName.valueOf('OFFNAME')))
        recordsMap.put('AOLEVEL', attrMap.get(QName.valueOf('AOLEVEL')))
        recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))

        return recordsMap;
    }


    Map houseRowMapper(generatedId, addressObjectGuidMap, houseGuidMap, attrMap) {
        def Map recordsMap = new HashMap<String, Object>()
        def houseGuid = attrMap.get(QName.valueOf('HOUSEGUID'))

        //сохраняем HOUSEGUID и соответсвующий ему идентификатор в карту для использования в room
        if (houseGuidMap.containsKey(houseGuid)){
            houseGuidMap.put(houseGuid, generatedId)
        }

        //меняем HOUSEGUID на целочисленный идентификатор
        recordsMap.put('ID', generatedId)
        recordsMap.put('AOGUID', addressObjectGuidMap.get(attrMap.get(QName.valueOf('AOGUID'))))

        recordsMap.put('ESTSTATUS', getInteger(attrMap.get(QName.valueOf('ESTSTATUS'))))
        recordsMap.put('STRSTATUS', getInteger(attrMap.get(QName.valueOf('STRSTATUS'))))
        recordsMap.put('STATSTATUS', getInteger(attrMap.get(QName.valueOf('STATSTATUS'))))
        recordsMap.put('DIVTYPE', getInteger(attrMap.get(QName.valueOf('DIVTYPE'))))
        recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
        recordsMap.put('HOUSENUM', attrMap.get(QName.valueOf('HOUSENUM')))
        recordsMap.put('BUILDNUM', attrMap.get(QName.valueOf('BUILDNUM')))
        recordsMap.put('STRUCNUM', attrMap.get(QName.valueOf('STRUCNUM')))
        return recordsMap;
    }

    Map houseIntervalRowMapper(generatedId, addressObjectGuidMap, attrMap) {
        def Map recordsMap = new HashMap<String, Object>();

        //меняем INTGUID на целочисленный идентификатор
        recordsMap.put('ID', generatedId)
        recordsMap.put('AOGUID', addressObjectGuidMap.get(attrMap.get(QName.valueOf('AOGUID'))))

        recordsMap.put('INTSTART', getInteger(attrMap.get(QName.valueOf('INTSTART'))))
        recordsMap.put('INTEND', getInteger(attrMap.get(QName.valueOf('INTEND'))))
        recordsMap.put('INTSTATUS', getInteger(attrMap.get(QName.valueOf('INTSTATUS'))))
        recordsMap.put('COUNTER', getInteger(attrMap.get(QName.valueOf('COUNTER'))))
        recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
        return recordsMap;
    }

    Map roomRowMapper(generatedId, houseGuidMap, attrMap) {
        def Map recordsMap = new HashMap<String, Object>();

        //меняем ROOMGUID на целочисленный идентификатор
        recordsMap.put('ID', generatedId)
        recordsMap.put('HOUSEGUID', houseGuidMap.get(attrMap.get(QName.valueOf('HOUSEGUID'))))
        recordsMap.put('REGIONCODE', attrMap.get(QName.valueOf('REGIONCODE')))
        recordsMap.put('FLATNUMBER', attrMap.get(QName.valueOf('FLATNUMBER')))
        recordsMap.put('FLATTYPE', getInteger(attrMap.get(QName.valueOf('FLATTYPE'))))
        recordsMap.put('LIVESTATUS', getInteger(attrMap.get(QName.valueOf('LIVESTATUS'))))
        recordsMap.put('ROOMNUMBER', attrMap.get(QName.valueOf('ROOMNUMBER')))
        recordsMap.put('ROOMTYPEID', getInteger(attrMap.get(QName.valueOf('ROOMTYPEID'))))
        recordsMap.put('POSTALCODE', attrMap.get(QName.valueOf('POSTALCODE')))
        return recordsMap;
    }

/**
 * Построить карту соответсвия Guid к идентификатору
 * @param fiasInputStream
 * @param elementName
 * @param attrName
 * @param attrMapper
 * @return
 */
    def buidGuidsMap(fiasInputStream, elementName, attrName) {

        println "Start build guids from ${elementName} by ${attrName}"

        def result = new HashMap();
        def time = System.currentTimeMillis()

        def xmlFactory = XMLInputFactory.newInstance()
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)

        def reader = xmlFactory.createXMLEventReader(fiasInputStream)
        int i = 1;
        try {
            while (reader.hasNext()) {

                ScriptUtils.checkInterrupted();

                XMLEvent event = reader.nextEvent()
                if (event.isStartElement() && event.getName().equals(elementName)) {
                    Iterator iterator = event.getAttributes()
                    while (iterator.hasNext()) {
                        Attribute attribute = (Attribute) iterator.next()
                        if (attribute.getName().equals(attrName)){
                            String guid = attribute.getValue()
                            result.put(guid, i);
                            i++;
                            break;
                        }
                    }

                    if ((i % 100000) == 0) {
                        println "${i} rows of ${elementName} (" + (System.currentTimeMillis() - time) + " ms) process..."
                    }
                }
            }
        } finally {
            reader?.close()
        }

        println "Build guids end ("+result.size()+ " rows) process ("+(System.currentTimeMillis() - time) + " ms)"

        return result
    }
}

