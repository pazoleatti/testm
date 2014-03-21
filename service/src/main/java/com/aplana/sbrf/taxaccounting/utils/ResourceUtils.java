package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import jcifs.smb.SmbFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитный класс для получения удаленных ресурсов
 * @author dloshkarev
 */
public class ResourceUtils {

    public static FileWrapper getSharedResource(String uri) {
        try {
            if (uri.startsWith("smb:")) {
                String checkedUri = Pattern.compile("smb:/*").matcher(uri).replaceFirst("smb://");
                return new FileWrapper(new SmbFile(checkedUri));
            } else {
                if (!uri.startsWith("file:")) {
                    uri = "file:///" + uri ;
                }
                String checkedUri = Pattern.compile("file:/*").matcher(uri).replaceFirst("file:///");
                URL url = new URL(checkedUri);
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
                String checkedUri = Pattern.compile("smb:/*").matcher(uri).replaceFirst("smb://");
                SmbFile file = new SmbFile(checkedUri);
                return file.getInputStream();
            } else {
                if (!uri.startsWith("file:")) {
                    uri = "file:///" + uri ;
                }
                String checkedUri = Pattern.compile("file:/*").matcher(uri).replaceFirst("file:///");
                URL url = new URL(checkedUri);
                return url.openStream();
            }
        } catch (MalformedURLException e) {
            throw new ServiceException("Неправильный формат URL до ресурса", e);
        } catch (IOException e) {
            throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права", e);
        }
    }
}
