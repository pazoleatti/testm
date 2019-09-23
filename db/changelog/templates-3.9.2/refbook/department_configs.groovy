package refbook // department_configs_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.log.Logger
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.service.TransactionHelper
import com.aplana.sbrf.taxaccounting.service.TransactionLogic
import com.aplana.sbrf.taxaccounting.service.refbook.DepartmentConfigService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDepartmentService
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookOktmoService
import com.aplana.sbrf.taxaccounting.service.util.ExcelImportUtils
import groovy.transform.TypeChecked
import org.apache.commons.lang3.time.FastDateFormat
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

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
    TransactionHelper transactionHelper

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
        this.transactionHelper = (TransactionHelper) getSafeProperty("transactionHelper")
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
                           "Документ полномочий подписанта", "Код формы реорганизации", "КПП реорганизованной организации", "ИНН реорганизованной организации",
                           "КПП подразделения правопреемника", "Наименование подразделения правопреемника", "Учитывать в КПП/ОКТМО"]

    void importData() {
        this.department = refBookDepartmentService.fetch(departmentId)

        List<List<String>> header = []
        List<List<String>> values = []
        ExcelImportUtils.checkAndReadFile(inputStream, fileName, values, header, this.header.first(), this.header.last(), 1, null)
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
                List<DepartmentConfig> departmentConfigsBeforeSave = departmentConfigService.findAllByDepartmentId(department.id)
                executeInNewTransaction {
                    departmentConfigService.deleteByDepartmentId(department.id)
                }

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

    final int endDateColIndex = 1
    final int reorganizationColIndex = 13

    /**
     * Создает {@link DepartmentConfig} по строке excel файла и выполняет проверки
     * ошибки пишет в логгер отдельной строки
     */
    DepartmentConfigExt checkAndMakeDepartmentConfig(Row row) {
        int colIndex = 0
        DepartmentConfigExt departmentConfig = new DepartmentConfigExt(row)
        departmentConfig.setDepartment(department)
        departmentConfig.setStartDate(row.cell(colIndex).nonEmpty().toDate())
        departmentConfig.setEndDate(row.cell(++colIndex).toDate())
        departmentConfig.setKpp(row.cell(++colIndex).getKpp())
        departmentConfig.setOktmo(row.cell(++colIndex).getOktmo())
        departmentConfig.setTaxOrganCode(row.cell(++colIndex).getTaxOrganCode())
        departmentConfig.setPresentPlace(row.cell(++colIndex).getPresentPlace())
        departmentConfig.setName(row.cell(++colIndex).getName())
        departmentConfig.setPhone(row.cell(++colIndex).getPhone())
        departmentConfig.setSignatoryMark(row.cell(++colIndex).getSignatoryMark())
        departmentConfig.setSignatorySurName(row.cell(++colIndex).getSignatorySurName())
        departmentConfig.setSignatoryFirstName(row.cell(++colIndex).getSignatoryFirstName())
        departmentConfig.setSignatoryLastName(row.cell(++colIndex).getSignatoryLastName())
        departmentConfig.setApproveDocName(row.cell(++colIndex).getApproveDocName())
        departmentConfig.setReorganization(row.cell(colIndex = reorganizationColIndex).getReorganization())
        departmentConfig.setReorgKpp(row.cell(++colIndex).getReorgKpp())
        departmentConfig.setReorgInn(row.cell(++colIndex).getReorgInn())
        departmentConfig.setReorgSuccessorKpp(row.cell(++colIndex).getReorgSuccessorKpp())
        departmentConfig.setReorgSuccessorName(row.cell(++colIndex).getReorgSuccessorName())
        departmentConfig.setRelatedKppOktmo(row.cell(++colIndex).getRelatedKppOktmo())
        return departmentConfig
    }

    // Создаёт настройки подразделений поочередно, с проверками на возможность создания.
    // Те что не удалось создать система просто пропускает
    List<DepartmentConfigExt> save(List<DepartmentConfigExt> departmentConfigs) {
        List<DepartmentConfigExt> savedDepartmentConfigs = []
        for (def departmentConfig : departmentConfigs) {
            checkInterrupted()
            def localLogger = new Logger()
            try {
                executeInNewTransaction {
                    departmentConfigService.create(departmentConfig, localLogger)
                }
            } catch (Exception e) {
                departmentConfig.row.logger.warn("Строка " + departmentConfig.row.num + ". " + e.getMessage())
                continue
            }
            if (localLogger.containsLevel(LogLevel.ERROR)) {
                for (def logEntry : localLogger.getEntries()) {
                    departmentConfig.row.logger.warn("Строка " + departmentConfig.row.num + ". " + logEntry.getMessage())
                }
                continue
            }
            savedDepartmentConfigs.add(departmentConfig)
        }
        return savedDepartmentConfigs
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

    void executeInNewTransaction(Closure closure) {
        transactionHelper.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            Object execute() {
                closure.call()
            }
        })
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
                    Objects.equals(reorgInn, that.reorgInn) &&
                    Objects.equals(reorgSuccessorKpp, that.reorgSuccessorKpp) &&
                    Objects.equals(reorgSuccessorName, that.reorgSuccessorName)
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
                logNotEmptyError()
            }
            return this
        }

        void logNotEmptyError() {
            logError("Отсутствует значение для ячейки столбца \"${header[index]}\"")
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
            if (!value || !isKppValid(value)) {
                logError("\"КПП\" должен содержать 9 цифр, из которых 5 и 6 цифры должны содержать одно из значений: \"01\", \"02\", \"03\", \"05\", \"31\", \"32\", \"43\", \"45\"")
                return null
            }
            return value
        }

        private boolean isKppValid(String value) {
            value.length() == 9 && value[4..5] in ["01", "02", "03", "05", "31", "32", "43", "45"]
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
            if (!row.cell(reorganizationColIndex).value && !value) {
                logNotEmptyError()
                return null
            } else {
                if (value && value.length() > 1000) {
                    logError("\"Наименование для титульного листа\" должно содержать строку длиной не более 1000 символов")
                    return null
                }
                return value
            }
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

        String getReorgSuccessorKpp() {
            if (row.cell(reorganizationColIndex).value && !value) {
                logNotEmptyError()
                return null
            } else {
                if (value && (value.length() != 9 || !(value[4..5] in ["01", "02", "03", "05", "31", "32", "43", "45"]))) {
                    logError("\"КПП подразделения правопреемника\" должен содержать 9 цифр, из которых 5 и 6 цифры должны содержать одно из значений: \"01\", \"02\", \"03\", \"05\", \"31\", \"32\", \"43\", \"45\"")
                    return null
                }
                return value ?: null
            }
        }

        String getReorgSuccessorName() {
            if (row.cell(reorganizationColIndex).value && !value) {
                logNotEmptyError()
                return null
            } else {
                if (value && value.length() > 1000) {
                    logError("\"Наименование подразделения правопреемника\" должно содержать строку длиной не более 1000 символов")
                    return null
                }
                return value
            }
        }

        RelatedKppOktmo getRelatedKppOktmo() {
            RelatedKppOktmo relatedKppOktmo = null
            if (value) {
                if (!row.cell(endDateColIndex).value && value) {
                    logError("Если не заполнено значение \"Дата окончания действия\", " +
                            "то соответствующая ячейка столбца \"Учитывать в КПП/ОКТМО\" тоже не должна быть заполнена")
                } else {
                    if (value.length() == 18 || value.length() == 21) {
                        def kpp = value.substring(0, 9)
                        def oktmo = value.substring(10)
                        if (isKppOktmoValid(kpp, oktmo)) {
                            relatedKppOktmo = new RelatedKppOktmo()
                            relatedKppOktmo.setKpp(kpp)
                            relatedKppOktmo.setOktmo(oktmo)
                        }
                        DepartmentConfigsFilter filter = new DepartmentConfigsFilter()
                        filter.kpp = kpp
                        filter.oktmo = oktmo
                        filter.relevanceDate = new Date()
                        List<DepartmentConfig> existingDepartmentConfigList = departmentConfigService.findPageByFilter(filter, null)
                        if (!existingDepartmentConfigList.department.id.contains(departmentId)) {
                            logError("Значение в поле \"Учитывать в КПП/ОКТМО\" не принадлежит ТБ настройки подразделения или принадлежит не актуальной настройке подразделения")
                        }
                    } else {
                        logError("Сочетание КПП/ОКТМО должно содержать 18 или 21 символ")
                    }
                }
            }
            return relatedKppOktmo
        }

        private boolean isKppOktmoValid(String kpp, String oktmo) {
            boolean valid = true

            if (!kpp.isNumber()) {
                logError("В сочетании КПП/ОКТМО, КПП должно содержать только цифры")
                valid = false
            } else if (!isKppValid(kpp)) {
                logError("В сочетании КПП/ОКТМО, 5 и 6 цифры  КПП  должны содержать одно из значений: " +
                        "\"01\", \"02\", \"03\", \"05\", \"31\", \"32\", \"43\", \"45\"")
                valid = false
            }

            if (!oktmo.isNumber()) {
                logError("В сочетании КПП/ОКТМО, ОКТМО должно содержать только цифры")
                valid = false
            }

            return valid
        }

        void logIncorrectTypeError(def type) {
            logError("Тип данных ячейки столбца \"${header[index]}\" не соответствует ожидаемому \"$type\".")
        }

        void logError(String message) {
            row.logger.warn("Строка " + row.num + ". " + message)
        }
    }
}