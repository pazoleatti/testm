package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Абстрактный сервис загрузки ТФ НФ и справочников
 *
 * @author Dmitriy Levykin
 */
public abstract class AbstractLoadTransportDataService {

	private static final Log LOG = LogFactory.getLog(AbstractLoadTransportDataService.class);

    @Autowired
    protected AuditService auditService;

    // Константы
    final static String ZIP_ENCODING = "cp866";
    private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("(yyyy.MM.dd HH.mm.ss)");
        }
    };

    protected Integer formDepartmentId = null;
    protected Integer formTypeId = null;

    // Сообщения при загрузке из каталогов http://conf.aplana.com/pages/viewpage.action?pageId=12324125
    protected enum LogData {
        L0_INFO("%s", LogLevel.INFO, true, true),
        L0_ERROR("%s", LogLevel.ERROR, true, true),

        L1("Запущена процедура загрузки транспортных файлов, содержащих данные налоговых форм.", LogLevel.INFO, true, false),
        L2("Завершена процедура загрузки транспортных файлов, содержащих данные налоговых форм. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true, false),
        L3("В каталоге загрузки для подразделения «%s» не найдены файлы!", LogLevel.ERROR, true, false),
        L4("Имя файла «%s» в каталоге «%s» не соответствует требованиям к имени транспортного файла. Файл не будет обработан.", LogLevel.ERROR, true, false),
        L5("В справочнике «%s» отсутствует подразделение, поле «%s» которого равно «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true, false),
        L6("В Системе отсутствует налоговая форма с кодом «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true, false),
        L7("Для вида налога «%s» в Системе не создан период с кодом «%s»%s, календарный год «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true, false),
        L8("Для налоговой формы «%s» открыт корректирующий период «%s»", LogLevel.INFO, false, true),
        L9("Для налоговой формы «%s» закрыт (либо еще не открыт) отчетный период «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true, true),
        // L10("ТФ с таким именем уже есть в архиве, текущий ТФ переименован в «%s».", LogLevel.INFO, false, false),
        L11("Перенос «%s» в каталог архива успешно выполнен.", LogLevel.INFO, false, false),
        L12("Ошибка при архивировании транспортного файла! Загрузка файла не выполнена. %s.", LogLevel.ERROR, true, false),
        L13("Налоговая форма существует и имеет статус «" + WorkflowState.CREATED.getTitle() + "».", LogLevel.INFO, false, true),
        L14("Для подразделения «%s» не назначено первичной или выходной налоговой формы «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true, true),
        L15("ЭП файла «%s» принята, начата загрузка данных файла.", LogLevel.INFO, true, true),
        L15_FD("Из наименования транспортного файла получены следующие данные:", LogLevel.INFO, true, true),
        L15_RP("Код вида НФ: %s, код подразделения: %s, код периода: %s, год: %s", LogLevel.INFO, true, true),
        L15_M("Код вида НФ: %s, код подразделения: %s, код периода: %s, год: %s, месяц: %s", LogLevel.INFO, true, true),
        L15_1("ЭП файла «%s» проверять не требуется, начата загрузка данных файла.", LogLevel.INFO, true, true),
        L16("ЭП файла «%s» не принята или отсутствует! Загрузка файла не выполнена.", LogLevel.ERROR, true, true),
        L17("Налоговая форма существует и находится в состоянии, отличном от «" + WorkflowState.CREATED.getTitle() + "»! Загрузка файла не выполнена.", LogLevel.ERROR, true, true),
        L18("Создана «%s» налоговая форма «%s» для подразделения «%s» в периоде «%s».", LogLevel.INFO, true, true),
        L19("«%s» налоговая форма «%s» для подразделения «%s» в периоде «%s» заполнена данными загруженного файла.", LogLevel.INFO, true, true),
        L20("Закончена загрузка данных файла «%s».", LogLevel.INFO, true, true),
        L21("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена. %s.", LogLevel.ERROR, true, true),
        // L22("Итоговая сумма в графе «%s» строки %d в транспортном файле некорректна. Загрузка файла не выполнена.", LogLevel.ERROR, true, true),
        L23("Запущена процедура загрузки транспортных файлов, содержащих данные справочников.", LogLevel.INFO, true, false),
        L24("Завершена процедура загрузки транспортных файлов, содержащих данные справочников. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true, false),
        // L25("Не указан путь к корректному файлу ключей ЭП! Загрузка файла не выполнена.", LogLevel.ERROR, true, false),
        L26("Транспортный файл размещен в каталоге ошибок в составе архива «%s».", LogLevel.INFO, true, false),
        L27("Транспортный файл не записан в каталог ошибок! Загрузка файла не выполнена. %s.", LogLevel.ERROR, true, false),
        L28("Ошибка при удалении файла «%s» при перемещении в каталог ошибок! %s.", LogLevel.ERROR, true, false),
        L29("Ошибка при удалении файла «%s» при перемещении в каталог архива! %s.", LogLevel.ERROR, true, false),
        L30("К каталогу загрузки для подразделения «%s» не указан корректный путь!", LogLevel.ERROR, true, false),
        L31("В каталоге загрузки для %s не найдены файлы!", LogLevel.ERROR, true, false),
        L36("ЭП файла «%s» не принята. Код ошибки «%s».", LogLevel.ERROR, true, false),
        L37("Не указан путь к каталогу загрузки для %s!", LogLevel.ERROR, true, false),
        L38("Не задан алгоритм загрузки для %s!", LogLevel.ERROR, true, false),
        L40("Обработка данных транспортного файла не выполнена, т.к. в данный момент выполняется операция \"%s\", инициированная пользователем \"%s\" в \"%s\".", LogLevel.ERROR, true, false),
        L40_1("Удалена блокировка \"%s\", инициированная пользователем %s в %s.", LogLevel.INFO, true, false),
        L41("Файл %s пропущен, т.к. он уже обрабатывается системой.", LogLevel.ERROR, true, false),
        L42("Нет доступа %s ТБ «%s»! Загрузка не выполнена.", LogLevel.ERROR, true, false),
        L42_1("Нет доступа %s справочников! Загрузка не выполнена.", LogLevel.ERROR, true, false),
        L42_2("Нет доступа к каталогу загрузки «%s» %s! Загрузка не выполнена.", LogLevel.ERROR, true, false),
        L52("Начата загрузка файлов из каталога загрузки для подразделения \"%s\"", LogLevel.INFO, true, false),
        L53("Завершена загрузка файлов из каталога загрузки для подразделения \"%s\"", LogLevel.INFO, true, false),
        // Сообщения которых нет в постановке
        L_1("Не указан каталог ошибок в конфигурационных параметрах АС «Учет налогов»!", LogLevel.ERROR, true, false),
        L_2("Не указан каталог архива в конфигурационных параметрах АС «Учет налогов»!", LogLevel.ERROR, true, false),
        L43("Не указан путь %s справочников в конфигурационных параметрах АС «Учет налогов»! Загрузка не выполнена.", LogLevel.ERROR, true, false),
        L47("Размер файла \"%s\" %s Кбайт (каталог загрузки \"%s\") превышает максимально допустимый (%s Кбайт)! Загрузка не выполнена.", LogLevel.ERROR, true, false),
        L48("Для налоговой формы загружаемого файла \"%s\" не предусмотрена обработка транспортного файла! Загрузка не выполнена.", LogLevel.ERROR, true, false);

        private LogLevel level;
        private String text;
        private boolean logSystem;
        private boolean isFormDepartmentId;

        LogData(String text, LogLevel level, boolean logSystem, boolean isFormDepartmentId) {
            this.text = text;
            this.level = level;
            this.logSystem = logSystem;
            this.isFormDepartmentId = isFormDepartmentId;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getText() {
            return text;
        }

        public boolean isLogSystem() {
            return logSystem;
        }

        public boolean isFormDepartmentId() {
            return isFormDepartmentId;
        }
    }

    /**
     * Логгирование в области уведомлений и ЖА при импорте из ТФ
     */
    protected final void log(TAUserInfo userInfo, LogData logData, Logger logger, String lockId, Object... args) {
        // Область уведомлений
        switch (logData.getLevel()) {
            case INFO:
                logger.info(logData.getText(), args);
                break;
            case ERROR:
                logger.error(logData.getText(), args);
                break;
        }
        // ЖА
        // TODO Указать признак ошибки в ЖА. См. logData.getLevel()
        if (logData.isLogSystem()) {
            Integer departmentId = null;
            if (logData.isFormDepartmentId()) {
                departmentId = formDepartmentId;
            }
            if (departmentId == null && userInfo != null) {
                departmentId = userInfo.getUser().getDepartmentId();
            }
            String prefix = "";
            if (userInfo.getUser().getId() == TAUser.SYSTEM_USER_ID) {
                prefix = "Событие инициировано Системой. ";
            }
            String lockInfo = String.format("Номер загрузки: %s. ", lockId);
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, departmentId, null,
                    null, null, null, prefix + lockInfo + String.format(logData.getText(), args), null);
        }
    }

    /**
     * Создание или обновление каталога с требуемой структурой:
     * Каталог/Текущий_год/Текущий_месяц/Текущий_день_месяца/
     */
    private FileWrapper createPath(String rootPath, Calendar calendar) {
        FileWrapper errorFolderDst = ResourceUtils.getSharedResource(rootPath + "/" + calendar.get(Calendar.YEAR)
                + "/" + Months.fromId(calendar.get(Calendar.MONTH) + 1).getTitle()
                + "/" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "/", false);
        errorFolderDst.mkDirs();
        return errorFolderDst;
    }

    /**
     * Перенос ТФ в каталог ошибок
     *
     * @param errorPath    Путь к каталогу ошибок
     * @param errorFileSrc Файл с ошибкой, который должен быть перенесен
     */
    protected final boolean moveToErrorDirectory(TAUserInfo userInfo, String errorPath, FileWrapper errorFileSrc,
                                                 List<LogEntry> errorList, Logger logger, String lock) {
        return moveToDirectory(userInfo, errorPath, errorFileSrc, errorList, logger, LogData.L28, LogData.L26, LogData.L27, lock);
    }

    /**
     * Перенос ТФ в каталог архива
     *
     * @param archivePath    Путь к каталогу архива
     * @param archiveFileSrc Файл, который должен быть перенесен
     */
    protected final boolean moveToArchiveDirectory(TAUserInfo userInfo, String archivePath, FileWrapper archiveFileSrc,
                                                   Logger logger, String lock) {
        return moveToDirectory(userInfo, archivePath, archiveFileSrc, null, logger, LogData.L29, LogData.L11, LogData.L12, lock);
    }

    /**
     * Перенос ТФ в каталог ошибок или архива
     */
    private boolean moveToDirectory(TAUserInfo userInfo, String rootPath, FileWrapper file, List<LogEntry> errorList,
                                    Logger logger, LogData deleteErrorLogData, LogData successLogData, LogData moveErrorLogData, String lock) {
        boolean success = true;
        try {
            // Создание дерева каталогов
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            FileWrapper folderDst = createPath(rootPath, calendar);

            // Создание архива
            String path = folderDst.getPath() + "/" + file.getName()
                    + dateFormat.get().format(calendar.getTime()) + ".zip";
            FileWrapper fileDst = ResourceUtils.getSharedResource(path, false);
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(fileDst.getOutputStream());
            zaos.setEncoding(ZIP_ENCODING);
            zaos.putArchiveEntry(new ZipArchiveEntry(file.getName()));
            InputStream inputStream = file.getInputStream();
			try {
            	IOUtils.copy(inputStream, zaos);
            	zaos.closeArchiveEntry();
			} finally {
            	IOUtils.closeQuietly(inputStream);
			}

            // Файл с логами, если логи есть
            if (errorList != null && !errorList.isEmpty()) {
                zaos.putArchiveEntry(new ZipArchiveEntry(file.getName() + ".txt"));
                StringBuilder sb = new StringBuilder();
                sb.append(userInfo.getUser().getName() + " \r\n");
                for (LogEntry logEntry : errorList) {
                    sb.append(logEntry.getLevel().name() + "\t" + logEntry.getMessage() + "\r\n");
                }
                IOUtils.copy(new ByteArrayInputStream(sb.toString().getBytes()), zaos);
                zaos.closeArchiveEntry();
            }
            IOUtils.closeQuietly(zaos);

            // Удаление
            try {
                file.delete();
            } catch (Exception e) {
                success = false;
                log(userInfo, deleteErrorLogData, logger, lock, file.getPath(), e.getMessage());
				LOG.error(e.getMessage(), e);
            }

            log(userInfo, successLogData, logger, lock, fileDst.getName());
        } catch (Exception e) {
            success = false;
            log(userInfo, moveErrorLogData, logger, lock, e.getMessage());
			LOG.error(e.getMessage(), e);
        }
        return success;
    }
}
