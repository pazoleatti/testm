package com.aplana.sbrf.taxaccounting.utils;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import jcifs.smb.SmbFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.regex.Pattern;

/**
 * Утилитный класс для получения удаленных ресурсов
 * @author dloshkarev
 */
public final class ResourceUtils {

    private ResourceUtils() {}

    public static FileWrapper getSharedResource(String destinationUri) {
        return getSharedResource(destinationUri, true);
    }

    public static FileWrapper getSharedResource(String destinationUri, boolean checkExist) {
        try {
            if (destinationUri.startsWith("smb:")) {
                String checkedUri = Pattern.compile("smb:/*").matcher(destinationUri).replaceFirst("smb://");
                return new FileWrapper(new SmbFile(checkedUri));
            } else {
                if (!destinationUri.startsWith("file:")) {
                    destinationUri = "file:///" + destinationUri ;
                }

                URL url = new URL(destinationUri);
                URI uri = null;
                try {
                    uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    if(uri.getAuthority() != null && !uri.getAuthority().isEmpty()) {
                        // Hack for UNC Path
                        url = new URL("file://" + destinationUri.substring("file:".length()));
                        uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    }
                } catch (URISyntaxException e) {
                    throw new ServiceException("Ошибка при попытке доступа к ресурсу.");
                }

                File file = new File(uri);
                if (!checkExist || file.exists()) {
                    return new FileWrapper(file);
                } else {
                    throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права.");
                }
            }
        } catch (MalformedURLException e) {
            throw new ServiceException("Неправильный формат URL до ресурса", e);
        } catch (IOException e) {
            throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права.", e);
        }
    }

    public static InputStream getSharedResourceAsStream(String destinationUri) {
        try {
            if (destinationUri.startsWith("smb:")) {
                String checkedUri = Pattern.compile("smb:/*").matcher(destinationUri).replaceFirst("smb://");
                SmbFile file = new SmbFile(checkedUri);
                return file.getInputStream();
            } else {
                if (!destinationUri.startsWith("file:")) {
                    destinationUri = "file:///" + destinationUri ;
                }

                URL url = new URL(destinationUri);
                URI uri = null;
                try {
                    uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    if(uri.getAuthority() != null && !uri.getAuthority().isEmpty()) {
                        // Hack for UNC Path
                        url = new URL("file://" + destinationUri.substring("file:".length()));
                        uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                    }
                } catch (URISyntaxException e) {
                    throw new ServiceException("Ошибка при попытке доступа к ресурсу.");
                }

                File file = new File(uri);

                return new FileInputStream(file);
            }
        } catch (MalformedURLException e) {
            throw new ServiceException("Неправильный формат URL до ресурса.", e);
        } catch (IOException e) {
            throw new ServiceException("Запрашиваемый ресурс не найден либо отсутствуют необходимые права.", e);
        }
    }
}
