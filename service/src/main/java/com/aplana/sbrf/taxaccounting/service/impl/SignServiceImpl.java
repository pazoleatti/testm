package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
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
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SignServiceImpl implements SignService {

	private static final Log LOG = LogFactory.getLog(SignServiceImpl.class);
    private static final Pattern DLL_PATTERN = Pattern.compile(".+\\.dll");
    private static final String SUCCESS_FLAG = "SUCCESS";
    private static final String TEMPLATE = ClassUtils.classPackageAsResourcePath(SignServiceImpl.class) + "/check-sign.exe";
	/** Таймаут работы утилиты для проверки ЭП */
    private static final long VALIDATION_TIMEOUT = 1000 * 60 * 10L;
	/** Максимальное количество попыток создания временной директории */
	private static final int TEMP_DIR_ATTEMPT_MAX_COUNT = 9;

    private static final String USER_ID_MSG = "ЭП файла «%s» проверена и подпись пользователя «%s» принята.";
    private static final String ERR_NO_SIGN_MSG = "В проверяемом файле «%s» отсутствует ЭП.";
    private static final String ERR_SIGN_NO_REG_MSG = "Идентификатор ЭП файла «%s» не зарегистрирован в БОК.";
    private static final String ERR_OTHER_MSG = "ЭП файла «%s» не принята. Код ошибки «%s».";

    private static final String pattern = "(ERROR:)(.+?)(, )(\\d+ )(.*)(, )(.*)";


	@Autowired
	private ConfigurationDao configurationDao;
	@Autowired
	private DepartmentService departmentService;

    class Status {
        private boolean check;
        private String msg;

        public boolean isCheck() {
            return check;
        }

        public void setCheck(boolean check) {
            this.check = check;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    private final class ProcessRunner implements Runnable{
        private String fileName;
        private String[] params;
        private Status status;

        private ProcessRunner(String[] params, String fileName, Status status) {
            this.params = params;
            this.fileName = fileName;
            this.status = status;
        }

        @Override
        public void run() {
            Process process;
            try {
                LOG.info("Запускаем проверку ЭП.");
                process = (new ProcessBuilder(params)).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
                try {
                    String s = reader.readLine();
                    if (s != null && s.startsWith("Result: " + SUCCESS_FLAG)) {
                        status.setCheck(true);
                        LOG.info("Проверка ЭП завершена успешно.");
                    }
                    do {
                        if (s.startsWith("UserId: ")) {
                            status.setMsg(String.format(USER_ID_MSG, fileName, s.substring(8)));
                        } else if (!s.startsWith("Result:") && !s.startsWith("Execution time:")) {
                            status.setCheck(false);
                            String code = s.replaceAll(pattern, "$5");
                            if (code.equals("ERR_NO_SIGN")) {
                                status.setMsg(String.format(ERR_NO_SIGN_MSG, fileName));
                            } else if (code.equals("ERR_SIGN_NO_REG")) {
                                status.setMsg(String.format(ERR_SIGN_NO_REG_MSG, fileName));
                            } else {
                                //String text = s.replaceAll(pattern, "$7");
                                status.setMsg(String.format(ERR_OTHER_MSG, fileName, code));
                            }
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

    boolean check(String[] params, String fileName, Status status) {
        ProcessRunner runner = new ProcessRunner(params, fileName, status);
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
                LOG.warn(String.format("Истекло время выполнения проверки ЭП. Проверка ЭП длилась более %d мс.", VALIDATION_TIMEOUT));
                status.setMsg(String.format("Истекло время выполнения проверки ЭП. Проверка ЭП длилась более %d мс.", VALIDATION_TIMEOUT));
                return false;
            }
            if (!threadRunner.isAlive()) {
                return status.isCheck();
            }
        }
    }

    @Override
    public Pair<Boolean, Set<String>> checkSign(String fileName, String pathToSignFile, int delFlag, Logger logger) {
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
        Set<String> msgs = new TreeSet<String>();
        try {
            //Копируем необходимые файлы во временную директорию
            try {
                // база открытых ключей
                keyTempDir = createTempDir("key_files_");
                for (String keyFolderPath : keys) {
                    FileWrapper keyResourceFolder = ResourceUtils.getSharedResource(keyFolderPath, false);
                    if (!keyResourceFolder.exists()) {
                        LOG.warn(String.format("Директория %s с ключами не существует", keyFolderPath));
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
                            LOG.warn(String.format("Директория %s с ключами пустая", keyFolderPath));
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

                // библиотеки ЭП
                encryptTempDir = createTempDir("encrypt_dll_");
                FileWrapper resourceDir = ResourceUtils.getSharedResource(encryptParams.get(0) + "/");
                if (!resourceDir.isDirectory())
                    throw new ServiceException("Необходимо указать директорию с библиотекой ЭП");
                String[] listFiles = resourceDir.list();
                assert listFiles != null;
                for (String dllFileName : listFiles) {
                    if (!DLL_PATTERN.matcher(dllFileName).matches())
                        continue;
                    File dllFile = new File(encryptTempDir.getPath()+ "/" + dllFileName);
                    if (dllFile.createNewFile()) {
                        FileWrapper resourceFile = ResourceUtils.getSharedResource(encryptParams.get(0) + "/" + dllFileName);
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
            for (String keyFileName: keyTempFiles) {
                params[1] = keyTempDir.getAbsolutePath()+ "\\" + keyFileName;
                final Status status = new Status();
                if (check(params, fileName, status)) {
                    return new Pair<Boolean, Set<String>>(true, new HashSet<String>(){{add(status.getMsg());}});
                } else {
                    msgs.add(status.getMsg());
                }
            }
            return new Pair<Boolean, Set<String>>(false, msgs);
        } finally {
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
        int attemptCount = 0;
        do {
            attemptCount++;
            if(attemptCount > TEMP_DIR_ATTEMPT_MAX_COUNT) {
                throw new IOException("Не удалось создать уникальный временный каталог после " + TEMP_DIR_ATTEMPT_MAX_COUNT + " попыток.");
            }
            String dirName = prefix + System.nanoTime();
            newTempDir = new File(sysTempDir, dirName);
        } while(newTempDir.exists());

        if (newTempDir.mkdirs()) {
            return newTempDir;
        }
        throw new IOException("Не удалось создать временный каталог с именем " + newTempDir.getAbsolutePath());
    }
}