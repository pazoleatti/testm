package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SignService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SignServiceImpl implements SignService {

	private static final Log LOG = LogFactory.getLog(SignServiceImpl.class);
    private static final Pattern DLL_PATTERN = Pattern.compile(".+\\.dll");
    private static final String SUCCESS_FLAG = "SUCCESS";
    private static final String TEMPLATE = ClassUtils.classPackageAsResourcePath(SignServiceImpl.class) + "/check-sign.exe";
    private static final long VALIDATION_TIMEOUT = 1000 * 60 * 10L; //таймаут работы утилиты для проверки ЭЦП

	@Autowired
	private ConfigurationDao configurationDao;
	@Autowired
	private DepartmentService departmentService;

    private final class ProcessRunner implements Runnable{
        private String[] params;
        private Logger logger;

        private ProcessRunner(String[] params, Logger logger) {
            this.params = params;
            this.logger = logger;
        }

        @Override
        public void run() {
            Process process;
            try {
                LOG.info("Запускаем проверку ЭЦП.");
                process = (new ProcessBuilder(params)).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
                try {
                    String s = reader.readLine();
                    if (s != null && s.startsWith("Result: " + SUCCESS_FLAG)) {
                        LOG.info("Проверка ЭЦП завершена успешно.");
                    }
                    do {
                        if (s.startsWith("UserId: ")) {
                            logger.info(s);
                        } else if (!s.startsWith("Result:") && !s.startsWith("Execution time:")) {
                            logger.errorIfNotExist(s);
                        }
                    } while ((s = reader.readLine()) != null);
                } finally {
                    process.destroy();
                    reader.close();
                }
            } catch (UnsupportedEncodingException e) {
                LOG.error("", e);
                throw new ServiceException("", e);
            } catch (IOException e) {
                LOG.error("", e);
                throw new ServiceException("", e);
            }
        }
    }

    boolean check(String[] params, Logger logger) {
        ProcessRunner runner = new ProcessRunner(params, logger);
        Thread threadRunner = new Thread(runner);
        threadRunner.start();
        long startTime = new Date().getTime();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (Math.abs(new Date().getTime() - startTime) > VALIDATION_TIMEOUT) {
                threadRunner.interrupt();
                LOG.warn(String.format("Истекло время выполнения проверки ЭЦП. Проверка ЭЦП длилась более %d мс.", VALIDATION_TIMEOUT));
                logger.error("Истекло время выполнения проверки ЭЦП. Проверка ЭЦП длилась более %d мс.", VALIDATION_TIMEOUT);
                return false;
            }
            if (!threadRunner.isAlive()) {
                return !logger.containsLevel(LogLevel.ERROR);
            }
        }
    }

    @Override
    public boolean checkSign(String pathToSignFile, int delFlag, Logger logger) {
        String[] params = new String[4];
        int rootDepartmentId = departmentService.getBankDepartment().getId();
        // путь к библиотеке для проверки ЭП
        List<String> encryptParams = configurationDao.getAll().get(ConfigurationParam.ENCRYPT_DLL, rootDepartmentId);
        if (encryptParams == null) {
            throw new ServiceException("Не заполнен конфигурационный параметр \"" + ConfigurationParam.ENCRYPT_DLL.getCaption() + '"');
        }
        // загрузка БОК (база открытых ключей)
        List<String> keys = configurationDao.getAll().get(ConfigurationParam.KEY_FILE, rootDepartmentId);
        if (keys == null){
            throw new ServiceException("Ошибка доступа к файлу базы открытых ключей. БОК не заданы.");
        }

        File checkSign = null;
        File keyTempDir = null;
        File encryptTempDir = null;
        FileOutputStream outputStream;
        InputStream inputStream;
        File keyFile;
        List<File> keyFiles = new ArrayList<File>();
        try {
            //Копируем необходимые файлы во временную директорию
            try {
                // база открытых ключей
                keyTempDir = createTempDir("key_files_");
                for (String keyFolderPath : keys) {
                    FileWrapper keyResourceFolder = ResourceUtils.getSharedResource(keyFolderPath, false);
                    if (!keyResourceFolder.exists()) {
                        LOG.warn(String.format("Директории %s с ключами не существует", keyFolderPath));
                        break;
                    }
                    String[] listFileNames;
                    if (keyResourceFolder.isFile()) {
                        keyFolderPath = keyFolderPath.replaceFirst(keyResourceFolder.getName() + "$", "");
                        listFileNames = new String[]{keyResourceFolder.getName()};
                    } else {
                        keyResourceFolder = ResourceUtils.getSharedResource(keyFolderPath + "/", false);
                        listFileNames = keyResourceFolder.list();
                        if (listFileNames == null || listFileNames.length == 0) {
                            LOG.warn(String.format("Директории %s с ключами пустая", keyFolderPath));
                            break;
                        }
                    }
                    //Проверяем по всем возможным БОК
                    for (String keyName : listFileNames) {
                        FileWrapper dbOfpk = ResourceUtils.getSharedResource(keyFolderPath + "/" + keyName);
                        if (dbOfpk.isDirectory()) {
                            continue;
                        }
                        keyFile = File.createTempFile("key_file_", ".dat", keyTempDir);
                        inputStream = dbOfpk.getInputStream();
                        outputStream = new FileOutputStream(keyFile);
                        try {
                            LOG.info("Key file copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
                        } finally {
                            inputStream.close();
                            outputStream.close();
                        }
                        keyFiles.add(keyFile);
                    }
                }

                // библиотеки ЭЦП
                encryptTempDir = createTempDir("encrypt_dll_");
                FileWrapper resourceDir = ResourceUtils.getSharedResource(encryptParams.get(0) + "/");
                if (!resourceDir.isDirectory())
                    throw new ServiceException("Необходимо указать директорию с библиотекой ЭЦП");
                String[] listFiles = resourceDir.list();
                assert listFiles != null;
                for (String fileName : listFiles) {
                    if (!DLL_PATTERN.matcher(fileName).matches())
                        continue;
                    File dllFile = new File(encryptTempDir.getPath()+ "/" + fileName);
                    if (dllFile.createNewFile()) {
                        FileWrapper resourceFile = ResourceUtils.getSharedResource(encryptParams.get(0) + "/" + fileName);
                        inputStream = resourceFile.getInputStream();
                        outputStream = new FileOutputStream(dllFile);
                        try {
                            LOG.info("Dll file copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
                        } finally {
                            inputStream.close();
                            outputStream.close();
                        }
                    }
                }
                //
                checkSign = new File(encryptTempDir.getPath()+ "\\check-sign.exe");
                outputStream = new FileOutputStream(checkSign);
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
                try {
                    LOG.info("check-sign.exe copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
                } finally {
                    inputStream.close();
                    outputStream.close();
                }
            } catch (IOException e) {
                LOG.error("", e);
                throw new ServiceException(e.getMessage(), e);
            }

            params[0] = checkSign.getAbsolutePath();
            params[2] = String.valueOf(delFlag);
            params[3] = pathToSignFile;

            String[] keyTempFiles = keyTempDir.list();
            Logger localLogger = new Logger();
            for (String fileName: keyTempFiles) {
                params[1] = keyTempDir.getAbsolutePath()+ "\\" + fileName;
                Logger checkLogger = new Logger();
                if (check(params, checkLogger)) {
                    logger.getEntries().addAll(checkLogger.getEntries());
                    return true;
                } else {
                    for(LogEntry log: checkLogger.getEntries()) {
                        if (!localLogger.getEntries().contains(log)) {
                            localLogger.getEntries().add(log);
                        }
                    }
                }
            }
            logger.error("Ошибки при проверке ЭЦП:");
            logger.getEntries().addAll(localLogger.getEntries());
            return false;
        } finally {
            try {
                //if (checkSign != null) checkSign.delete();
            } catch (Exception e) {
                LOG.error("", e);
            }
            try {
                if (keyTempDir != null) FileUtils.deleteDirectory(keyTempDir);
            } catch (Exception e) {
                LOG.error("", e);
            }
            try {
                if (encryptTempDir != null) FileUtils.deleteDirectory(encryptTempDir);
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    private static File createTempDir(String prefix) throws IOException {
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        File newTempDir;
        final int maxAttempts = 9;
        int attemptCount = 0;
        do {
            attemptCount++;
            if(attemptCount > maxAttempts) {
                throw new IOException(
                        "Не удалось создать уникальный временный каталог после " + maxAttempts + " попыток.");
            }
            String dirName = prefix + System.nanoTime();
            newTempDir = new File(sysTempDir, dirName);
        } while(newTempDir.exists());

        if (newTempDir.mkdirs()) {
            return newTempDir;
        } else {
            throw new IOException(
                    "Не удалось создать временный каталог с именем " +
                            newTempDir.getAbsolutePath());
        }
    }
}
