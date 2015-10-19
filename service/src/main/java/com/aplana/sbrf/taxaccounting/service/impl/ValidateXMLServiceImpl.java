package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
public class ValidateXMLServiceImpl implements ValidateXMLService {

    static final Log LOG = LogFactory.getLog(ValidateXMLServiceImpl.class);

    private static final String TEMPLATE = ClassUtils.classPackageAsResourcePath(ValidateXMLServiceImpl.class) + "/VSAX3.exe";
	private static final long VALIDATION_TIMEOUT = 1000 * 60 * 60; //таймаут работы утилиты для валидации XML по XSD

    private static final String SUCCESS_FLAG = "SUCCESS";
    public static final String NOT_DELETE_WARN = "Файл %s не был удален";
    private static final String FILE_NAME_IN_TEMP_PATTERN = System.getProperty("java.io.tmpdir")+ File.separator +"%s.%s";

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private LockDataService lockDataService;

    private final class ProcessRunner implements Runnable{
        private String[] params;
        private Logger logger;
        private boolean isErrorFatal;

        private ProcessRunner(String[] params, Logger logger, boolean isErrorFatal) {
            this.params = params;
            this.logger = logger;
            this.isErrorFatal = isErrorFatal;
        }

        @Override
        public void run() {
            Process process;
            try {
                LOG.info("Запускаем проверку xml.");
                process = (new ProcessBuilder(params)).start();
                /*process.waitFor();*/
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "Cp866"));
                try {
                    String s = reader.readLine();
                    if (s != null && s.startsWith("Result: " + SUCCESS_FLAG)) {
                        logger.info("Проверка xml по xsd завершена успешно.");
                    } else if(s!=null) {
                        while ((s = reader.readLine()) != null) {
                            if (!s.startsWith("Execution time:")) {
                                if (isErrorFatal)
                                    logger.error(s);
                                else
                                    logger.warn(s);
                            }
                        }
                    }
                    /*lock.notify();*/
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

    @Override
    public boolean validate(DeclarationData data,  TAUserInfo userInfo, Logger logger, boolean isErrorFatal, File xmlFile) {
		return validate(data, userInfo, logger, isErrorFatal, xmlFile, VALIDATION_TIMEOUT);
	}

	boolean validate(DeclarationData data,  TAUserInfo userInfo, Logger logger, boolean isErrorFatal, File xmlFile, long timeout) {
        if (xmlFile!=null){
            return isValid(data, userInfo, logger, isErrorFatal, xmlFile, timeout);
        } else {
            return isValid(data, userInfo, logger, isErrorFatal, timeout);
        }
    }

    boolean isValid(DeclarationData data, TAUserInfo userInfo, Logger logger, boolean isErrorFatal, File xmlFile, long timeout) {
        String[] params = new String[3];
        DeclarationTemplate template = declarationTemplateService.get(data.getDeclarationTemplateId());

        FileOutputStream outputStream;
        InputStream inputStream;
        File xsdFile = null, vsax3File = null;
        try {
            vsax3File = File.createTempFile("VSAX3",".exe");
            outputStream = new FileOutputStream(vsax3File);
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE);
			try {
            	LOG.info("VSAX3.exe copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
			} finally {
				inputStream.close();
				outputStream.close();
			}
            params[0] = vsax3File.getAbsolutePath();
            //Получаем xml
            params[1] = xmlFile.getAbsolutePath();
            //Получаем xsd файл
            xsdFile = File.createTempFile("validation_file",".xsd");
            outputStream = new FileOutputStream(xsdFile);
            BlobData blobData = blobDataService.get(template.getXsdId());
            inputStream = blobData.getInputStream();
			try {
            	LOG.info("Xsd copy, total number of bytes " + IOUtils.copy(inputStream, outputStream));
			} finally {
				inputStream.close();
				outputStream.close();
			}
            params[2] = xsdFile.getAbsolutePath();

            ProcessRunner runner = new ProcessRunner(params, logger, isErrorFatal);
            Thread threadRunner = new Thread(runner);
            threadRunner.start();
            try {
                long startTime = new Date().getTime();
                while (true) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (Math.abs(new Date().getTime() - startTime) > timeout) {
                        threadRunner.interrupt();
                        logger.warn(String.format("Истекло время выполнения проверки. Проверка длилась более %d мс.", timeout));
                        return false;
                    }
                    if (!threadRunner.isAlive()){
                        return !logger.containsLevel(LogLevel.ERROR);
                    }
                }

            } finally {
                logger.info("Проверка выполнена по файлу xsd %s", blobData.getName());
                fileInfo(logger, vsax3File);
            }
        } catch (IOException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        } finally {
            if (xsdFile != null && !xsdFile.delete()){
                LOG.warn(String.format(NOT_DELETE_WARN, xsdFile.getName()));
            }
            if (vsax3File != null && !vsax3File.delete()){
                LOG.warn(String.format(NOT_DELETE_WARN, vsax3File.getName()));
            }
        }
    }

    private boolean isValid(DeclarationData data, TAUserInfo userInfo, Logger logger, boolean isErrorFatal, long timeout) {
        BlobData xmlBlob = blobDataService.get(reportService.getDec(userInfo, data.getId(), ReportType.XML_DEC));
        File xmlFileBD = null;
        try {
            String fileName = xmlBlob.getName().substring(0, xmlBlob.getName().lastIndexOf('.'));
            xmlFileBD = new File(String.format(FILE_NAME_IN_TEMP_PATTERN, fileName, "xml"));
            FileOutputStream outputStream = new FileOutputStream(xmlFileBD);
            InputStream inputStream = xmlBlob.getInputStream();
            unzip(outputStream, inputStream);
            return isValid(data, userInfo, logger, isErrorFatal, xmlFileBD, timeout);
        } catch (IOException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        } finally {
            if (xmlFileBD!=null && !xmlFileBD.delete()){
                LOG.warn(String.format(NOT_DELETE_WARN, xmlFileBD.getName()));
            }
        }
    }

    interface Version extends Library {

        Version INSTANCE = (Version) Native.loadLibrary("Version", Version.class, W32APIOptions.UNICODE_OPTIONS);

        /**
         * Determines whether the operating system can retrieve version information for a specified file.
         * If version information is available, GetFileVersionInfoSize returns the size, in bytes, of that information
         * @param lptstrFilename The name of the file of interest.
         * @param dwDummy A pointer to a variable that the function sets to zero.
         * @return info size
         */
        int GetFileVersionInfoSizeW(String lptstrFilename, int dwDummy);

        /**
         * Retrieves version information for the specified file.
         * @param lptstrFilename The name of the file. If a full path is not specified, the function uses the search
         *                       sequence specified by the LoadLibrary function.
         * @param dwHandle This parameter is ignored.
         * @param dwLen The size, in bytes, of the buffer pointed to by the lpData parameter.
         * @param lpData Pointer to a buffer that receives the file-version information.
         * @return true If the function succeeds
         */
        boolean GetFileVersionInfoW(String lptstrFilename, int dwHandle,
                                           int dwLen, Pointer lpData);

        /**
         * Retrieves specified version information from the specified version-information resource.
         * To retrieve the appropriate resource, before you call VerQueryValue, you must first call the
         * {@link #GetFileVersionInfoSizeW(String, int)} function, and then the
         * {@link #GetFileVersionInfoW(String, int, int, com.sun.jna.Pointer)} function.
         * @param pBlock The version-information resource returned by the GetFileVersionInfo function.
         * @param lpSubBlock The version-information value to be retrieved.
         *                   The string must consist of names separated by backslashes (\) and it must have one of the following forms.
         * @param lplpBuffer When this method returns, contains the address of a pointer to the requested
         *                   version information in the buffer pointed to by pBlock.
         * @param puLen When this method returns, contains a pointer to the size of the requested data pointed to by lplpBuffer;
         *              for version information values, the length in characters of the string stored at lplpBuffer;
         *              for translation array values, the size in bytes of the array stored at lplpBuffer;
         *              and for root block, the size in bytes of the structure
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

        public VS_FIXEDFILEINFO(com.sun.jna.Pointer p){
            super(p);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("dwFileVersionMS", "dwFileVersionLS", "dwStrucVersion", "dwProductVersionMS", "dwProductVersionLS");
        }
    }

    private void fileInfo(Logger logger, File fileVSAX){

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
        if (!fileInfoResult){
            logger.error("Нет связанной с файлом информации.");
            return;
        }
        int verQueryVal = Version.INSTANCE.VerQueryValueW(lpData,
                "\\", lplpBuffer,
                puLen);
        if (verQueryVal == 0){
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
			while (zis.getNextEntry() != null){
				LOG.info("Xml copy, total number of bytes " + IOUtils.copy(zis, outFile));
			}
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(outFile);
			IOUtils.closeQuietly(zipXml);
		}
    }
}