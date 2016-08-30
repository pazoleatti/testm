package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Реализация сервиса для выполнения скриптов над формой.
 *
 * @author Stanislav Yasinskiy
 */
@Service
@Transactional
public class ScriptExecutionServiceImpl extends TAAbstractScriptingServiceImpl implements ApplicationContextAware, ScriptExecutionService {

	private static final Log LOG = LogFactory.getLog(ScriptExecutionServiceImpl.class);


    final static String LOCK_MESSAGE = "%s \"%s\" заблокирован пользователем с логином \"%s\". Макет пропущен.";
    final static String ZIP_ENCODING = "cp866";

    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private RefBookScriptingService refBookScriptingService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public void executeScript(TAUserInfo userInfo, String script, Logger logger) {
		if (!canExecuteScript(script, null)) {
			logger.warn("Скрипт не может быть выполнен, так как он не содержит кода");
			return;
		}
        // Биндим параметры для выполнения скрипта
        Bindings b = getScriptEngine().createBindings();
        Map<String, ?> scriptComponents =  getScriptExposedBeans();
        for (Object component : scriptComponents.values()) {
            ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
            scriptComponentContext.setUserInfo(userInfo);
            if (component instanceof ScriptComponentContextHolder){
                ((ScriptComponentContextHolder)component).setScriptComponentContext(scriptComponentContext);
            }
        }
        b.putAll(scriptComponents);
        b.put("logger", logger);
        b.put("user", userInfo.getUser());

        executeScript(b, script, logger);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Найдены ошибки при выполнении расчета формы", logEntryService.save(logger.getEntries()));
        } else {
            logger.info("Скрипт успешно выполнен");
        }
    }

    private void executeScript(Bindings bindings, String script, Logger logger) {
        try {
            getScriptEngine().eval(script, bindings);
        } catch (ScriptException e) {
            logScriptException(e, logger);
        } catch (Exception e) {
            logger.error(e.getMessage());
			LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Возвращает спринг-бины доступные для использования в скрипте.
     */
    private Map<String, ?> getScriptExposedBeans() {
        Map<String, Object> beans = new HashMap<String, Object>();

        // http://jira.aplana.com/browse/SBRFACCTAX-10954 Передаем в скрипты все спринговые бины
        for(String name: applicationContext.getBeanDefinitionNames()) {
            beans.put(name, applicationContext.getBean(name));
        }

        return beans;
    }

    /**
     * Проверяе, возможен ли импорт в указаннный макет (нет ли на нем блокировки)
     * @param lockKey ключ блокировки макета
     * @return если блокировка существует, то возвращается логин пользователя (тевущий пользователь не учитывается)
     */
    private String canImportScript(String lockKey, TAUser user){
        LockData lockData = lockDataService.getLock(lockKey);
        if (lockData != null && lockData.getUserId() != user.getId()) {
            return userService.getUser(lockData.getUserId()).getLogin();
        }
        return null;
    }

    @Override
    public void importScripts(Logger logger, InputStream zipFile, String fileName, TAUserInfo userInfo) {
        Map<String, List<String>> files = new HashMap<String, List<String>>();
        ZipArchiveInputStream zis = null;
        boolean hasFatalError = false;
        Set<String> errorFolders = new HashSet<String>();
        try {
            zis = new ZipArchiveInputStream(new BufferedInputStream(zipFile), ZIP_ENCODING);
            ArchiveEntry entry;
            while((entry = zis.getNextZipEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (!entry.getName().contains("/")) {
                        logger.error("Структура архива некорректна! Импорт файла отменен.");
                        hasFatalError = true;
                        break;
                    }
                    File file = new File(entry.getName());
                    String folderName = file.getParent();
                    String scriptFileName = file.getName();
                    String scriptName = scriptFileName.substring(0, scriptFileName.indexOf("."));

                    if (!files.containsKey(folderName)) {
                        files.put(folderName, new ArrayList<String>());
                    }
                    files.get(folderName).add(scriptName);

                    if (!FOLDERS.contains(folderName)) {
                        if (errorFolders.add(folderName))
                            logger.error("Пропущен каталог \"%s\", так как его имя не поддерживается", folderName);
                        continue;
                    }
                    String script = IOUtils.toString(zis, "UTF-8");
                    /** Импорт скриптов нф */
                    if (folderName.equals(FOLDERS.form_template.name())) {
                        int formTypeId, year;
                        try {
                            formTypeId = Integer.parseInt(scriptName.substring(0, scriptName.indexOf("-")));
                            year = Integer.parseInt(scriptName.substring(scriptName.indexOf("-") + 1));
                        } catch (Exception e) {
                            logger.error("Наименование файла \"%s\" некорректно. Файл пропущен.", scriptFileName);
                            continue;
                        }
                        Integer formTemplateId = formTemplateService.get(formTypeId, year);
                        if (formTemplateId == null) {
                            logger.error("Макет налоговой формы, указанный в файле \"%s\" не существует. Файл пропущен.", scriptFileName);
                            continue;
                        }

                        //Проверяем не заблокирован ли макет
                        FormTemplate formTemplate = SerializationUtils.clone(formTemplateService.get(formTemplateId));
                        String lockUser = canImportScript(LockData.LockObjects.FORM_TEMPLATE.name() + "_" + formTemplateId, userInfo.getUser());
                        if (lockUser == null){
                            formTemplate.setScript(script);
                            Logger localLogger = new Logger();
                            formTemplateService.updateScript(formTemplate, localLogger, userInfo);
                            if (localLogger.containsLevel(LogLevel.ERROR)) {
                                logger.getEntries().addAll(localLogger.getEntries());
                                logger.error("Макет налоговой формы \"%s\", указанный в файле \"%s\" содержит ошибки. Файл пропущен ", formTemplate.getName(), scriptFileName);                                continue;
                            }
                            logger.info("Выполнен импорт скрипта для макета налоговой формы \"%s\" из файла \"%s\"", formTemplate.getName(), scriptFileName);
                        } else {
                            logger.error(LOCK_MESSAGE, "Макет налоговой формы", formTemplate.getName(), lockUser);
                        }
                    }

                    /** Импорт скриптов деклараций */
                    if (folderName.equals(FOLDERS.declaration_template.name())) {
                        int declarationTypeId, year;
                        try {
                            declarationTypeId = Integer.parseInt(scriptName.substring(0, scriptName.indexOf("-")));
                            year = Integer.parseInt(scriptName.substring(scriptName.indexOf("-") + 1));
                        } catch (Exception e) {
                            logger.error("Наименование файла \"%s\" некорректно. Файл пропущен.", scriptFileName);
                            continue;
                        }
                        Integer declarationTemplateId = declarationTemplateService.get(declarationTypeId, year);
                        if (declarationTemplateId == null) {
                            logger.error("Макет декларации/уведомления, указанный в файле \"%s\" не существует. Файл пропущен.", scriptFileName);
                            continue;
                        }

                        //Проверяем не заблокирован ли макет
                        DeclarationTemplate declarationTemplate = SerializationUtils.clone(declarationTemplateService.get(declarationTemplateId));
                        String lockUser = canImportScript(LockData.LockObjects.DECLARATION_TEMPLATE.name() + "_" + declarationTemplateId, userInfo.getUser());
                        if (lockUser == null) {
                            declarationTemplate.setCreateScript(script);
                            Logger localLogger = new Logger();
                            declarationTemplateService.updateScript(declarationTemplate, localLogger, userInfo);
                            if (localLogger.containsLevel(LogLevel.ERROR)) {
                                logger.getEntries().addAll(localLogger.getEntries());
                                logger.error("%s \"%s\", указанный в файле \"%s\" содержит ошибки. Файл пропущен.",
                                        declarationTemplate.getType().getTaxType() != TaxType.DEAL ? "Макет декларации" : "Макет уведомления",
                                        declarationTemplate.getName(), scriptName);
                                continue;
                            }
                            logger.info("Выполнен импорт скрипта для макета декларации формы \"%s\" из файла \"%s\"", declarationTemplate.getName(), scriptFileName);
                        } else {
                            logger.error(LOCK_MESSAGE,
                                    declarationTemplate.getType().getTaxType() != TaxType.DEAL ? "Макет декларации" : "Макет уведомления",
                                    declarationTemplate.getName(), lockUser);
                        }

                    }

                    /** Импорт скриптов справочников */
                    if (folderName.equals(FOLDERS.ref_book.name())) {
                        long refBookId;
                        try {
                            refBookId = Long.parseLong(scriptName);
                        } catch (NumberFormatException e) {
                            logger.error("Наименование файла \"%s\" некорректно. Файл пропущен.", scriptFileName);
                            continue;
                        }
                        if (!refBookDao.isRefBookExist(refBookId)) {
                            logger.error("Справочник, указанный в файле \"%s\" не существует. Файл пропущен.", scriptFileName);
                            continue;
                        }

                        //Проверяем не заблокирован ли справочник
                        RefBook refBook = SerializationUtils.clone(refBookDao.get(refBookId));
                        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(refBookId);
                        String lockUser = canImportScript(lockType != null ? lockType.getSecond().getKey() : null, userInfo.getUser());
                        if (lockUser == null) {
                            Logger localLogger = new Logger();
                            refBookScriptingService.importScript(refBookId, script, localLogger, userInfo);
                            if (localLogger.containsLevel(LogLevel.ERROR)) {
                                logger.getEntries().addAll(localLogger.getEntries());
                                logger.error("Справочник \"%s\", указанный в файле \"%s\" содержит ошибки. Файл пропущен.", refBook.getName(), scriptFileName);
                                continue;
                            }
                            logger.info("Выполнен импорт скрипта для справочника \"%s\" из файла \"%s\"", refBook.getName(), scriptFileName);
                        } else {
                            logger.error(LOCK_MESSAGE, "Справочник", refBook.getName(), lockUser);
                        }
                    }
                }
            }
        } catch(Exception e) {
            LOG.error(e.getMessage(), e);
            logger.error("При импорте скриптов произошла ошибка. Подробности записаны в журнал сервера. Все изменения отменены.");
            hasFatalError = true;
            throw new ServiceException(e.getMessage(), e);
        } finally {
            if (!hasFatalError) {
                logger.info("Импорт завершен");
            }
            StringBuilder auditMsg = new StringBuilder()
                    .append(hasFatalError ? "Ошибка при импорте скриптов из файла \"" : "Выполнен импорт скриптов из файла \"")
                    .append(fileName).append("\". ");
            for (Map.Entry<String, List<String>> file : files.entrySet()) {
                auditMsg.append(file.getKey()).append(": ");
                for (Iterator<String> it = file.getValue().iterator(); it.hasNext();) {
                    auditMsg.append(it.next());
                    if (it.hasNext()) {
                        auditMsg.append(",");
                    } else {
                        auditMsg.append("; ");
                    }
                }
            }
            auditService.add(FormDataEvent.SCRIPTS_IMPORT, userInfo, "", null, null, null, null, auditMsg.toString(), null, logEntryService.save(logger.getEntries()));
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {}
            }
        }
    }

    private enum FOLDERS {declaration_template, form_template, ref_book;
        public static boolean contains(String fileName) {
            for (FOLDERS folder : values()) {
                if (folder.name().equals(fileName)) {
                    return true;
                }
            }
            return false;
        }}
}
