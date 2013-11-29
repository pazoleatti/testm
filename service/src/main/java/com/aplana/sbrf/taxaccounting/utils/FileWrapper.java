package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import java.io.*;

/**
 * Обертка для работы с файлами через протокол smb и файловую систему
 * @author dloshkarev
 */
public class FileWrapper {
    private File file = null;
    private SmbFile smbFile = null;

    private FileWrapper() {}

    public FileWrapper(File file) {
        this.file = file;
    }
    
    public FileWrapper(SmbFile smbFile) {
        this.smbFile = smbFile;
    }

    public String[] list() {
        if (file != null) {
            return file.list();
        }
        if (smbFile != null) {
            try {
                return smbFile.list();
            } catch (SmbException e) {
                throw new ServiceException("Ошибка получения списка файлов через протокол smb", e);
            }
        }
        throw new ServiceException("Ошибка получения списка файлов. Ресурс не проинициализирован");
    }

    public boolean isFile() {
        if (file != null) {
            return file.isFile();
        }
        if (smbFile != null) {
            try {
                return smbFile.isFile();
            } catch (SmbException e) {
                throw new ServiceException("Ошибка получения типа ресурса через протокол smb", e);
            }
        }
        throw new ServiceException("Ошибка получения типа ресурса. Ресурс не проинициализирован");
    }

    public InputStream getStream() {
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ServiceException("Ошибка получения ресурса. Ресурс не найден", e);
            }
        }
        if (smbFile != null) {
            try {
                return smbFile.getInputStream();
            } catch (IOException e) {
                throw new ServiceException("Ошибка получения ресурса через протокол smb", e);
            }
        }
        throw new ServiceException("Ошибка получения ресурса. Ресурс не проинициализирован");
    }
}
