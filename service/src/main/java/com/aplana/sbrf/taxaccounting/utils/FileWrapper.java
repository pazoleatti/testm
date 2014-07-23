package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import java.io.*;

/**
 * Обертка для работы с файлами через протокол smb и файловую систему
 * @author dloshkarev
 */
public class FileWrapper {
    private File file = null;
    private SmbFile smbFile = null;

    private static String ERROR_TYPE_SMB = "Ошибка получения типа ресурса через протокол smb!";
    private static String ERROR_TYPE = "Ошибка получения типа ресурса. Ресурс не проинициализирован!";
    private static String ERROR_RESOURCE = "Ошибка получения ресурса. Ресурс не найден!";
    private static String ERROR_RESOURCE_SMB = "Ошибка получения ресурса через протокол smb!";
    private static String ERROR_RESOURCE_INIT = "Ошибка получения ресурса. Ресурс не проинициализирован!";
    private static String ERROR_LIST_SMB = "Ошибка получения списка файлов через протокол smb!";
    private static String ERROR_LIST_INIT = "Ошибка получения списка файлов. Ресурс не проинициализирован!";
    private static String ERROR_DELETE = "Не удалось удалить файл!";

    public FileWrapper(File file) {
        this.file = file;
    }
    
    public FileWrapper(SmbFile smbFile) {
        this.smbFile = smbFile;
    }

    public boolean canWrite() {
        if (file != null) {
            return file.canWrite();
        }
        if (smbFile != null) {
            try {
                return smbFile.canWrite();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_RESOURCE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    public boolean canRead() {
        if (file != null) {
            return file.canRead();
        }
        if (smbFile != null) {
            try {
                return smbFile.canRead();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_RESOURCE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    public String getPath() {
        if (file != null) {
            return file.getPath();
        }
        if (smbFile != null) {
            return smbFile.getPath();
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    public String getName() {
        if (file != null) {
            return file.getName();
        }
        if (smbFile != null) {
            return smbFile.getName();
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    public void mkDirs() {
        if (file != null) {
            file.mkdirs();
            return;
        }
        if (smbFile != null) {
            try {
                smbFile.mkdirs();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_RESOURCE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    public String[] list() {
        if (file != null) {
            return file.list();
        }
        if (smbFile != null) {
            try {
                return smbFile.list();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_LIST_SMB, e);
            }
        }
        throw new ServiceException(ERROR_LIST_INIT);
    }

    public void delete() {
        if (file != null) {
            if (!file.delete()) {
                throw new ServiceException(ERROR_DELETE);
            }
            return;
        }
        if (smbFile != null) {
            try {
                smbFile.delete();
                return;
            } catch (SmbException e) {
                throw new ServiceException(ERROR_RESOURCE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_LIST_INIT);
    }

    public boolean isFile() {
        if (file != null) {
            return file.isFile();
        }
        if (smbFile != null) {
            try {
                return smbFile.isFile();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_TYPE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_TYPE);
    }

    public boolean exists() {
        if (file != null) {
            return file.exists();
        }
        if (smbFile != null) {
            try {
                return smbFile.exists();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_TYPE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_TYPE);
    }

    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }
        if (smbFile != null) {
            try {
                return smbFile.isDirectory();
            } catch (SmbException e) {
                throw new ServiceException(ERROR_TYPE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_TYPE);
    }

    public InputStream getInputStream() {
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ServiceException(ERROR_RESOURCE, e);
            }
        }
        if (smbFile != null) {
            try {
                return smbFile.getInputStream();
            } catch (IOException e) {
                throw new ServiceException(ERROR_RESOURCE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    public OutputStream getOutputStream() {
        if (file != null) {
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new ServiceException(ERROR_RESOURCE, e);
            }
        }
        if (smbFile != null) {
            try {
                return new SmbFileOutputStream(smbFile);
            } catch (IOException e) {
                throw new ServiceException(ERROR_RESOURCE_SMB, e);
            }
        }
        throw new ServiceException(ERROR_RESOURCE_INIT);
    }

    /**
     * Проверка возможности чтения директории
     */
    public static boolean canReadFolder(String path) {
        return checkAccess(path, false, true);
    }

    /**
     * Проверка возможности чтения файла
     */
    public static boolean canReadFile(String path) {
        return checkAccess(path, false, false);
    }

    /**
     * Проверка возможности записи директории
     */
    public static boolean canWriteFolder(String path) {
        return checkAccess(path, true, true);
    }

    /**
     * Проверка возможности записи файла
     */
    public static boolean canWriteFile(String path) {
        return checkAccess(path, true, false);
    }

    /**
     * Проверка возможности записи/чтения файла/директории
     */
    private static boolean checkAccess(String path, boolean write, boolean folder) {
        try {
            FileWrapper folderOrFile = ResourceUtils.getSharedResource(path);
            if (folder && folderOrFile.isFile()
                    || !folder && folderOrFile.isDirectory()
                    || write && !folderOrFile.canWrite()
                    || !write && !folderOrFile.canRead()) {
                return false;
            }
        } catch (ServiceException e) {
            return false;
        }
        return true;
    }
}
