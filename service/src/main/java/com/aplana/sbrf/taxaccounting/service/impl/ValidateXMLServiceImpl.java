package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.google.common.io.Files;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
public class ValidateXMLServiceImpl implements ValidateXMLService {

    private static final Log LOG = LogFactory.getLog(ValidateXMLServiceImpl.class);

    private static final long VALIDATION_TIMEOUT = 1000 * 60 * 60L; //таймаут работы утилиты для валидации XML по XSD

    private static final String SUCCESS_FLAG = "SUCCESS";
    private static final String NOT_DELETE_WARN = "Файл %s не был удален";
    private static final String FILE_NAME_IN_TEMP_PATTERN = System.getProperty("java.io.tmpdir") + File.separator + "%s.%s";

    @Autowired
    private DeclarationService declarationService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ReportService reportService;

    private final class ProcessRunner implements Runnable {
        private String[] params;
        private Logger logger;
        private Process process;

        private ProcessRunner(String[] params, Logger logger) {
            this.params = params;
            this.logger = logger;
        }

        private void processDestroy() {
            if (process != null) {
                process.destroy();
            }
        }

        @Override
        public void run() {
            try {
                LOG.info("Запускаем проверку xml.");
                process = (new ProcessBuilder(params)).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
                try {
                    if (Thread.interrupted()) {
                        LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
                        throw new TAInterruptedException();
                    }
                    String s = reader.readLine();
                    if (s != null && s.startsWith("Result: " + SUCCESS_FLAG)) {
                        logger.info("Проверка xml по xsd завершена успешно.");
                    } else if (s != null) {
                        while ((s = reader.readLine()) != null) {
                            if (Thread.interrupted()) {
                                LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
                                throw new TAInterruptedException();
                            }
                            if (!s.startsWith("Execution time:")) {
                                logger.error(s);
                            }
                        }
                    } else {
                        logger.warn("Не удалось заустить проверку xml по xsd.");
                    }
                } finally {
                    processDestroy();
                    reader.close();
                }
            } catch (IOException e) {
                LOG.error("", e);
                throw new ServiceException("", e);
            }
        }
    }

    @Override
    public boolean validate(DeclarationData data, Logger logger, File xmlFile, String fileName, String xsdBlobDataId) {

        // Создаём локальный логгер на случай,
        // если в пришедшем логгере есть нефатальные ошибки или предупреждения с предыдущего этапа
        Logger localLogger = new Logger();
        boolean result = validate(data, localLogger, xmlFile, fileName, xsdBlobDataId, VALIDATION_TIMEOUT);

        // Переносим записи из локального логгера
        logger.getEntries().addAll(localLogger.getEntries());
        return result;
    }

    boolean validate(DeclarationData data, Logger logger, File xmlFile, String fileName, String xsdBlobDataId, long timeout) {
        if (xsdBlobDataId == null) {
            xsdBlobDataId = declarationService.findXsdIdByTemplateId(data.getDeclarationTemplateId());
        }
        if (xmlFile != null) {
            return isValid(logger, xmlFile, fileName, xsdBlobDataId, timeout);
        } else {
            return isValid(data, logger, fileName, xsdBlobDataId, timeout);
        }
    }


    @Override
    public boolean validate(Logger logger, String xmlFileName, File xmlFile, String xsdFileName, InputStream xsdStream) {
        return isValid(logger, xmlFileName, xmlFile, xsdFileName, xsdStream, VALIDATION_TIMEOUT);
    }

    private boolean isValid(Logger logger, String xmlFileName, File xmlFile, String xsdFileName, InputStream xsdStream, long timeout) {
        String[] params = new String[StringUtils.isNotBlank(xmlFileName) ? 4 : 3];

        File xsdFile = null, vsax3TempDir = null, vsax3ExeFile, vsax3DllFile;
        try {
            vsax3TempDir = Files.createTempDir();
            vsax3ExeFile = new File(vsax3TempDir, "VSAX3.exe");
            vsax3DllFile = new File(vsax3TempDir, "VSAX3.dll");
            copyToFile(this.getClass().getResourceAsStream("/vsax3/VSAX3.exe"), vsax3ExeFile);
            copyToFile(this.getClass().getResourceAsStream("/vsax3/VSAX3.dll"), vsax3DllFile);

            params[0] = vsax3ExeFile.getAbsolutePath();
            //Получаем xml
            params[1] = xmlFile.getAbsolutePath();
            //Получаем xsd файл
            xsdFile = createTempFile(xsdStream, "validation_file", ".xsd");
            params[2] = xsdFile.getAbsolutePath();

            if (StringUtils.isNotBlank(xmlFileName)) {
                params[3] = xmlFileName;
            }

            ProcessRunner runner = new ProcessRunner(params, logger);
            Thread threadRunner = new Thread(runner);
            threadRunner.start();
            try {
                long startTime = new Date().getTime();
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
                        throw new TAInterruptedException();
                    }
                    if (Math.abs(new Date().getTime() - startTime) > timeout) {
                        logger.warn(String.format("Истекло время выполнения проверки. Проверка длилась более %d мс.", timeout));
                        return false;
                    }
                    if (!threadRunner.isAlive()) {
                        return !logger.containsLevel(LogLevel.ERROR);
                    }
                }
            } finally {
                if (threadRunner.isAlive()) {
                    runner.processDestroy();
                    threadRunner.interrupt();
                    try {
                        threadRunner.join();
                    } catch (InterruptedException e) {
                        LOG.info("Thread " + Thread.currentThread().getName() + " was interrupted");
                        throw new TAInterruptedException();
                    }
                }
                logger.info("Проверка выполнена по файлу xsd: \"%s\"", xsdFileName);
                fileInfo(logger, vsax3DllFile);
            }
        } catch (IOException e) {
            LOG.error("", e);
            logger.warn("Произошла ошибка ввода-вывода. Проверка не выполнена.");
            return false;
        } finally {
            deleteTempFile(xsdFile);
            deleteTempFile(vsax3TempDir);
        }
    }

    private File createTempFile(InputStream inputStream, String prefix, String suffix) {
        File file;
        try {
            file = File.createTempFile(prefix, suffix);
            copyToFile(inputStream, file);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        return file;
    }

    private void copyToFile(InputStream inputStream, File fileDest) throws IOException {
        try (InputStream in = inputStream;
             OutputStream out = new BufferedOutputStream(new FileOutputStream(fileDest))) {
            IOUtils.copy(in, out);
        }
    }

    private void deleteTempFile(File tempFile) {
        if (tempFile != null && !tempFile.delete()) {
            LOG.warn(String.format(NOT_DELETE_WARN, tempFile.getName()));
        }
    }

    private boolean isValid(Logger logger, File xmlFile, String fileName, String xsdBlobDataId, long timeout) {
        BlobData xsd = blobDataService.get(xsdBlobDataId);
        return isValid(logger, fileName, xmlFile, xsd.getName(), xsd.getInputStream(), timeout);
    }

    private boolean isValid(DeclarationData data, Logger logger, String fileName, String xsdBlobDataId, long timeout) {

        BlobData xmlBlob = blobDataService.get(reportService.getReportFileUuidSafe(data.getId(), DeclarationDataReportType.XML_DEC));
        File xmlFileBD = null;
        try {
            String xmlFileName = xmlBlob.getName().substring(0, xmlBlob.getName().lastIndexOf('.'));
            xmlFileBD = new File(String.format(FILE_NAME_IN_TEMP_PATTERN, xmlFileName, "xml"));
            FileOutputStream outputStream = new FileOutputStream(xmlFileBD);
            InputStream inputStream = xmlBlob.getInputStream();
            unzip(outputStream, inputStream);
            return isValid(logger, xmlFileBD, fileName, xsdBlobDataId, timeout);
        } catch (IOException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        } finally {
            deleteTempFile(xmlFileBD);
        }
    }

    interface Version extends Library {

        Version INSTANCE = (Version) Native.loadLibrary("Version", Version.class, W32APIOptions.UNICODE_OPTIONS);

        /**
         * Determines whether the operating system can retrieve version information for a specified file.
         * If version information is available, GetFileVersionInfoSize returns the size, in bytes, of that information
         *
         * @param lptstrFilename The name of the file of interest.
         * @param dwDummy        A pointer to a variable that the function sets to zero.
         * @return info size
         */
        int GetFileVersionInfoSizeW(String lptstrFilename, int dwDummy);

        /**
         * Retrieves version information for the specified file.
         *
         * @param lptstrFilename The name of the file. If a full path is not specified, the function uses the search
         *                       sequence specified by the LoadLibrary function.
         * @param dwHandle       This parameter is ignored.
         * @param dwLen          The size, in bytes, of the buffer pointed to by the lpData parameter.
         * @param lpData         Pointer to a buffer that receives the file-version information.
         * @return true If the function succeeds
         */
        boolean GetFileVersionInfoW(String lptstrFilename, int dwHandle,
                                    int dwLen, Pointer lpData);

        /**
         * Retrieves specified version information from the specified version-information resource.
         * To retrieve the appropriate resource, before you call VerQueryValue, you must first call the
         * {@link #GetFileVersionInfoSizeW(String, int)} function, and then the
         * {@link #GetFileVersionInfoW(String, int, int, com.sun.jna.Pointer)} function.
         *
         * @param pBlock     The version-information resource returned by the GetFileVersionInfo function.
         * @param lpSubBlock The version-information value to be retrieved.
         *                   The string must consist of names separated by backslashes (\) and it must have one of the following forms.
         * @param lplpBuffer When this method returns, contains the address of a pointer to the requested
         *                   version information in the buffer pointed to by pBlock.
         * @param puLen      When this method returns, contains a pointer to the size of the requested data pointed to by lplpBuffer;
         *                   for version information values, the length in characters of the string stored at lplpBuffer;
         *                   for translation array values, the size in bytes of the array stored at lplpBuffer;
         *                   and for root block, the size in bytes of the structure
         * @return the return value is nonzero - If the specified version-information structure exists, and version information is available
         */
        int VerQueryValueW(Pointer pBlock, String lpSubBlock,
                           PointerByReference lplpBuffer, IntByReference puLen);

    }

    /**
     * Contains version information for a file. This information is language and code page independent.
     * https://msdn.microsoft.com/en-us/library/ms646997(v=vs.85).aspx
     */
    public static class VS_FIXEDFILEINFO extends com.sun.jna.Structure {
        // DWORD is always 32 bits, so first two revision numbers come from dwFileVersionMS, last two come from dwFileVersionLS
        public int dwFileVersionMS;
        public int dwFileVersionLS;
        /**
         * The binary version number of this structure. The high-order word of this member contains the major version number,
         * and the low-order word contains the minor version number.
         */
        public int dwStrucVersion;
        /**
         * The most significant 32 bits of the binary version number of the product with which this file was distributed.
         */
        public int dwProductVersionMS;

        /**
         * The least significant 32 bits of the binary version number of the product with which this file was distributed.
         */
        public int dwProductVersionLS;

        public VS_FIXEDFILEINFO(com.sun.jna.Pointer p) {
            super(p);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("dwFileVersionMS", "dwFileVersionLS", "dwStrucVersion", "dwProductVersionMS", "dwProductVersionLS");
        }
    }

    private void fileInfo(Logger logger, File fileVSAX) {

        int dwDummy = 0;
        int versionlength = Version.INSTANCE.GetFileVersionInfoSizeW(
                fileVSAX.getAbsolutePath(), dwDummy);

        byte[] bufferarray = new byte[versionlength];
        Pointer lpData = new Memory(bufferarray.length);

        PointerByReference lplpBuffer = new PointerByReference();
        IntByReference puLen = new IntByReference();
        boolean fileInfoResult = Version.INSTANCE.GetFileVersionInfoW(
                fileVSAX.getAbsolutePath(),
                0, versionlength, lpData);
        if (!fileInfoResult) {
            logger.error("Нет связанной с файлом информации.");
            return;
        }
        int verQueryVal = Version.INSTANCE.VerQueryValueW(lpData,
                "\\", lplpBuffer,
                puLen);
        if (verQueryVal == 0) {
            logger.error("Связанная с файлом мета-информация недоступна.");
            return;
        }

        VS_FIXEDFILEINFO lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
        lplpBufStructure.read();

        short[] rtnData = new short[4];
        rtnData[0] = (short) (lplpBufStructure.dwProductVersionLS >> 16);
        rtnData[1] = (short) (lplpBufStructure.dwProductVersionLS & 0xffff);
        rtnData[2] = (short) (lplpBufStructure.dwProductVersionMS >> 16);
        rtnData[3] = (short) (lplpBufStructure.dwProductVersionMS & 0xffff);

        logger.info("Проверка выполнена по библиотеке ФНС версии %d.%d.%d.%d",
                rtnData[0], rtnData[1], rtnData[2], rtnData[3]);
    }

    private void unzip(FileOutputStream outFile, InputStream zipXml) throws IOException {
        ZipInputStream zis = new ZipInputStream(zipXml);
        try {
            while (zis.getNextEntry() != null) {
                LOG.info("Xml copy, total number of bytes " + IOUtils.copy(zis, outFile));
            }
        } finally {
            IOUtils.closeQuietly(zis);
            IOUtils.closeQuietly(outFile);
            IOUtils.closeQuietly(zipXml);
        }
    }
}