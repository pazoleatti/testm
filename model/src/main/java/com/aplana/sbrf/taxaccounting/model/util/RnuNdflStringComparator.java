package com.aplana.sbrf.taxaccounting.model.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Компаратор для сравнивания строк в особом порядке.
 * Порядок следования char предопределен порядком следования int, который лежит в основе каждого char. Для того чтобы получить особый порядок, переопределяем порядок
 * следования int и символы идут в необходимом порядке. Т.е. значения int всех непечатаемых символов сделаны самыми маленькими, затем идут значения печатаемых символов и цифр,
 * затем идут буквы латинского алфавита, затем идут буквы кириллического алфавита в алфавитном порядке Причем прописные и строчные буквы объединены вместе.
 * Например, AaBbCc etc. В натуральном порядке сначала шли все прописные буквы, потом все строчные буквы. Объединение прописных и строчных букв дает регистронезависимость при сортировке.
 *
 */
public class RnuNdflStringComparator implements Comparator<String> {

    public static final RnuNdflStringComparator INSTANCE = new RnuNdflStringComparator();

    private final Map<Integer, Integer> charOrderingRef = new HashMap<>();

    private RnuNdflStringComparator() {
        charOrderingRef.put(1072, 1041);
        charOrderingRef.put(1073, 1043);
        charOrderingRef.put(1074, 1045);
        charOrderingRef.put(1075, 1047);
        charOrderingRef.put(1076, 1049);
        charOrderingRef.put(1077, 1051);
        charOrderingRef.put(1078, 1055);
        charOrderingRef.put(1079, 1057);
        charOrderingRef.put(1080, 1059);
        charOrderingRef.put(1081, 1061);
        charOrderingRef.put(1082, 1063);
        charOrderingRef.put(1083, 1065);
        charOrderingRef.put(1084, 1067);
        charOrderingRef.put(1085, 1069);
        charOrderingRef.put(1086, 1071);
        charOrderingRef.put(1087, 1073);
        charOrderingRef.put(1088, 1075);
        charOrderingRef.put(1089, 1077);
        charOrderingRef.put(1090, 1079);
        charOrderingRef.put(1091, 1081);
        charOrderingRef.put(1092, 1083);
        charOrderingRef.put(1093, 1085);
        charOrderingRef.put(1094, 1087);
        charOrderingRef.put(1095, 1089);
        charOrderingRef.put(1096, 1091);
        charOrderingRef.put(1097, 1093);
        charOrderingRef.put(1098, 1095);
        charOrderingRef.put(1099, 1097);
        charOrderingRef.put(1100, 1099);
        charOrderingRef.put(1101, 1101);
        charOrderingRef.put(1102, 1103);
        charOrderingRef.put(1103, 1105);
        charOrderingRef.put(1041, 1042);
        charOrderingRef.put(1042, 1044);
        charOrderingRef.put(1043, 1046);
        charOrderingRef.put(1044, 1048);
        charOrderingRef.put(1045, 1050);
        charOrderingRef.put(1046, 1054);
        charOrderingRef.put(1047, 1056);
        charOrderingRef.put(1048, 1058);
        charOrderingRef.put(1049, 1060);
        charOrderingRef.put(1050, 1062);
        charOrderingRef.put(1051, 1064);
        charOrderingRef.put(1052, 1066);
        charOrderingRef.put(1053, 1068);
        charOrderingRef.put(1054, 1070);
        charOrderingRef.put(1055, 1072);
        charOrderingRef.put(1056, 1074);
        charOrderingRef.put(1057, 1076);
        charOrderingRef.put(1058, 1078);
        charOrderingRef.put(1059, 1080);
        charOrderingRef.put(1060, 1082);
        charOrderingRef.put(1061, 1084);
        charOrderingRef.put(1062, 1086);
        charOrderingRef.put(1063, 1088);
        charOrderingRef.put(1064, 1090);
        charOrderingRef.put(1065, 1092);
        charOrderingRef.put(1066, 1094);
        charOrderingRef.put(1067, 1096);
        charOrderingRef.put(1068, 1098);
        charOrderingRef.put(1069, 1100);
        charOrderingRef.put(1070, 1102);
        charOrderingRef.put(1071, 1104);
        charOrderingRef.put(1025, 1052);
        charOrderingRef.put(1105, 1053);
        charOrderingRef.put(33, 34);
        charOrderingRef.put(34, 35);
        charOrderingRef.put(35, 36);
        charOrderingRef.put(36, 37);
        charOrderingRef.put(37, 38);
        charOrderingRef.put(38, 39);
        charOrderingRef.put(39, 40);
        charOrderingRef.put(40, 41);
        charOrderingRef.put(41, 42);
        charOrderingRef.put(42, 43);
        charOrderingRef.put(43, 44);
        charOrderingRef.put(44, 45);
        charOrderingRef.put(45, 46);
        charOrderingRef.put(45, 46);
        charOrderingRef.put(46, 47);
        charOrderingRef.put(47, 48);
        charOrderingRef.put(48, 49);
        charOrderingRef.put(49, 50);
        charOrderingRef.put(50, 51);
        charOrderingRef.put(51, 52);
        charOrderingRef.put(52, 53);
        charOrderingRef.put(53, 54);
        charOrderingRef.put(54, 55);
        charOrderingRef.put(55, 56);
        charOrderingRef.put(56, 57);
        charOrderingRef.put(57, 58);
        charOrderingRef.put(58, 59);
        charOrderingRef.put(59, 60);
        charOrderingRef.put(61, 62);
        charOrderingRef.put(62, 63);
        charOrderingRef.put(63, 64);
        charOrderingRef.put(64, 65);
        charOrderingRef.put(65, 78);
        charOrderingRef.put(66, 80);
        charOrderingRef.put(67, 82);
        charOrderingRef.put(68, 84);
        charOrderingRef.put(69, 86);
        charOrderingRef.put(70, 88);
        charOrderingRef.put(71, 90);
        charOrderingRef.put(72, 92);
        charOrderingRef.put(73, 94);
        charOrderingRef.put(74, 96);
        charOrderingRef.put(75, 98);
        charOrderingRef.put(76, 100);
        charOrderingRef.put(77, 102);
        charOrderingRef.put(78, 104);
        charOrderingRef.put(79, 106);
        charOrderingRef.put(80, 108);
        charOrderingRef.put(81, 110);
        charOrderingRef.put(82, 112);
        charOrderingRef.put(83, 114);
        charOrderingRef.put(84, 116);
        charOrderingRef.put(85, 118);
        charOrderingRef.put(86, 120);
        charOrderingRef.put(87, 122);
        charOrderingRef.put(88, 124);
        charOrderingRef.put(89, 126);
        charOrderingRef.put(90, 128);
        charOrderingRef.put(91, 66);
        charOrderingRef.put(92, 67);
        charOrderingRef.put(93, 68);
        charOrderingRef.put(94, 69);
        charOrderingRef.put(95, 70);
        charOrderingRef.put(96, 71);
        charOrderingRef.put(97, 79);
        charOrderingRef.put(98, 81);
        charOrderingRef.put(99, 83);
        charOrderingRef.put(100, 85);
        charOrderingRef.put(101, 87);
        charOrderingRef.put(102, 89);
        charOrderingRef.put(103, 91);
        charOrderingRef.put(104, 93);
        charOrderingRef.put(105, 95);
        charOrderingRef.put(106, 97);
        charOrderingRef.put(107, 99);
        charOrderingRef.put(108, 101);
        charOrderingRef.put(109, 103);
        charOrderingRef.put(110, 105);
        charOrderingRef.put(111, 107);
        charOrderingRef.put(112, 109);
        charOrderingRef.put(113, 111);
        charOrderingRef.put(114, 113);
        charOrderingRef.put(115, 115);
        charOrderingRef.put(116, 117);
        charOrderingRef.put(117, 119);
        charOrderingRef.put(118, 121);
        charOrderingRef.put(119, 122);
        charOrderingRef.put(120, 125);
        charOrderingRef.put(121, 127);
        charOrderingRef.put(122, 129);
        charOrderingRef.put(123, 72);
        charOrderingRef.put(124, 73);
        charOrderingRef.put(125, 74);
        charOrderingRef.put(126, 75);
        charOrderingRef.put(127, 33);
        charOrderingRef.put(8470, 77);
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
            int i1 = charOrderingRef.get((int) c1) == null ? (int) c1 : charOrderingRef.get((int) c1);
            int i2 = charOrderingRef.get((int) c2) == null ? (int) c2 : charOrderingRef.get((int) c2);
            if (i1 != i2) {
                return i1 - i2;
            }
            k++;
        }
        return len1 - len2;
    }
}
