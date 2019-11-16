package com.scvtoxlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        try {

            List<Data> objects = csvToListOfObjects(
                "./in",

                pathName -> {
                    try {
                        return csvToObject(
                            pathName,
                            ";",
                            (key, value, data) -> {
                                if ("key1".equals(key))
                                    data.setValue1(value);

                                if ("key2".equals(key))
                                    data.setValue2(value);

                                if ("ключ3".equals(key))
                                    data.setValue3(value);
                            }
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            );

            objectsToCsv(objects, "./out/processing.csv", ";",

                (string, printWriter) ->
                {

                },

                (string, printWriter) -> {

                }
            );

            objToXlsx(
                new String[]{ "ID", "Key1", "Key2", "Ключ3"},

                (row, o) -> {
                    row.createCell(0).setCellValue(o.getId());
                    row.createCell(1).setCellValue(o.getValue1());
                    row.createCell(2).setCellValue(o.getValue2());
                    row.createCell(3).setCellValue(o.getValue3());
                },

                objects,

                "./out/confluence.xlsx");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Data> csvToListOfObjects(String folderPath, Function<String, Data> function) {
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .map(Path::toString)
                    .map(function)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Data csvToObject(String csvPath, String splitChar, TriConsumer<String, String, Data> triConsumer) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvPath), StandardCharsets.UTF_8)) {

            Data data = new Data();
            data.setId(fileName(csvPath));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(splitChar);
                String key = split[0];
                String value = split[1];

                triConsumer.accept(key, value, data);
            }
            return data;
        }
    }

    private static String fileName(String csvPath) {
        return csvPath.replace(".\\in\\", "").replace(".csv", "");
    }

    public interface TriConsumer<K, V, S> {
        void accept(K var1, V var2, S var3);
    }

    private static void objToXlsx(String[] titleColumns, BiConsumer<Row, Data> dataColumns, List<Data> objects, String xlsxPath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        fillTitleColumns(workbook, sheet, titleColumns);
        fillDataColumns(objects, sheet, dataColumns);
        resize(titleColumns, sheet);
        writeToFile(xlsxPath, workbook);
    }

    private static void fillTitleColumns(Workbook workbook, Sheet sheet, String[] titleColumns) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < titleColumns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titleColumns[i]);
            cell.setCellStyle(style(workbook));
        }
    }

    private static CellStyle style(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        return headerCellStyle;
    }

    private static void fillDataColumns(List<Data> objects, Sheet sheet, BiConsumer<Row, Data> dataColumns) {
        int rowNum = 1;
        for (Data obj : objects) {
            Row row = sheet.createRow(rowNum++);
            dataColumns.accept(row, obj);
        }
    }

    private static void resize(String[] titleColumns, Sheet sheet) {
        for (int i = 0; i < titleColumns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void writeToFile(String xlsxPath, Workbook workbook) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(xlsxPath)) {
            workbook.write(fileOut);
        }
    }

    private static void objectsToCsv(List<Data> objects, String csvFilePath, String separator,
                              BiConsumer<String, PrintWriter> beforeObjectsToScv,
                              BiConsumer<String, PrintWriter> afterObjectsToScv) throws FileNotFoundException {
        File csvOutputFile = new File(csvFilePath);

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            beforeObjectsToScv.accept(separator, pw);
            objectsToCsv(objects, separator, pw);
            afterObjectsToScv.accept(separator, pw);
        }

    }

    private static void beforeObjectsToScv(String separator, PrintWriter pw) {

    }

    private static void objectsToCsv(List<Data> objects, String separator, PrintWriter pw) {

        objects.stream()
                .map(data -> convertToCsv(data, separator))
                .forEach(pw::println);


    }

    private static void afterObjectsToScv(String separator, PrintWriter pw) {

    }

    private static String convertToCsv(Data o, String sep) {
        return o.getId() + sep
                + o.getValue2();
    }
}
