CREATE USER &3 IDENTIFIED BY tax DEFAULT TABLESPACE NDFL_TEST TEMPORARY TABLESPACE NDFL_TEMP;

CREATE USER &1 IDENTIFIED BY &2 DEFAULT TABLESPACE NDFL_TEST TEMPORARY TABLESPACE NDFL_TEMP;

exit;