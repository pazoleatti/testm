--ПерсСвСтрахЛиц
INSERT INTO RASCHSV_PERS_SV_STRAH_LIC (ID, DECLARATION_DATA_ID, NOM_KORR, PERIOD, OTCHET_GOD, NOMER, SV_DATA, INNFL, SNILS, DATA_ROZD, GRAZD, POL, KOD_VID_DOC, SER_NOM_DOC, PRIZ_OPS, PRIZ_OMS, PRIZ_OSS, FAMILIA, IMYA, OTCHESTVO) VALUES (111, 1, 1, '01', '2016', 1, TO_DATE('01-01-1980', 'DD-MM-YYYY'), '111111111111', '11111111111111', TO_DATE('01-01-1980', 'DD-MM-YYYY'), 'RUS', '1', '09', '5555 555555', '1', '1', '1', 'Иванов', 'Иван', 'Иванович')
INSERT INTO RASCHSV_SV_VYPL (ID, SUM_VYPL_VS3, VYPL_OPS_VS3, VYPL_OPS_DOG_VS3, NACHISL_SV_VS3, RASCHSV_PERS_SV_STRAH_LIC_ID) VALUES (222, 2.2, 2.2, 2.2, 2.2, 111)
INSERT INTO RASCHSV_SV_VYPL (ID, SUM_VYPL_VS3, VYPL_OPS_VS3, VYPL_OPS_DOG_VS3, NACHISL_SV_VS3, RASCHSV_PERS_SV_STRAH_LIC_ID) VALUES (223, 2.2, 2.2, 2.2, 2.2, 111)
INSERT INTO RASCHSV_SV_VYPL_MK (ID, RASCHSV_SV_VYPL_ID, MESYAC, KOD_KAT_LIC, SUM_VYPL, VYPL_OPS, VYPL_OPS_DOG, NACHISL_SV) VALUES (333, 222, '03', '3333', 3.3, 3.3, 3.3, 3.3)
INSERT INTO RASCHSV_SV_VYPL_MK (ID, RASCHSV_SV_VYPL_ID, MESYAC, KOD_KAT_LIC, SUM_VYPL, VYPL_OPS, VYPL_OPS_DOG, NACHISL_SV) VALUES (777, 222, '07', '7777', 7.7, 7.7, 7.7, 7.7)
INSERT INTO RASCHSV_SV_VYPL_MK (ID, RASCHSV_SV_VYPL_ID, MESYAC, KOD_KAT_LIC, SUM_VYPL, VYPL_OPS, VYPL_OPS_DOG, NACHISL_SV) VALUES (778, 223, '07', '7777', 7.7, 7.7, 7.7, 7.7)
INSERT INTO RASCHSV_SV_VYPL_MK (ID, RASCHSV_SV_VYPL_ID, MESYAC, KOD_KAT_LIC, SUM_VYPL, VYPL_OPS, VYPL_OPS_DOG, NACHISL_SV) VALUES (779, 223, '07', '7777', 7.7, 7.7, 7.7, 7.7)
INSERT INTO RASCHSV_VYPL_SV_DOP (ID, VYPL_SV_VS3, NACHISL_SV_VS3, RASCHSV_PERS_SV_STRAH_LIC_ID) VALUES (444, 4.4, 4.4, 111)
INSERT INTO RASCHSV_VYPL_SV_DOP (ID, VYPL_SV_VS3, NACHISL_SV_VS3, RASCHSV_PERS_SV_STRAH_LIC_ID) VALUES (445, 4.4, 4.4, 111)
INSERT INTO RASCHSV_VYPL_SV_DOP_MT (ID, RASCHSV_VYPL_SV_DOP_ID, MESYAC, TARIF, VYPL_SV, NACHISL_SV) VALUES (555, 444, '05', '55', 5.5, 5.5)
INSERT INTO RASCHSV_VYPL_SV_DOP_MT (ID, RASCHSV_VYPL_SV_DOP_ID, MESYAC, TARIF, VYPL_SV, NACHISL_SV) VALUES (666, 444, '06', '66', 6.6, 6.6)
INSERT INTO RASCHSV_VYPL_SV_DOP_MT (ID, RASCHSV_VYPL_SV_DOP_ID, MESYAC, TARIF, VYPL_SV, NACHISL_SV) VALUES (667, 445, '06', '66', 6.6, 6.6)
INSERT INTO RASCHSV_VYPL_SV_DOP_MT (ID, RASCHSV_VYPL_SV_DOP_ID, MESYAC, TARIF, VYPL_SV, NACHISL_SV) VALUES (668, 445, '06', '66', 6.6, 6.6)

--СвНП и Подписант
INSERT INTO RASCHSV_SVNP_PODPISANT (ID, DECLARATION_DATA_ID, SVNP_OKVED, SVNP_TLPH, SVNP_NAIM_ORG, SVNP_INNYL, SVNP_KPP, SVNP_SV_REORG_FORM, SVNP_SV_REORG_INNYL, SVNP_SV_REORG_KPP, FAMILIA, IMYA, OTCHESTVO, PODPISANT_PR_PODP, PODPISANT_NAIM_DOC, PODPISANT_NAIM_ORG) VALUES (111, 1, '1111', '2222', '3333', '4444', '5555', '6', '6666', '7777', '8888', '9999', '0000', '0', 'AAAA', 'BBBB')

--ОбязПлатСВ
INSERT INTO RASCHSV_OBYAZ_PLAT_SV (ID, OKTMO, DECLARATION_DATA_ID) VALUES (111, '00000000000', 1)

--УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО
INSERT INTO RASCHSV_UPL_PER (ID, RASCHSV_OBYAZ_PLAT_SV_ID, NODE_NAME, KBK, SUM_SB_UPL_PER, SUM_SB_UPL_1M, SUM_SB_UPL_2M, SUM_SB_UPL_3M) VALUES (222, 111, '2222', '2222', 2.1, 2.2, 2.3, 2.4)

--УплПревОСС
INSERT INTO RASCHSV_UPL_PREV_OSS (ID, RASCHSV_OBYAZ_PLAT_SV_ID, KBK, SUM_SB_UPL_PER, SUM_SB_UPL_1M, SUM_SB_UPL_2M, SUM_SB_UPL_3M, PREV_RASH_SV_PER, PREV_RASH_SV_1M, PREV_RASH_SV_2M, PREV_RASH_SV_3M) VALUES (333, 111, '3333', 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8)

--СвСум1Тип
INSERT INTO RASCHSV_SV_SUM_1TIP (ID, SUM_VSEGO_PER, SUM_VSEGO_POSL_3M, SUM_1M_POSL_3M, SUM_2M_POSL_3M, SUM_3M_POSL_3M) VALUES (444, 4.1, 4.2, 4.3, 4.4, 4.5)
INSERT INTO RASCHSV_SV_SUM_1TIP (ID, SUM_VSEGO_PER, SUM_VSEGO_POSL_3M, SUM_1M_POSL_3M, SUM_2M_POSL_3M, SUM_3M_POSL_3M) VALUES (555, 5.1, 5.2, 5.3, 5.4, 5.5)
INSERT INTO RASCHSV_SV_SUM_1TIP (ID, SUM_VSEGO_PER, SUM_VSEGO_POSL_3M, SUM_1M_POSL_3M, SUM_2M_POSL_3M, SUM_3M_POSL_3M) VALUES (666, 6.1, 6.2, 6.3, 6.4, 6.5)
INSERT INTO RASCHSV_SV_SUM_1TIP (ID, SUM_VSEGO_PER, SUM_VSEGO_POSL_3M, SUM_1M_POSL_3M, SUM_2M_POSL_3M, SUM_3M_POSL_3M) VALUES (777, 7.1, 7.2, 7.3, 7.4, 7.5)
INSERT INTO RASCHSV_SV_SUM_1TIP (ID, SUM_VSEGO_PER, SUM_VSEGO_POSL_3M, SUM_1M_POSL_3M, SUM_2M_POSL_3M, SUM_3M_POSL_3M) VALUES (888, 8.1, 8.2, 8.3, 8.4, 8.5)
INSERT INTO RASCHSV_SV_SUM_1TIP (ID, SUM_VSEGO_PER, SUM_VSEGO_POSL_3M, SUM_1M_POSL_3M, SUM_2M_POSL_3M, SUM_3M_POSL_3M) VALUES (999, 9.1, 9.2, 9.3, 9.4, 9.5)

--КолЛицТип
INSERT INTO RASCHSV_KOL_LIC_TIP (ID, KOL_VSEGO_PER, KOL_VSEGO_POSL_3M, KOL_1M_POSL_3M, KOL_2M_POSL_3M, KOL_3M_POSL_3M) VALUES (111, 111, 112, 113, 114, 115)
INSERT INTO RASCHSV_KOL_LIC_TIP (ID, KOL_VSEGO_PER, KOL_VSEGO_POSL_3M, KOL_1M_POSL_3M, KOL_2M_POSL_3M, KOL_3M_POSL_3M) VALUES (222, 221, 222, 223, 224, 225)

--РасчСВ_ОПС_ОМС
INSERT INTO RASCHSV_SV_OPS_OMS (ID, RASCHSV_OBYAZ_PLAT_SV_ID, TARIF_PLAT) VALUES (444, 111, '44')
INSERT INTO RASCHSV_SV_OPS_OMS_RASCH (ID, RASCHSV_SV_OPS_OMS_ID, NODE_NAME, PR_OSN_SV_DOP, KOD_OSNOV, OSNOV_ZAP, KLAS_USL_TRUD, PR_RASCH_SUM) VALUES (555, 444, '5555', '1', '2', '3', '4', '5')
INSERT INTO RASCHSV_SV_OPS_OMS_RASCH (ID, RASCHSV_SV_OPS_OMS_ID, NODE_NAME, PR_OSN_SV_DOP, KOD_OSNOV, OSNOV_ZAP, KLAS_USL_TRUD, PR_RASCH_SUM) VALUES (666, 444, '6666', '6', '7', '8', '9', '0')
INSERT INTO RASCHSV_OPS_OMS_RASCH_SUM (RASCHSV_OPS_OMS_RASCH_SUM_ID, RASCHSV_SV_SUM1_TIP_ID, NODE_NAME) VALUES (555, 777, '5757')
INSERT INTO RASCHSV_OPS_OMS_RASCH_SUM (RASCHSV_OPS_OMS_RASCH_SUM_ID, RASCHSV_SV_SUM1_TIP_ID, NODE_NAME) VALUES (666, 888, '5757')
INSERT INTO RASCHSV_OPS_OMS_RASCH_KOL (RASCHSV_OPS_OMS_RASCH_KOL_ID, RASCHSV_KOL_LIC_TIP_ID, NODE_NAME) VALUES (555, 111, '111')
INSERT INTO RASCHSV_OPS_OMS_RASCH_KOL (RASCHSV_OPS_OMS_RASCH_KOL_ID, RASCHSV_KOL_LIC_TIP_ID, NODE_NAME) VALUES (666, 222, '222')

--РасчСВ_ОСС.ВНМ
INSERT INTO RASCHSV_OSS_VNM (ID, RASCHSV_OBYAZ_PLAT_SV_ID, PRIZ_VYPL) VALUES (111, 111, '1')
INSERT INTO RASCHSV_UPL_SV_PREV (ID, RASCHSV_OSS_VNM_ID, NODE_NAME, PRIZNAK, SV_SUM) VALUES (222, 111, '2222', '2', 2.2)
INSERT INTO RASCHSV_UPL_SV_PREV (ID, RASCHSV_OSS_VNM_ID, NODE_NAME, PRIZNAK, SV_SUM) VALUES (223, 111, '3333', '3', 2.3)
INSERT INTO RASCHSV_OSS_VNM_SUM (RASCHSV_OSS_VNM_ID, RASCHSV_SV_SUM1_TIP_ID, NODE_NAME) VALUES (111, 777, '7777')
INSERT INTO RASCHSV_OSS_VNM_SUM (RASCHSV_OSS_VNM_ID, RASCHSV_SV_SUM1_TIP_ID, NODE_NAME) VALUES (111, 888, '8888')
INSERT INTO RASCHSV_OSS_VNM_KOL (RASCHSV_OSS_VNM_ID, RASCHSV_KOL_LIC_TIP_ID, NODE_NAME) VALUES (111, 111, '1111')
INSERT INTO RASCHSV_OSS_VNM_KOL (RASCHSV_OSS_VNM_ID, RASCHSV_KOL_LIC_TIP_ID, NODE_NAME) VALUES (111, 222, '2222')

--РасхОССЗак
INSERT INTO RASCHSV_RASH_OSS_ZAK (ID, RASCHSV_OBYAZ_PLAT_SV_ID) VALUES (333, 111)
INSERT INTO RASCHSV_RASH_OSS_ZAK_RASH (ID, RASCHSV_RASH_OSS_ZAK_ID, NODE_NAME, CHISL_SLUCH, KOL_VYPL, RASH_VSEGO, RASH_FIN_FB) VALUES (444, 333, '4444', 4, 4, 4.4, 4.4)
INSERT INTO RASCHSV_RASH_OSS_ZAK_RASH (ID, RASCHSV_RASH_OSS_ZAK_ID, NODE_NAME, CHISL_SLUCH, KOL_VYPL, RASH_VSEGO, RASH_FIN_FB) VALUES (555, 333, '5555', 5, 5, 5.5, 5.5)

--ВыплФинФБ
INSERT INTO RASCHSV_VYPL_FIN_FB (ID, RASCHSV_OBYAZ_PLAT_SV_ID) VALUES (666, 111)
INSERT INTO RASCHSV_VYPL_PRICHINA (ID, RASCHSV_VYPL_FIN_FB_ID, NODE_NAME, SV_VNF_UHOD_INV) VALUES (777, 666, '7777', 7.1)
INSERT INTO RASCHSV_VYPL_PRICHINA (ID, RASCHSV_VYPL_FIN_FB_ID, NODE_NAME, SV_VNF_UHOD_INV) VALUES (888, 666, '888', 8.1)
INSERT INTO RASCHSV_RASH_VYPL (ID, RASCHSV_VYPL_PRICHINA_ID, NODE_NAME, CHISL_POLUCH, KOL_VYPL, RASHOD) VALUES (111, 777, '1111', 1, 1, 1.1)
INSERT INTO RASCHSV_RASH_VYPL (ID, RASCHSV_VYPL_PRICHINA_ID, NODE_NAME, CHISL_POLUCH, KOL_VYPL, RASHOD) VALUES (222, 888, '2222', 2, 2, 2.2)

--ПравТариф3.1.427
INSERT INTO RASCHSV_PRAV_TARIF3_1_427 (ID, RASCHSV_OBYAZ_PLAT_SV_ID, SR_CHISL_9MPR, SR_CHISL_PER, DOH248_9MPR, DOH248_PER, DOH_KR5_427_9MPR, DOH_KR5_427_PER, DOH_DOH5_427_9MPR, DOH_DOH5_427_PER, DATA_ZAP_AK_ORG, NOM_ZAP_AK_ORG) VALUES (333, 111, 3, 3, 3, 3, 3, 3, 3.3, 3.3, TO_DATE('01-01-1980', 'DD-MM-YYYY'), '3333')

--ПравТариф5.1.427
INSERT INTO RASCHSV_PRAV_TARIF5_1_427 (ID, RASCHSV_OBYAZ_PLAT_SV_ID, DOH346_15VS, DOH6_427, DOL_DOH6_427) VALUES (444, 111, 444, 444, 4.4)

--ПравТариф7.1.427
INSERT INTO RASCHSV_PRAV_TARIF7_1_427 (ID, RASCHSV_OBYAZ_PLAT_SV_ID, DOH_VS_PRED, DOH_VS_PER, DOH_CEL_POST_PRED, DOH_CEL_POST_PER, DOH_GRANT_PRED, DOH_GRANT_PER, DOH_EK_DEYAT_PRED, DOH_EK_DEYAT_PER, DOL_DOH_PRED, DOL_DOH_PER) VALUES (555, 111, 555, 555, 555, 555, 555, 555, 555, 555, 5.5, 5.5)

--СвПримТариф9.1.427
INSERT INTO RASCHSV_SV_PRIM_TARIF9_1_427 (ID, RASCHSV_OBYAZ_PLAT_SV_ID) VALUES (666, 111)
INSERT INTO RASCHSV_VYPLAT_IT_427 (RASCHSV_SV_PRIM_TARIF9_427_ID, RASCHSV_SV_SUM1_TIP_ID) VALUES (666, 444)
INSERT INTO RASCHSV_SVED_PATENT (RASCHSV_SV_PRIM_TARIF9_427_ID, RASCHSV_SV_SUM1_TIP_ID, NOM_PATENT, VYD_DEYAT_PATENT, DATA_NACH_DEYST, DATA_KON_DEYST) VALUES (666, 555, '6666', '6666', TO_DATE('01-01-1980', 'DD-MM-YYYY'), TO_DATE('01-01-1980', 'DD-MM-YYYY'))

--СвПримТариф2.2.425
INSERT INTO RASCHSV_SV_PRIM_TARIF2_2_425 (ID, RASCHSV_OBYAZ_PLAT_SV_ID) VALUES (555, 111)
INSERT INTO RASCHSV_VYPLAT_IT_425 (RASCHSV_SV_PRIM_TARIF2_425_ID, RASCHSV_SV_SUM1_TIP_ID) VALUES (555, 666)
INSERT INTO RASCHSV_SV_INO_GRAZD (RASCHSV_SV_PRIM_TARIF2_425_ID, RASCHSV_SV_SUM1_TIP_ID, INNFL, SNILS, GRAZD, FAMILIA, IMYA, OTCHESTVO) VALUES (555, 777, '5551', '5552', 'RUS', 'IVANOV', 'IVAN', 'IVANOVICH')

--СвПримТариф1.3.422
INSERT INTO RASCHSV_SV_PRIM_TARIF1_3_422 (ID, RASCHSV_OBYAZ_PLAT_SV_ID) VALUES (111, 111)
INSERT INTO RASCHSV_VYPLAT_IT_422 (RASCHSV_SV_PRIM_TARIF1_422_ID, RASCHSV_SV_SUM1_TIP_ID) VALUES (111, 888)
INSERT INTO RASCHSV_SVED_OBUCH (ID, RASCHSV_SV_PRIM_TARIF1_422_ID, RASCHSV_SV_SUM1_TIP_ID, UNIK_NOMER, FAMILIA, IMYA, OTCHESTVO, SPRAV_NOMER, SPRAV_DATA, SPRAV_NODE_NAME) VALUES (222, 111, 999, '222', '2222', '2222', '2222', '2222', TO_DATE('01-01-1980', 'DD-MM-YYYY'), '2222')
INSERT INTO RASCHSV_SV_REESTR_MDO (ID, RASCHSV_SVED_OBUCH_ID, NAIM_MDO, NOMER_ZAPIS, DATA_ZAPIS) VALUES (333, 222, '3333', '3333', TO_DATE('01-01-1980', 'DD-MM-YYYY'))