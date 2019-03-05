package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.result.RefBookConfListItem;
import com.aplana.sbrf.taxaccounting.model.util.AppFileUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.script.service.RefBookService;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.getExtension;


@Service("refBookService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefBookServiceImpl implements RefBookService {

    @Autowired
    private CommonRefBookService commonRefBookService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookAsnuService refBookAsnuService;

    @Autowired
    private RefBookCountryService refBookCountryService;

    @Autowired
    private RefBookDocTypeService refBookDocTypeService;

    @Autowired
    private RefBookTaxpayerStateService refBookTaxpayerStateService;


    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        try {
            return refBookFactory.getDataProvider(refBookId).getRecordData(recordId);
        } catch (DaoException e) {
            return null;
        }
    }

    @Override
    public String getStringValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getStringValue() : null;
    }

    @Override
    public void executeInNewTransaction(TransactionLogic logic) {
        transactionHelper.executeInNewTransaction(logic);
    }

    private RefBookValue getValue(Long refBookId, Long recordId, String alias) {
        if (refBookId == null || recordId == null || alias == null || alias.isEmpty())
            return null;

        Map<String, RefBookValue> map = getRecordData(refBookId, recordId);

        if (map == null || map.isEmpty() || !map.containsKey(alias))
            return null;

        return map.get(alias);
    }

    @Override
    public Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId,
                                                     Map<String, Map<String, RefBookValue>> refBookCache) {
        if (recordId == null) {
            return null;
        }
        String key = refBookId + "_" + recordId;
        if (!refBookCache.containsKey(key)) {
            refBookCache.put(key, getRecordData(refBookId, recordId));
        }
        return refBookCache.get(key);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_SETTINGS)")
    public PagingResult<RefBookConfListItem> fetchRefBookConfPage(PagingParams pagingParams, TAUserInfo userInfo) {
        return refBookDao.fetchRefBookConfPage(pagingParams);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_SETTINGS)")
    @Transactional
    public BlobData exportRefBookConfs(TAUserInfo userInfo) {
        String resultUUID;
        File file = null;
        try {
            file = File.createTempFile("refBookData", "zip");

            try (OutputStream outputStream = new FileOutputStream(file);
                 ZipArchiveOutputStream zos = new ZipArchiveOutputStream(outputStream)
            ) {
                List<RefBook> refBooks = commonRefBookService.findAllVisible();
                for (RefBook refBook : refBooks) {
                    addFileToZip(refBook.getScriptId(), zos, refBook.getId() + "\\script.groovy");
                    addFileToZip(refBook.getXsdId(), zos, refBook.getId() + "\\schema.xsd");
                }
            }

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                resultUUID = blobDataService.create(inputStream, "refBooksData.zip");
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        } finally {
            AppFileUtils.deleteTmp(file);
        }
        return blobDataService.get(resultUUID);
    }

    private void addFileToZip(String uuid, ZipArchiveOutputStream zipOutputStream, String path) throws IOException {
        if (!StringUtils.isEmpty(uuid)) {
            ZipArchiveEntry zipEntry = new ZipArchiveEntry(path);
            zipOutputStream.putArchiveEntry(zipEntry);
            IOUtils.copy(blobDataService.get(uuid).getInputStream(), zipOutputStream);
            zipOutputStream.closeArchiveEntry();
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_ADMINISTRATION_SETTINGS)")
    public String importRefBookConfs(InputStream inputStream, String fileName, TAUserInfo userInfo) {
        Logger logger = new Logger();

        boolean filesExists = false;
        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream)) {
            ArchiveEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    filesExists = true;
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(zipInputStream));
                    importRefBookConf(byteArrayInputStream, zipEntry.getName(), logger);
                }
            }
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }

        if (filesExists) {
            logger.logTopMessage(LogLevel.INFO, "Выполнен импорт скриптов и xsd справочников");
        } else {
            logger.logTopMessage(LogLevel.INFO, "Выполнен импорт скриптов и xsd справочников. Загружаемых файлов в архиве не обнаружено.");
        }
        return logEntryService.save(logger.getEntries());
    }

    private void importRefBookConf(ByteArrayInputStream inputStream, String fileName, Logger logger) throws IOException {
        String dirName = new File(fileName).getParent();
        if (dirName != null && new File(dirName).getParent() == null) {
            try {
                Long refBookId = Long.valueOf(dirName);
                RefBook refBook = commonRefBookService.get(refBookId);
                if ("groovy".equals(getExtension(fileName))) {
                    String oldUuid = refBook.getScriptId();
                    if (inputStream.available() == 0) { // пустой файл, значит удаляем
                        if (oldUuid != null) {
                            refBookDao.updateScriptId(refBookId, null);
                        }
                        logger.info("Удален скрипт для справочника \"%s\", т.к. файл \"%s\" пустой.", refBook.getName(), fileName);
                    } else if (oldUuid == null) {
                        String newUuid = blobDataService.create(inputStream, "script.groovy");
                        refBookDao.updateScriptId(refBookId, newUuid);
                        logger.info("Добавлен скрипт для справочника \"%s\", взятый из файла \"%s\".", refBook.getName(), fileName);
                    } else if (!IOUtils.contentEquals(inputStream, blobDataService.get(oldUuid).getInputStream())) {
                        inputStream.reset();
                        blobDataService.save(oldUuid, inputStream);
                        logger.info("Обновлен скрипт для справочника \"%s\", взятый из файла \"%s\".", refBook.getName(), fileName);
                    } else {
                        logger.info("Пропущен скрипт для справочника \"%s\", взятый из файла \"%s\", т.к. содержимое файла не отличается от текущего.",
                                refBook.getName(), fileName);
                    }
                } else if ("xsd".equals(getExtension(fileName))) {
                    String oldUuid = refBook.getXsdId();
                    if (inputStream.available() == 0) { // пустой файл, значит удаляем
                        if (oldUuid != null) {
                            refBookDao.updateXsdId(refBookId, null);
                        }
                        logger.info("Удален xsd для справочника \"%s\", т.к. файл \"%s\" пустой.", refBook.getName(), fileName);
                    } else if (oldUuid == null) {
                        String newUuid = blobDataService.create(inputStream, "schema.xsd");
                        refBookDao.updateXsdId(refBookId, newUuid);
                        logger.info("Добавлен xsd для справочника \"%s\", взятый из файла \"%s\".", refBook.getName(), fileName);
                    } else if (!IOUtils.contentEquals(inputStream, blobDataService.get(oldUuid).getInputStream())) {
                        inputStream.reset();
                        blobDataService.save(oldUuid, inputStream);
                        logger.info("Обновлен xsd для справочника \"%s\", взятый из файла \"%s\".", refBook.getName(), fileName);
                    } else {
                        logger.info("Пропущен xsd для справочника \"%s\", взятый из файла \"%s\", т.к. содержимое файла не отличается от текущего.",
                                refBook.getName(), fileName);
                    }
                } else {
                    logger.warn("Пропущен файл \"%s\". Файлы с расширением \"%s\" не обрабатывается текущей реализацией.",
                            fileName, getExtension(fileName));
                }
            } catch (NumberFormatException e) {
                logger.warn("Не загружен файл \"%s\". Папка, содержащая файл, \"%s\" должна соответствовать идентификатору справочника.", fileName, dirName);
            }
        } else {
            logger.warn("Пропущен файл \"%s\". Загружаемый файл должен лежать в папке с именем, соответствующим идентификатору справочника.",
                    fileName);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<RefBookAsnu> findAllAsnu() {
        return refBookAsnuService.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public RefBookAsnu getAsnu(Long asnuId) {
        return refBookAsnuService.fetchById(asnuId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsCountryByCode(String code) {
        return refBookCountryService.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsDocTypeByCode(String code) {
        return refBookDocTypeService.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsTaxpayerStateByCode(String code) {
        return refBookTaxpayerStateService.existsByCode(code);
    }
}
