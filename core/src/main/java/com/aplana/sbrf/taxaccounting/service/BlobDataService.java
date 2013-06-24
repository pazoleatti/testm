package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;

/**
 * User: avanteev
 */
public interface BlobDataService {
    String create(InputStream is, String name);
    String createTemporary(InputStream is, String name);
    void delete(String blob_id);
    void save(String blob_id, InputStream is);
    InputStream get(String blob_id);
}
