package com.bochocredit.util;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CsvExporter {

    /**
     * Export a list of objects to CSV using ";" as delimiter.
     * Each object's fields will be written as columns.
     */
    public static <T> void exportListToCsv(List<T> rows, String filePath) throws IOException {
        if (rows == null || rows.isEmpty()) return;

        try (FileWriter writer = new FileWriter(filePath)) {
            Class<?> clazz = rows.get(0).getClass();
            Field[] fields = clazz.getDeclaredFields();

            // Header
            for (int i = 0; i < fields.length; i++) {
                writer.append(fields[i].getName());
                if (i < fields.length - 1) writer.append(";");
            }
            writer.append("\n");

            // Rows
            for (T row : rows) {
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    Object value = fields[i].get(row);
                    writer.append(value != null ? value.toString() : "");
                    if (i < fields.length - 1) writer.append(";");
                }
                writer.append("\n");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accessing fields for CSV export", e);
        }
    }

    /**
     * Export a Map<String,String> to CSV with ";" delimiter.
     * Each entry will be written as "key;value".
     */
    public static void exportMapToCsv(Map<String, String> map, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Title;Value\n");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writer.append(entry.getKey()).append(";").append(entry.getValue()).append("\n");
            }
        }
    }
}