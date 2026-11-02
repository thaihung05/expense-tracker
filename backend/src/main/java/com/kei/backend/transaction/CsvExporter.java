package com.kei.backend.transaction;

import com.kei.backend.category.CategoryType;
import java.util.List;

/** Xuat giao dich ra CSV (mo duoc bang Excel, giu dung tieng Viet nho ky tu BOM o dau file). */
final class CsvExporter {

    private static final String BOM = "﻿";

    private CsvExporter() {
    }

    static String toCsv(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder(BOM).append("Ngày,Loại,Danh mục,Số tiền,Ghi chú\r\n");
        for (Transaction t : transactions) {
            csv.append(t.getOccurredOn()).append(',')
                    .append(t.getCategory().getType() == CategoryType.INCOME ? "Thu" : "Chi").append(',')
                    .append(escape(t.getCategory().getName())).append(',')
                    .append(t.getAmount()).append(',')
                    .append(escape(t.getNote())).append("\r\n");
        }
        return csv.toString();
    }

    /** Boc trong dau ngoac kep neu co dau phay/ngoac kep/xuong dong; chan cong thuc Excel doc hai. */
    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
