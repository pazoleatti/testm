INSERT INTO RASCHSV_PERS_SV_STRAH_LIC (ID, DECLARATION_DATA_ID, NOM_KORR, PERIOD, OTCHET_GOD, NOMER, SV_DATA, INNFL, SNILS, DATA_ROZD, GRAZD, POL, KOD_VID_DOC, SER_NOM_DOC, PRIZ_OPS, PRIZ_OMS, PRIZ_OSS, FAMILIA, IMYA, MIDDLE_NAME) VALUES (111, 1, 1, '01', '2016', 1, TO_DATE('01-01-1980', 'DD-MM-YYYY'), '111111111111', '11111111111111', TO_DATE('01-01-1980', 'DD-MM-YYYY'), 'RUS', '1', '09', '5555 555555', '1', '1', '1', 'Иванов', 'Иван', 'Иванович')
INSERT INTO RASCHSV_SV_VYPL (ID, SUM_VYPL_VS3, VYPL_OPS_VS3, VYPL_OPS_DOG_VS3, NACHISL_SV_VS3, RASCHSV_PERS_SV_STRAH_LIC_ID) VALUES (222, 2.2, 2.2, 2.2, 2.2, 111)
INSERT INTO RASCHSV_SV_VYPL_MK (ID, RASCHSV_SV_VYPL_ID, MESYAC, KOD_KAT_LIC, SUM_VYPL, VYPL_OPS, VYPL_OPS_DOG, NACHISL_SV) VALUES (333, 222, '03', '3333', 3.3, 3.3, 3.3, 3.3)
INSERT INTO RASCHSV_SV_VYPL_MK (ID, RASCHSV_SV_VYPL_ID, MESYAC, KOD_KAT_LIC, SUM_VYPL, VYPL_OPS, VYPL_OPS_DOG, NACHISL_SV) VALUES (777, 222, '07', '7777', 7.7, 7.7, 7.7, 7.7)
INSERT INTO RASCHSV_VYPL_SV_DOP (ID, VYPL_SV_VS3, NACHISL_SV_VS3, RASCHSV_PERS_SV_STRAH_LIC_ID) VALUES (444, 4.4, 4.4, 111)
INSERT INTO RASCHSV_VYPL_SV_DOP_MT (ID, RASCHSV_VYPL_SV_DOP_ID, MESYAC, TARIF, VYPL_SV, NACHISL_SV) VALUES (555, 444, '05', '55', 5.5, 5.5)
INSERT INTO RASCHSV_VYPL_SV_DOP_MT (ID, RASCHSV_VYPL_SV_DOP_ID, MESYAC, TARIF, VYPL_SV, NACHISL_SV) VALUES (666, 444, '06', '66', 6.6, 6.6)