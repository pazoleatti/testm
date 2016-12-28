package form_template.raschsv

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvFile
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvPersSvStrahLic
import groovy.transform.Field

@Field final PATTERN_DATE_FORMAT = "dd.mm.yyyy"

// Узлы
@Field final NODE_NAME_DOCUMENT = "Документ"
@Field final NODE_NAME_RASCHET_SV = "РасчетСВ"
@Field final NODE_NAME_OBYAZ_PLAT_SV = "ОбязПлатСВ"
@Field final NODE_NAME_PERV_SV_STRAH_LIC = "ПерсСвСтрахЛиц"
@Field final DOCUMENT_ID_FILE = "@ИдФайл"

// Атрибуты ПерсСвСтрахЛиц
@Field final PERV_SV_STRAH_LIC_NOM_KORR = 'НомКорр'
@Field final PERV_SV_STRAH_LIC_PERIOD = "Период"
@Field final PERV_SV_STRAH_LIC_OTCHET_GOD = "ОтчетГод"
@Field final PERV_SV_STRAH_LIC_NOMER = "Номер"
@Field final PERV_SV_STRAH_LIC_SV_DATA = "Дата"

switch (formDataEvent) {
    case FormDataEvent.IMPORT:
        parseRaschsv()
        break
    default:
        break
}

void parseRaschsv() {

    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    def fileNode = new XmlSlurper().parse(xmlInputStream);
    if (fileNode == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }

    // Набор ПерсСвСтрахЛиц
    def raschsvPersSvStrahLicList = []

    RaschsvFile raschsvFile = new RaschsvFile()
    raschsvFile.idFile = fileNode.DOCUMENT_ID_FILE
    Long raschsvFileId = raschsvFileService.insert(raschsvFile)

    fileNode.childNodes().each { documentNode ->
        if (documentNode.name == NODE_NAME_DOCUMENT) {
            documentNode.childNodes().each { raschetSvNode ->
                if (raschetSvNode.name == NODE_NAME_RASCHET_SV) {
                    raschetSvNode.childNodes().each { node ->
                        if (node.name == NODE_NAME_OBYAZ_PLAT_SV) {
                            // Разбор ветки ОбязПлатСВ
                        } else if (node.name == NODE_NAME_PERV_SV_STRAH_LIC) {
                            // Разбор ветки ПерсСвСтрахЛиц
                            raschsvPersSvStrahLicList.add(parseRaschsvPersSvStrahLic(node, raschsvFileId))
                        }
                    }
                }
            }
        }
    }
    println(raschsvPersSvStrahLicService.insert(raschsvPersSvStrahLicList))
//    logger.error("Запись не может быть добавлена!")
}

/**
 * Разбор ветки ПерсСвСтрахЛиц
 * @param node
 * @param raschsvFileId - идентификатор файла
 * @return
 */
RaschsvPersSvStrahLic parseRaschsvPersSvStrahLic(Object node, Long raschsvFileId) {
    RaschsvPersSvStrahLic raschsvPersSvStrahLic = new RaschsvPersSvStrahLic()
    raschsvPersSvStrahLic.raschsvFileId = raschsvFileId
    raschsvPersSvStrahLic.nomKorr = getInteger(node.attributes()[PERV_SV_STRAH_LIC_NOM_KORR])
    raschsvPersSvStrahLic.period = node.attributes()[PERV_SV_STRAH_LIC_PERIOD]
    raschsvPersSvStrahLic.otchetGod = node.attributes()[PERV_SV_STRAH_LIC_OTCHET_GOD]
    raschsvPersSvStrahLic.nomer = getInteger(node.attributes()[PERV_SV_STRAH_LIC_NOMER])
    raschsvPersSvStrahLic.svData = getDate(node.attributes()[PERV_SV_STRAH_LIC_SV_DATA])
    return raschsvPersSvStrahLic
}

Date getDate(String val) {
    if (val != null) {
        if (val != "") {
            return new java.sql.Date(Date.parse(PATTERN_DATE_FORMAT, val).getTime())
        }
    }
}

Integer getInteger (String val) {
    if (val != null) {
        if (val != "") {
            return val.toInteger()
        }
    }
}