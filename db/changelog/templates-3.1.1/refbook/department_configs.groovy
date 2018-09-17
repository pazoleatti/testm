package refbook

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentDataService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookOktmoService
import groovy.transform.TypeChecked
import org.apache.commons.lang3.time.FastDateFormat

import java.text.SimpleDateFormat

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.NDFL_DETAIL
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkAndReadFile
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkInterrupted

// department_configs_ref комментарий для локального поиска скрипта
/**
 * Cкрипт Настроек подразделений
 */

(new DepartmentConfigScript(this)).run()

@SuppressWarnings("GrMethodMayBeStatic")
@TypeChecked
class DepartmentConfigScript extends AbstractScriptClass {

    RefBookDepartmentDataService refBookDepartmentDataService
    RefBookOktmoService refBookOktmoService
    RefBookFactory refBookFactory
    DepartmentConfigService departmentConfigService

    Integer departmentId
    RefBookDepartment department
    InputStream inputStream
    String fileName

    FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yyyy")

    DepartmentConfigScript(groovy.lang.Script script) {
        super(script)
        this.refBookDepartmentDataService = (RefBookDepartmentDataService) getSafeProperty("refBookDepartmentDataService")
        this.refBookOktmoService = (RefBookOktmoService) getSafeProperty("refBookOktmoService")
        this.refBookFactory = (RefBookFactory) getSafeProperty("refBookFactory")
        this.departmentConfigService = (DepartmentConfigService) getSafeProperty("departmentConfigService")
        this.departmentId = (Integer) getSafeProperty("departmentId")
        this.inputStream = (InputStream) getSafeProperty("inputStream")
        this.fileName = (String) getSafeProperty("fileName")
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.IMPORT:
                importData()
                break
        }
    }

    List<String> header = ["Дата начала действия настройки", "Дата окончания действия настройки", "КПП", "ОКТМО", "Код НО (конечного)", "Код по месту представления",
                           "Наименование для титульного листа", "Контактный телефон", "Признак подписанта", "Фамилия подписанта", "Имя подписанта", "Отчество подписанта",
                           "Документ полномочий подписанта", "Код формы реорганизации", "КПП реорганизованной организации", "ИНН реорганизованной организации"]

    void importData() {
        this.department = refBookDepartmentDataService.fetch(departmentId)

        List<List<String>> header = []
        List<List<String>> rows = []
        checkAndReadFile(inputStream, fileName, rows, header, this.header.first(), this.header.last(), 1, null)
        checkHeader(header)
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }
        if (!rows) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки.")
            return
        }

        int rowIndex = 0
        List<DepartmentConfigExt> departmentConfigs = []
        for (def iterator = rows.iterator(); iterator.hasNext(); rowIndex++) {
            checkInterrupted()

            def row = new Row(rowIndex, iterator.next())
            iterator.remove()
            if (row.isEmpty()) {// все строки пустые - выход
                if (rowIndex == 0) {
                    logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки.")
                    return
                }
                break
            }
            def departmentConfig = makeDepartmentConfig(row)
            departmentConfigs.add(departmentConfig)
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }
        save(departmentConfigs)
    }

    void checkHeader(List<List<String>> headerActual) {
        if (!headerActual || !headerActual[0]) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Не удалось распознать заголовок таблицы.")
            return
        }
        for (int i = 0; i < header.size(); i++) {
            if (i >= headerActual[0].size() || header[i] != headerActual[0][i]) {
                logger.error("Ошибка при загрузке файла \"$fileName\". Заголовок таблицы не соответствует требуемой структуре.")
                logger.error("Столбец заголовка таблицы \"${i >= headerActual[0].size() ? "Не задан или отсутствует" : headerActual[0][i]}\" № ${i} " +
                        "не соответствует ожидаемому \"${header[i]}\" № ${i}")
                break
            }
        }
    }

    DepartmentConfigExt makeDepartmentConfig(Row row) {
        int colNum = 0
        DepartmentConfigExt departmentConfig = new DepartmentConfigExt(row)
        departmentConfig.setDepartment(department)
        departmentConfig.setStartDate(row.cell(colNum).toDate())
        departmentConfig.setEndDate(row.cell(++colNum).toDate())
        departmentConfig.setKpp(row.cell(++colNum).toString())
        departmentConfig.setOktmo(getOktmoByCode(row.cell(++colNum).toString()))
        departmentConfig.setTaxOrganCode(row.cell(++colNum).toString())
        departmentConfig.setPresentPlace(getPresentPlaceByCode(row.cell(++colNum).toString()))
        departmentConfig.setName(row.cell(++colNum).toString())
        departmentConfig.setPhone(row.cell(++colNum).toString())
        departmentConfig.setSignatoryMark(getSignatoryMarkByCode(row.cell(++colNum).toInteger()))
        departmentConfig.setSignatorySurName(row.cell(++colNum).toString())
        departmentConfig.setSignatoryFirstName(row.cell(++colNum).toString())
        departmentConfig.setSignatoryLastName(row.cell(++colNum).toString())
        departmentConfig.setApproveDocName(row.cell(++colNum).toString())
        departmentConfig.setReorganization(getReorganizationByCode(row.cell(++colNum).toString()))
        departmentConfig.setReorgKpp(row.cell(++colNum).toString())
        departmentConfig.setReorgInn(row.cell(++colNum).toString())
        return departmentConfig
    }

    void save(List<DepartmentConfigExt> departmentConfigs) {
        List<DepartmentConfig> departmentConfigsBeforeSave = departmentConfigService.fetchAllByDepartmentId(department.id)
        departmentConfigService.delete(departmentConfigsBeforeSave, logger)

        List<DepartmentConfigExt> savedDepartmentConfigs = []
        for (def departmentConfig : departmentConfigs) {
            List<DepartmentConfig> relatedDepartmentConfigs = departmentConfigService.fetchAllByKppAndOktmo(departmentConfig.kpp, departmentConfig.oktmo.code)
            try {
                departmentConfigService.checkDepartmentConfig(departmentConfig, relatedDepartmentConfigs)
            } catch (ServiceException e) {
                // пропускаем запись и переходим на следующую
                logger.warn("Строка " + departmentConfig.row.num + " не сохранена: " + e.getMessage())
                break
            }
            if (!relatedDepartmentConfigs.isEmpty()) {
                // если записи с такой парой КПП/ОКТМО уже есть, то recordId должен быть тот же что у них
                departmentConfig.setRecordId(relatedDepartmentConfigs.get(0).getRecordId())
            }
            def insertedDepartmentConfig = insertRecord(departmentConfig)
            if (insertedDepartmentConfig != null) {
                savedDepartmentConfigs.add(insertedDepartmentConfig)
            }
        }

        logSaveResult(savedDepartmentConfigs, departmentConfigsBeforeSave)
    }

    // Создаёт запись настройки подразделений в бд
    // через batch не получается из-за дат актуальности
    DepartmentConfigExt insertRecord(DepartmentConfigExt departmentConfig) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId())
        Logger localLogger = new Logger()
        RefBookRecord record = departmentConfigService.convertToRefBookRecord(departmentConfig)
        try {
            def ids = provider.createRecordVersionWithoutLock(localLogger, departmentConfig.getStartDate(), departmentConfig.getEndDate(), [record])
            departmentConfig.setId(ids[0])
        } catch (Exception e) {
            logger.warn("Строка " + departmentConfig.row.num + " не сохранена: " + e.getMessage())
            return null
        }
        if (localLogger.containsLevel(LogLevel.ERROR)) {
            for (def logEntry : localLogger.getEntries()) {
                logger.warn("Строка " + departmentConfig.row.num + " не сохранена: " + logEntry.getMessage())
            }
            return null
        }
        return departmentConfig
    }

    void logSaveResult(List<DepartmentConfigExt> savedDepartmentConfigs, List<DepartmentConfig> departmentConfigsBeforeSave) {
        def iterator = savedDepartmentConfigs.iterator()
        while (iterator.hasNext()) {
            DepartmentConfigExt savedDepartmentConfig = iterator.next()
            if (departmentConfigsBeforeSave.contains(savedDepartmentConfig)) {
                iterator.remove()
            }
            departmentConfigsBeforeSave.remove(savedDepartmentConfig)
        }
        List<DepartmentConfigExt> createdDepartmentConfigs = savedDepartmentConfigs
        List<DepartmentConfig> deletedDepartmentConfigs = departmentConfigsBeforeSave

        for (def createdDepartmentConfig : createdDepartmentConfigs) {
            logger.info("Создана новая настройка подразделения \"${department.shortName}\" с КПП: \"${createdDepartmentConfig.kpp}\", " +
                    "ОКТМО: \"${createdDepartmentConfig.oktmo.code}\", период действия " +
                    "с ${dateFormat.format(createdDepartmentConfig.startDate)} по " +
                    "${createdDepartmentConfig.endDate ? dateFormat.format(createdDepartmentConfig.endDate) : "__"}.")
        }
        for (def deletedDepartmentConfig : deletedDepartmentConfigs) {
            logger.info("Удалена настройка подразделения \"${department.shortName}\" с КПП: \"${deletedDepartmentConfig.kpp}\", " +
                    "ОКТМО: \"${deletedDepartmentConfig.oktmo.code}\", период действия " +
                    "с ${dateFormat.format(deletedDepartmentConfig.startDate)} по " +
                    "${deletedDepartmentConfig.endDate ? dateFormat.format(deletedDepartmentConfig.endDate) : "__"}.")
        }
    }

    /**************************************
     ******* Работа со справочниками ******
     **************************************/

    RefBookOktmo getOktmoByCode(String code) {
        return refBookOktmoService.fetchByCode(code, new Date())
    }

    Map<String, RefBookPresentPlace> presentPlaceByCode = [:]

    RefBookPresentPlace getPresentPlaceByCode(String code) {
        RefBookPresentPlace presentPlace = null
        if (code) {
            presentPlace = presentPlaceByCode.get(code)
            if (!presentPlace) {
                def recordData = refBookFactory.getDataProvider(RefBook.Id.PRESENT_PLACE.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    presentPlace = new RefBookPresentPlace()
                    presentPlace.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()?.longValue())
                    presentPlace.setCode(record.get("CODE").getStringValue())
                    presentPlaceByCode.put(code, presentPlace)
                }
            }
        }
        return presentPlace
    }

    Map<Integer, RefBookSignatoryMark> signatoryMarkByCode = [:]

    RefBookSignatoryMark getSignatoryMarkByCode(Integer code) {
        RefBookSignatoryMark signatoryMark = null
        if (code != null) {
            signatoryMark = signatoryMarkByCode.get(code)
            if (!signatoryMark) {
                def recordData = refBookFactory.getDataProvider(RefBook.Id.MARK_SIGNATORY_CODE.getId()).getRecordDataVersionWhere(" where code = ${code}", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    signatoryMark = new RefBookSignatoryMark()
                    signatoryMark.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()?.longValue())
                    signatoryMark.setCode(record.get("CODE").getNumberValue().intValue())
                    signatoryMarkByCode.put(code, signatoryMark)
                }
            }
        }
        return signatoryMark
    }

    Map<String, RefBookReorganization> reorganizationByCode = [:]

    RefBookReorganization getReorganizationByCode(String code) {
        RefBookReorganization reorganization = null
        if (code) {
            reorganization = reorganizationByCode.get(code)
            if (!reorganization) {
                def recordData = refBookFactory.getDataProvider(RefBook.Id.REORGANIZATION.getId()).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == recordData.entrySet().size()) {
                    def record = recordData.entrySet().iterator().next().getValue()
                    reorganization = new RefBookReorganization()
                    reorganization.setId(record.get(RefBook.RECORD_ID_ALIAS).getNumberValue()?.longValue())
                    reorganization.setCode(record.get("CODE").getStringValue())
                    reorganizationByCode.put(code, reorganization)
                }
            }
        }
        return reorganization
    }

    /*******************************
     * Утилитные классы
     *******************/

    // Настройка подразделений с ссылкой на строку из excel файда и дополнительной инфой
    class DepartmentConfigExt extends DepartmentConfig {
        Row row

        DepartmentConfigExt(Row row) {
            this.row = row
        }

        @Override
        boolean equals(Object obj) {
            if (this.is(obj)) return true
            DepartmentConfig that = (DepartmentConfig) obj
            return Objects.equals(startDate, that.startDate) &&
                    Objects.equals(endDate, that.endDate) &&
                    Objects.equals(department.id, that.department.id) &&
                    Objects.equals(kpp, that.kpp) &&
                    Objects.equals(oktmo.id, that.oktmo.id) &&
                    Objects.equals(taxOrganCode, that.taxOrganCode) &&
                    Objects.equals(presentPlace?.id, that.presentPlace?.id) &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(phone, that.phone) &&
                    Objects.equals(signatoryMark?.id, that.signatoryMark?.id) &&
                    Objects.equals(signatorySurName, that.signatorySurName) &&
                    Objects.equals(signatoryFirstName, that.signatoryFirstName) &&
                    Objects.equals(signatoryLastName, that.signatoryLastName) &&
                    Objects.equals(approveDocName, that.approveDocName) &&
                    Objects.equals(reorganization?.id, that.reorganization?.id) &&
                    Objects.equals(reorgKpp, that.reorgKpp) &&
                    Objects.equals(reorgInn, that.reorgInn)
        }
    }

    // Считанная строка из excel файла
    class Row {
        // индекс строки начиная с 0
        int index
        List<String> values

        Row(int index, List<String> values) {
            this.index = index
            this.values = values
        }

        Cell cell(int index) {
            return new Cell(index, values.get(index), this)
        }

        int getNum() {
            return index + 1
        }

        boolean isEmpty(Range<Integer> range = 1..values.size()) {
            if (values) {
                for (int index : range) {
                    if (values[index - 1]) {
                        return false
                    }
                }
            }
            return true
        }
    }

    // Считанная ячейка из excel файла
    class Cell {
        // индекс строки начиная с 0
        int index
        String value
        Row row

        Cell(int index, String value, Row row) {
            this.index = index
            this.value = value
            this.row = row
        }

        int getNum() {
            return index + 1
        }

        Date toDate() {
            if (value != null && !value.isEmpty()) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat(SharedConstants.DATE_FORMAT)
                    formatter.setLenient(false)
                    return formatter.parse(value)
                } catch (Exception ignored) {
                    logIncorrectTypeError("Дата")
                }
            }
            return null
        }

        private Integer toInteger(Integer precision = null) {
            return toBigDecimal(precision)?.intValue()
        }

        Long toLong(Integer precision = null) {
            return toBigDecimal(precision)?.longValue()
        }

        BigDecimal toBigDecimal(Integer precision = null, Integer scale = null) {
            assert (precision == null || precision > 0) && (scale == null || scale > 0)
            BigDecimal result = null
            if (value) {
                try {
                    def bigDecimal = new BigDecimal(value)
                    if (precision != null && bigDecimal.precision() > precision || scale != null && bigDecimal.scale() > scale) {
                        logIncorrectTypeError("${!scale ? "Целое число" : "Число"}${!precision ? "" : "/${precision}${!scale ? "" : ".${scale}/"}"}")
                    }
                    result = bigDecimal
                } catch (NumberFormatException ignored) {
                    logIncorrectTypeError("${!scale ? "Целое число" : "Число"}${!precision ? "" : "/${precision}${!scale ? "" : ".${scale}/"}"}")
                }
            }
            return result
        }

        String toString(Integer maxLength = null, Integer minLength = null) {
            if (value && (maxLength && value.length() > maxLength || minLength && value.length() < minLength)) {
                logIncorrectTypeError("Строка/${maxLength}/")
                return null
            }
            return value ?: null
        }

        void logIncorrectTypeError(def type) {
            logger.error("Ошибка при определении значения ячейки файла \"$fileName\". Тип данных ячейки столбца \"${header[index]}\" № " + index +
                    " строки ${row.index + 1} не соответствует ожидаемому \"$type\".")
        }
    }
}