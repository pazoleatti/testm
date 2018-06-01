package com.aplana.sbrf.taxaccounting.model.util;

import java.util.Comparator;

public class DeclarationStringComparator implements Comparator<String> {

    public static final DeclarationStringComparator INSTANCE = new DeclarationStringComparator();

    private DeclarationStringComparator() {
    }

    @Override
    public int compare(String o1, String o2) {
        char v1[] = o1.toCharArray();
        char v2[] = o2.toCharArray();
        int len1 = v1.length;
        int len2 = v2.length;
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            int i1 = reorder((int) c1);
            int i2 = reorder((int) c2);
            if (i1 != i2) {
                return i1 - i2;
            }
            k++;
        }
        return len1 - len2;
    }

    private int reorder(int i) {
        int toReturn = i;
        switch (i) {
            case (1072): {
                toReturn = 1041;
                break;
            }
            case (1073): {
                toReturn = 1043;
                break;
            }
            case (1074): {
                toReturn = 1045;
                break;
            }
            case (1075): {
                toReturn = 1047;
                break;
            }
            case (1076): {
                toReturn = 1049;
                break;
            }
            case (1077): {
                toReturn = 1051;
                break;
            }
            case (1078): {
                toReturn = 1055;
                break;
            }
            case (1079): {
                toReturn = 1057;
                break;
            }
            case (1080): {
                toReturn = 1059;
                break;
            }
            case (1081): {
                toReturn = 1061;
                break;
            }
            case (1082): {
                toReturn = 1063;
                break;
            }
            case (1083): {
                toReturn = 1065;
                break;
            }
            case (1084): {
                toReturn = 1067;
                break;
            }
            case (1085): {
                toReturn = 1069;
                break;
            }
            case (1086): {
                toReturn = 1071;
                break;
            }
            case (1087): {
                toReturn = 1073;
                break;
            }
            case (1088): {
                toReturn = 1075;
                break;
            }
            case (1089): {
                toReturn = 1077;
                break;
            }
            case (1090): {
                toReturn = 1079;
                break;
            }
            case (1091): {
                toReturn = 1081;
                break;
            }
            case (1092): {
                toReturn = 1083;
                break;
            }
            case (1093): {
                toReturn = 1085;
                break;
            }
            case (1094): {
                toReturn = 1087;
                break;
            }
            case (1095): {
                toReturn = 1089;
                break;
            }
            case (1096): {
                toReturn = 1091;
                break;
            }
            case (1097): {
                toReturn = 1093;
                break;
            }
            case (1098): {
                toReturn = 1095;
                break;
            }
            case (1099): {
                toReturn = 1097;
                break;
            }
            case (1100): {
                toReturn = 1099;
                break;
            }
            case (1101): {
                toReturn = 1101;
                break;
            }
            case (1102): {
                toReturn = 1103;
                break;
            }
            case (1103): {
                toReturn = 1105;
                break;
            }
            case (1041): {
                toReturn = 1042;
                break;
            }
            case (1042): {
                toReturn = 1044;
                break;
            }
            case (1043): {
                toReturn = 1046;
                break;
            }
            case (1044): {
                toReturn = 1048;
                break;
            }
            case (1045): {
                toReturn = 1050;
                break;
            }
            case (1046): {
                toReturn = 1054;
                break;
            }
            case (1047): {
                toReturn = 1056;
                break;
            }
            case (1048): {
                toReturn = 1058;
                break;
            }
            case (1049): {
                toReturn = 1060;
                break;
            }
            case (1050): {
                toReturn = 1062;
                break;
            }
            case (1051): {
                toReturn = 1064;
                break;
            }
            case (1052): {
                toReturn = 1066;
                break;
            }
            case (1053): {
                toReturn = 1068;
                break;
            }
            case (1054): {
                toReturn = 1070;
                break;
            }
            case (1055): {
                toReturn = 1072;
                break;
            }
            case (1056): {
                toReturn = 1074;
                break;
            }
            case (1057): {
                toReturn = 1076;
                break;
            }
            case (1058): {
                toReturn = 1078;
                break;
            }
            case (1059): {
                toReturn = 1080;
                break;
            }
            case (1060): {
                toReturn = 1082;
                break;
            }
            case (1061): {
                toReturn = 1084;
                break;
            }
            case (1062): {
                toReturn = 1086;
                break;
            }
            case (1063): {
                toReturn = 1088;
                break;
            }
            case (1064): {
                toReturn = 1090;
                break;
            }
            case (1065): {
                toReturn = 1092;
                break;
            }
            case (1066): {
                toReturn = 1094;
                break;
            }
            case (1067): {
                toReturn = 1096;
                break;
            }
            case (1068): {
                toReturn = 1098;
                break;
            }
            case (1069): {
                toReturn = 1100;
                break;
            }
            case (1070): {
                toReturn = 1102;
                break;
            }
            case (1071): {
                toReturn = 1104;
                break;
            }
            case (1025): {
                toReturn = 1052;
                break;
            }
            case (1105): {
                toReturn = 1053;
                break;
            }
            case (33): {
                toReturn = 34;
                break;
            }
            case (34): {
                toReturn = 35;
                break;
            }
            case (35): {
                toReturn = 36;
                break;
            }
            case (36): {
                toReturn = 37;
                break;
            }
            case (37): {
                toReturn = 38;
                break;
            }
            case (38): {
                toReturn = 39;
                break;
            }
            case (39): {
                toReturn = 40;
                break;
            }
            case (40): {
                toReturn = 41;
                break;
            }
            case (41): {
                toReturn = 42;
                break;
            }
            case (42): {
                toReturn = 43;
                break;
            }
            case (43): {
                toReturn = 44;
                break;
            }
            case (44): {
                toReturn = 45;
                break;
            }
            case (45): {
                toReturn = 46;
                break;
            }
            case (46): {
                toReturn = 47;
                break;
            }
            case (47): {
                toReturn = 48;
                break;
            }
            case (48): {
                toReturn = 49;
                break;
            }
            case (49): {
                toReturn = 50;
                break;
            }
            case (50): {
                toReturn = 51;
                break;
            }
            case (51): {
                toReturn = 52;
                break;
            }
            case (52): {
                toReturn = 53;
                break;
            }
            case (53): {
                toReturn = 54;
                break;
            }
            case (54): {
                toReturn = 55;
                break;
            }
            case (55): {
                toReturn = 56;
                break;
            }
            case (56): {
                toReturn = 57;
                break;
            }
            case (57): {
                toReturn = 58;
                break;
            }
            case (58): {
                toReturn = 59;
                break;
            }
            case (59): {
                toReturn = 60;
                break;
            }
            case (60): {
                toReturn = 61;
                break;
            }
            case (61): {
                toReturn = 62;
                break;
            }
            case (62): {
                toReturn = 63;
                break;
            }
            case (63): {
                toReturn = 64;
                break;
            }
            case (64): {
                toReturn = 65;
                break;
            }
            case (65): {
                toReturn = 78;
                break;
            }
            case (66): {
                toReturn = 80;
                break;
            }
            case (67): {
                toReturn = 82;
                break;
            }
            case (68): {
                toReturn = 84;
                break;
            }
            case (69): {
                toReturn = 86;
                break;
            }
            case (70): {
                toReturn = 88;
                break;
            }
            case (71): {
                toReturn = 90;
                break;
            }
            case (72): {
                toReturn = 92;
                break;
            }
            case (73): {
                toReturn = 94;
                break;
            }
            case (74): {
                toReturn = 96;
                break;
            }
            case (75): {
                toReturn = 98;
                break;
            }
            case (76): {
                toReturn = 100;
                break;
            }
            case (77): {
                toReturn = 102;
                break;
            }
            case (78): {
                toReturn = 104;
                break;
            }
            case (79): {
                toReturn = 106;
                break;
            }
            case (80): {
                toReturn = 108;
                break;
            }
            case (81): {
                toReturn = 110;
                break;
            }
            case (82): {
                toReturn = 112;
                break;
            }
            case (83): {
                toReturn = 114;
                break;
            }
            case (84): {
                toReturn = 116;
                break;
            }
            case (85): {
                toReturn = 118;
                break;
            }
            case (86): {
                toReturn = 120;
                break;
            }
            case (87): {
                toReturn = 122;
                break;
            }
            case (88): {
                toReturn = 124;
                break;
            }
            case (89): {
                toReturn = 126;
                break;
            }
            case (90): {
                toReturn = 128;
                break;
            }
            case (91): {
                toReturn = 66;
                break;
            }
            case (92): {
                toReturn = 67;
                break;
            }
            case (93): {
                toReturn = 68;
                break;
            }
            case (94): {
                toReturn = 69;
                break;
            }
            case (95): {
                toReturn = 70;
                break;
            }
            case (96): {
                toReturn = 71;
                break;
            }
            case (97): {
                toReturn = 79;
                break;
            }
            case (98): {
                toReturn = 81;
                break;
            }
            case (99): {
                toReturn = 83;
                break;
            }
            case (100): {
                toReturn = 85;
                break;
            }
            case (101): {
                toReturn = 87;
                break;
            }
            case (102): {
                toReturn = 89;
                break;
            }
            case (103): {
                toReturn = 91;
                break;
            }
            case (104): {
                toReturn = 93;
                break;
            }
            case (105): {
                toReturn = 95;
                break;
            }
            case (106): {
                toReturn = 97;
                break;
            }
            case (107): {
                toReturn = 99;
                break;
            }
            case (108): {
                toReturn = 101;
                break;
            }
            case (109): {
                toReturn = 103;
                break;
            }
            case (110): {
                toReturn = 105;
                break;
            }
            case (111): {
                toReturn = 107;
                break;
            }
            case (112): {
                toReturn = 109;
                break;
            }
            case (113): {
                toReturn = 111;
                break;
            }
            case (114): {
                toReturn = 113;
                break;
            }
            case (115): {
                toReturn = 115;
                break;
            }
            case (116): {
                toReturn = 117;
                break;
            }
            case (117): {
                toReturn = 119;
                break;
            }
            case (118): {
                toReturn = 121;
                break;
            }
            case (119): {
                toReturn = 123;
                break;
            }
            case (120): {
                toReturn = 125;
                break;
            }
            case (121): {
                toReturn = 127;
                break;
            }
            case (122): {
                toReturn = 129;
                break;
            }
            case (123): {
                toReturn = 72;
                break;
            }
            case (124): {
                toReturn = 73;
                break;
            }
            case (125): {
                toReturn = 74;
                break;
            }
            case (126): {
                toReturn = 75;
                break;
            }
            case (127): {
                toReturn = 33;
                break;
            }
            case (8470): {
                toReturn = 77;
                break;
            }
        }
        return toReturn;
    }
}
