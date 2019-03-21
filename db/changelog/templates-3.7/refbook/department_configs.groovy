package refbook// department_configs_ref комментарий для локального поиска скрипта

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
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookOktmoService
import groovy.transform.TypeChecked
import org.apache.commons.lang3.time.FastDateFormat
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id.NDFL_DETAIL
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkAndReadFile
import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.checkInterrupted

/**
 * Cкрипт Настроек подразделений
 */

(new DepartmentConfigScript(this)).run()

@SuppressWarnings("GrMethodMayBeStatic")
@TypeChecked
class DepartmentConfigScript extends AbstractScriptClass {

    RefBookDepartmentService refBookDepartmentService
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
        this.refBookDepartmentService = (RefBookDepartmentService) getSafeProperty("refBookDepartmentService")
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
        this.department = refBookDepartmentService.fetch(departmentId)

        List<List<String>> header = []
        List<List<String>> values = []
        checkAndReadFile(inputStream, fileName, values, header, this.header.first(), this.header.last(), 1, null)
        checkHeader(header)
        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            return
        }
        if (!values || new Row(values.first()).isEmpty()) {
            logger.error("Ошибка при загрузке файла \"$fileName\". Отсутствуют данные для загрузки.")
            return
        }

        int rowIndex = 1
        int rowsCheckLogIndex = logger.entries.size()
        List<DepartmentConfigExt> departmentConfigs = []
        List<Row> rows = []
        try {
            for (def iterator = values.iterator(); iterator.hasNext(); rowIndex++) {
                checkInterrupted()
                Row row = new Row(rowIndex, iterator.next())
                if (row.isEmpty()) {
                    break
                }
                rows.add(row)
                def departmentConfig = checkAndMakeDepartmentConfig(row)
                if (row.logger.containsLevel(LogLevel.ERROR) || row.logger.containsLevel(LogLevel.WARNING)) {
                    continue// строки с ошибками пропускаем
                }
                departmentConfigs.add(departmentConfig)
            }
            if (logger.containsLevel(LogLevel.ERROR)) {
                logger.error("Загрузка файла \"$fileName\" не может быть выполнена")
            } else if (departmentConfigs.isEmpty()) {
                logger.warn("Не удалось загрузить ни одной строки")
            } else {
                List<DepartmentConfig> departmentConfigsBeforeSave = departmentConfigService.fetchAllByDepartmentId(department.id)
                departmentConfigService.delete(departmentConfigsBeforeSave, logger)

                List<DepartmentConfigExt> savedDepartmentConfigs = save(departmentConfigs)

                logSaveResult(savedDepartmentConfigs, departmentConfigsBeforeSave)
            }
        } finally {
            // все ошибки по строкам логируем в одну кучу и по порядку
            for (Row row : rows) {
                logger.entries.addAll(rowsCheckLogIndex, row.logger.getEntries())
                rowsCheckLogIndex += row.logger.getEntries().size()
            }
        }
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

    /**
     * Создает {@link DepartmentConfig} по строке excel файла и выполняет проверки
     * ошибки пишет в логгер отдельной строки
     */
    DepartmentConfigExt checkAndMakeDepartmentConfig(Row row) {
        int colNum = 0
        DepartmentConfigExt departmentConfig = new DepartmentConfigExt(row)
        departmentConfig.setDepartment(department)
        departmentConfig.setStartDate(row.cell(colNum).nonEmpty().toDate())
        departmentConfig.setEndDate(row.cell(++colNum).toDate())
        departmentConfig.setKpp(row.cell(++colNum).getKpp())
        departmentConfig.setOktmo(row.cell(++colNum).getOktmo())
        departmentConfig.setTaxOrganCode(row.cell(++colNum).getTaxOrganCode())
        departmentConfig.setPresentPlace(row.cell(++colNum).getPresentPlace())
        departmentConfig.setName(row.cell(++colNum).getName())
        departmentConfig.setPhone(row.cell(++colNum).getPhone())
        departmentConfig.setSignatoryMark(row.cell(++colNum).getSignatoryMark())
        departmentConfig.setSignatorySurName(row.cell(++colNum).getSignatorySurName())
        departmentConfig.setSignatoryFirstName(row.cell(++colNum).getSignatoryFirstName())
        departmentConfig.setSignatoryLastName(row.cell(++colNum).getSignatoryLastName())
        departmentConfig.setApproveDocName(row.cell(++colNum).getApproveDocName())
        departmentConfig.setReorganization(row.cell(++colNum).getReorganization())
        departmentConfig.setReorgKpp(row.cell(++colNum).getReorgKpp())
        departmentConfig.setReorgInn(row.cell(++colNum).getReorgInn())
        return departmentConfig
    }

    // Сохраняет настройки подразделений с проверками
    List<DepartmentConfigExt> save(List<DepartmentConfigExt> departmentConfigs) {
        List<DepartmentConfigExt> savedDepartmentConfigs = []
        for (def departmentConfig : departmentConfigs) {
            checkInterrupted()

            List<DepartmentConfig> relatedDepartmentConfigs = departmentConfigService.fetchAllByKppAndOktmo(departmentConfig.kpp, departmentConfig.oktmo.code)
            try {
                departmentConfigService.checkDepartmentConfig(departmentConfig, relatedDepartmentConfigs)
            } catch (ServiceException e) {
                // пропускаем запись и переходим на следующую
                departmentConfig.row.logger.warn("Строка " + departmentConfig.row.num + ". " + e.getMessage())
                continue
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
        return savedDepartmentConfigs
    }

    // Сохраняет запись настройки подразделений в бд
    // через batch не получается из-за дат актуальности
    DepartmentConfigExt insertRecord(DepartmentConfigExt departmentConfig) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(NDFL_DETAIL.getId())
        Logger localLogger = new Logger()
        RefBookRecord record = departmentConfigService.convertToRefBookRecord(departmentConfig)
        try {
            def ids = provider.createRecordVersionWithoutLock(localLogger, departmentConfig.getStartDate(), departmentConfig.getEndDate(), [record])
            departmentConfig.setId(ids[0])
        } catch (Exception e) {
            departmentConfig.row.logger.warn("Строка " + departmentConfig.row.num + ". " + e.getMessage())
            return null
        }
        if (localLogger.containsLevel(LogLevel.ERROR)) {
            for (def logEntry : localLogger.getEntries()) {
                departmentConfig.row.logger.warn("Строка " + departmentConfig.row.num + ". " + logEntry.getMessage())
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
                    def record = recordData.entrySet().first().getValue()
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
                    def record = recordData.entrySet().first().getValue()
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
                    def record = recordData.entrySet().first().getValue()
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
        Logger logger = new Logger()

        Row(int index = 0, List<String> values) {
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
            this.value = value ? value.trim() : null
            this.row = row
        }

        int getNum() {
            return index + 1
        }

        Cell nonEmpty() {
            if (!value) {
                logError("Отсутствует значение для ячейки столбца \"${header[index]}\"")
            }
            return this
        }

        Date toDate() {
            if (value != null && !value.isEmpty()) {
                try {
                    return LocalDate.parse(value, DateTimeFormat.forPattern(SharedConstants.DATE_FORMAT)).toDate()
                } catch (Exception ignored) {
                    logIncorrectTypeError("Дата")
                }
            }
            return null
        }

        String getKpp() {
            if (!value || value.length() != 9 || !(value[4..5] in ["01", "02", "03", "05", "31", "32", "43", "45"])) {
                logError("\"КПП\" должен содержать 9 цифр, из которых 5 и 6 цифры должны содержать одно из значений: \"01\", \"02\", \"03\", \"05\", \"31\", \"32\", \"43\", \"45\"")
                return null
            }
            return value
        }

        RefBookOktmo getOktmo() {
            RefBookOktmo oktmo = null
            if (!value || !(value.length() in [8, 11]) || !value.isNumber()) {
                logError("\"ОКТМО\" должен содержать либо 8, либо 11 цифр")
            } else {
                oktmo = getOktmoByCode(value)
                if (oktmo == null) {
                    logError("Не найдена запись в справочнике \"ОКТМО\" по коду " + value)
                }
            }
            return oktmo
        }

        String getTaxOrganCode() {
            if (!value || value.length() != 4 || !value.isNumber()) {
                logError("\"\"Код НО (конечного)\" должен содержать 4 цифры.")
                return null
            }
            return value
        }

        RefBookPresentPlace getPresentPlace() {
            RefBookPresentPlace presentPlace
            if (!value || value.length() != 3 || !value.isNumber()) {
                logError("\"Код по месту представления\" должен содержать 3 цифры")
                return null
            } else {
                presentPlace = getPresentPlaceByCode(value)
                if (presentPlace == null) {
                    logError("Не найдена запись в справочнике \"Коды места представления расчета\" по коду " + value)
                }
            }
            return presentPlace
        }

        String getName() {
            if (value && value.length() > 1000) {
                logError("\"Наименование для титульного листа\" должно содержать строку длиной не более 1000 символов")
                return null
            }
            return value
        }

        String getPhone() {
            if (value && value.length() > 20) {
                logError("\"Контактный телефон\" должен содержать строку длиной не более 20 символов")
                return null
            }
            return value
        }

        RefBookSignatoryMark getSignatoryMark() {
            RefBookSignatoryMark signatoryMark
            if (!value || value.length() != 1 || !value.isNumber()) {
                logError("\"Признак подписанта\" должен содержать число от 0 до 9")
                return null
            } else {
                signatoryMark = getSignatoryMarkByCode(Integer.valueOf(value))
                if (signatoryMark == null) {
                    logError("Не найдена запись в справочнике \"Признак лица, подписавшего документ\" по коду " + value)
                }
            }
            return signatoryMark
        }

        String getSignatorySurName() {
            if (value && value.length() > 60) {
                logError("\"Фамилия подписанта\" должна быть строкой длиной не более 60 символов")
                return null
            }
            return value ?: null
        }

        String getSignatoryFirstName() {
            if (value && value.length() > 60) {
                logError("\"Имя подписанта\" должно быть строкой длиной не более 60 символов")
                return null
            }
            return value ?: null
        }

        String getSignatoryLastName() {
            if (value && value.length() > 60) {
                logError("\"Отчество подписанта\" должно быть строкой длиной не более 60 символов")
                return null
            }
            return value ?: null
        }

        String getApproveDocName() {
            if (value && value.length() > 120) {
                logError("\"Документ полномочий подписанта\" должен быть строкой длиной не более 120 символов")
                return null
            }
            return value ?: null
        }

        RefBookReorganization getReorganization() {
            RefBookReorganization reorganization = null
            if (value) {
                if (value.length() != 1 || !value.isNumber()) {
                    logError("\"Код формы реорганизации\" должен содержать число от 0 до 9")
                } else {
                    reorganization = getReorganizationByCode(value)
                    if (reorganization == null) {
                        logError("Не найдена запись в справочнике \"Коды форм реорганизации (ликвидации) организации\" по коду " + value)
                    }
                }
            }
            return reorganization
        }

        String getReorgKpp() {
            if (value && (value.length() != 9 || !(value[4..5] in ["01", "02", "03", "05", "31", "32", "43", "45"]))) {
                logError("\"КПП реорганизованной организации\" должен содержать 9 цифр, из которых 5 и 6 цифры должны содержать одно из значений: \"01\", \"02\", \"03\", \"05\", \"31\", \"32\", \"43\", \"45\"")
                return null
            }
            return value ?: null
        }

        String getReorgInn() {
            if (value && value.length() != 10) {
                logError("\"ИНН реорганизованной организации\" должен содержать 10 цифр")
                return null
            }
            return value ?: null
        }

        void logIncorrectTypeError(def type) {
            logError("Тип данных ячейки столбца \"${header[index]}\" не соответствует ожидаемому \"$type\".")
        }

        void logError(String message) {
            row.logger.warn("Строка " + row.num + ". " + message)
        }
    }
}