package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import jcifs.smb.SmbFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Утилитный класс для получения удаленных ресурсов
 * @author dloshkarev
 */
public class ResourceUtils {

    public static FileWrapper getSharedResource(String uri) {
        try {
            if (uri.startsWith("smb:")) {
                return new FileWrapper(new SmbFile(uri));
            } else {
                if (!uri.startsWith("file:")) {
                    uri = "file:" + uri ;
                }
                URL url = new URL(uri);
                File file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
                if (file.exists()) {
                    return new FileWrapper(file);
                } else {
                    throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права");
                }
            }
        } catch (MalformedURLException e) {
            throw new ServiceException("Неправильный формат URL до ресурса", e);
        } catch (IOException e) {
            throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права", e);
        }
    }

    public static InputStream getSharedResourceAsStream(String uri) {
        try {
            if (uri.startsWith("smb:")) {
                SmbFile file = new SmbFile(uri);
                return file.getInputStream();
            } else {
                URL url = new URL(uri);
                return url.openStream();
            }
        } catch (MalformedURLException e) {
            throw new ServiceException("Неправильный формат URL до ресурса", e);
        } catch (IOException e) {
            throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права", e);
        }
    }
}
