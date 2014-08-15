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

    @Autowired
    private AuditService auditService;

    // Константы
    final static String ZIP_ENCODING = "cp866";
    final static SimpleDateFormat dateFormat = new SimpleDateFormat("(yyyy.MM.dd HH.mm.ss)");

    // Сообщения при загрузке из каталогов http://conf.aplana.com/pages/viewpage.action?pageId=12324125
    protected static enum LogData {
        L1("Запущена процедура загрузки транспортных файлов, содержащих данные налоговых форм.", LogLevel.INFO, true),
        L2("Завершена процедура загрузки транспортных файлов, содержащих данные налоговых форм. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true),
        L3("В каталоге загрузки для подразделения «%s» не найдены файлы!", LogLevel.ERROR, true),
        L4("Имя или формат файла «%s» не соответствует требованиям к транспортному файлу! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L5("Указанный в имени файла «%s» код подразделения не существует в Системе! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L6("Указанный в имени файла «%s» код налоговой формы не существует в Системе! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L7("Указанный в имени файла «%s» код отчетного периода не существует в Системе! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        // L8("Для налоговой формы «%s» открыт корректирующий период «%s»", LogLevel.INFO, false),
        L9("Для налоговой формы «%s» закрыт (либо еще не открыт) отчетный период «%s»! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        // L10("ТФ с таким именем уже есть в архиве, текущий ТФ переименован в «%s».", LogLevel.INFO, false),
        L11("Перенос «%s» в каталог архива успешно выполнен.", LogLevel.INFO, false),
        L12("Ошибка при архивировании транспортного файла! Загрузка файла не выполнена. %s.", LogLevel.ERROR, true),
        L13("Налоговая форма существует и имеет статус «" + WorkflowState.CREATED.getName() + "».", LogLevel.INFO, false),
        L14("Назначение налоговой формы «%s» подразделению «%s» не выполнено! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L15("ЭЦП файла «%s» принята, начата загрузка данных файла.", LogLevel.INFO, true),
        L15_1("ЭЦП файла «%s» проверять не требуется, начата загрузка данных файла.", LogLevel.INFO, true),
        L16("ЭЦП файла «%s» не принята или отсутствует! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L17("Налоговая форма существует и находится в состоянии, отличном от «" + WorkflowState.CREATED.getName() + "»! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L18("Создана новая налоговая форма «%s» для подразделения «%s» в периоде «%s».", LogLevel.INFO, true),
        L19("Первичная налоговая форма «%s» для подразделения «%s» в периоде «%s» сохранена.", LogLevel.INFO, true),
        L20("Закончена загрузка данных файла «%s».", LogLevel.INFO, true),
        L21("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена. %s.", LogLevel.ERROR, true),
        // L22("Итоговая сумма в графе «%s» строки %d в транспортном файле некорректна. Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L23("Запущена процедура загрузки транспортных файлов, содержащих данные справочников.", LogLevel.INFO, true),
        L24("Завершена процедура загрузки транспортных файлов, содержащих данные справочников. Файлов загружено: %d. Файлов отклонено: %d.", LogLevel.INFO, true),
        // L25("Не указан путь к корректному файлу ключей ЭЦП! Загрузка файла не выполнена.", LogLevel.ERROR, true),
        L26("Транспортный файл размещен в каталоге ошибок в составе архива «%s».", LogLevel.INFO, true),
        L27("Транспортный файл не записан в каталог ошибок! Загрузка файла не выполнена. %s.", LogLevel.ERROR, true),
        L28("Ошибка при удалении файла «%s» при перемещении в каталог ошибок! %s.", LogLevel.ERROR, true),
        L29("Ошибка при удалении файла «%s» при перемещении в каталог архива! %s.", LogLevel.ERROR, true),
        L30("К каталогу загрузки для подразделения «%s» не указан корректный путь!", LogLevel.ERROR, true),
        L31("В каталоге загрузки для %s не найдены файлы!", LogLevel.ERROR, true),
        L36("Обнаружена ошибка при использовании библиотеки для проверки ЭЦП! %s.", LogLevel.ERROR, true),
        L37("Не указан путь к каталогу загрузки для %s!", LogLevel.ERROR, true),
        // Сообщения которых нет в постановке
        L_1("Не указан каталог ошибок в конфигурационных параметрах АС «Учет налогов»!", LogLevel.ERROR, true),
        L_2("Не указан каталог архива в конфигурационных параметрах АС «Учет налогов»!", LogLevel.ERROR, true);

        private LogLevel level;
        private String text;
        private boolean logSystem;

        private LogData(String text, LogLevel level, boolean logSystem) {
            this.text = text;
            this.level = level;
            this.logSystem = logSystem;
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
    }

    /**
     * Логгирование в области уведомлений и ЖА при импорте из ТФ
     */
    protected final void log(TAUserInfo userInfo, LogData logData, Logger logger, Object... args) {
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
            if (userInfo != null) {
                departmentId = userInfo.getUser().getDepartmentId();
            }
            String prefix = "";
            if (userInfo.getUser().getId() == TAUser.SYSTEM_USER_ID) {
                prefix = "Событие инициировано Системой. ";
            }
            auditService.add(FormDataEvent.IMPORT_TRANSPORT_FILE, userInfo, departmentId, null,
                    null, null, null, prefix + String.format(logData.getText(), args));
        }
    }

    /**
     * Создание или обновление каталога с требуемой структурой:
     * Каталог/Текущий_год/Текущий_месяц/Текущий_день_месяца/
     */
    private FileWrapper createPath(String rootPath, Calendar calendar) {
        FileWrapper errorFolderDst = ResourceUtils.getSharedResource(rootPath + calendar.get(Calendar.YEAR)
                + "/" + Months.fromId(calendar.get(Calendar.MONTH)).getName()
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
                                                 List<LogEntry> errorList, Logger logger) {
        return moveToDirectory(userInfo, errorPath, errorFileSrc, errorList, logger, LogData.L28, LogData.L26, LogData.L27);
    }

    /**
     * Перенос ТФ в каталог архива
     *
     * @param archivePath    Путь к каталогу архива
     * @param archiveFileSrc Файл, который должен быть перенесен
     */
    protected final boolean moveToArchiveDirectory(TAUserInfo userInfo, String archivePath, FileWrapper archiveFileSrc,
                                                   Logger logger) {
        return moveToDirectory(userInfo, archivePath, archiveFileSrc, null, logger, LogData.L29, LogData.L11, LogData.L12);
    }

    /**
     * Перенос ТФ в каталог ошибок или архива
     */
    private boolean moveToDirectory(TAUserInfo userInfo, String rootPath, FileWrapper file, List<LogEntry> errorList,
                                    Logger logger, LogData deleteErrorLogData, LogData successLogData, LogData moveErrorLogData) {
        boolean success = true;
        try {
            // Создание дерева каталогов
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            FileWrapper folderDst = createPath(rootPath, calendar);

            // Создание архива
            String path = folderDst.getPath() + "/" + file.getName()
                    + dateFormat.format(calendar.getTime()) + ".zip";
            FileWrapper fileDst = ResourceUtils.getSharedResource(path, false);
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(fileDst.getOutputStream());
            zaos.setEncoding(ZIP_ENCODING);
            zaos.putArchiveEntry(new ZipArchiveEntry(file.getName()));
            InputStream inputStream = file.getInputStream();
            IOUtils.copy(inputStream, zaos);
            zaos.closeArchiveEntry();
            IOUtils.closeQuietly(inputStream);

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
                log(userInfo, deleteErrorLogData, logger, file.getPath(), e.getMessage());
            }

            log(userInfo, successLogData, logger, fileDst.getName());
        } catch (Exception e) {
            success = false;
            log(userInfo, moveErrorLogData, logger, e.getMessage());
        }
        return success;
    }
}
