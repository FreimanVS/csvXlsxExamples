package com.scvtoxlsx;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static int n = 0;
    private static final String INPUT_CSV_PATH = "./in";
    private static final String OUTPUT_CSV_PATH = "./out/processing.csv";
    private static final String SEPARATOR = ";";
    private static final String OUTPUT_XLSX_PATH = "./out/confluence.xlsx";

    public static void main(String[] args) {
        try {

            List<Data> objects = csvToListOfObjects(
                INPUT_CSV_PATH,

                pathName -> {
                    try {
                        return csvToObject(
                            pathName,
                            SEPARATOR,
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

            objectsToCsv(objects, OUTPUT_CSV_PATH, SEPARATOR,

                (sep, pw) -> {
                    pw.println("blablabla".concat(sep).concat(getDateNow()).concat(sep).concat("MANUAL_VALUE"));
                },

                (o, sep) -> o.getId().concat(sep).concat(o.getValue1()).concat(System.lineSeparator())
                        .concat(o.getValue2()).concat(sep).concat("MANUAL_VALUE3").concat(sep).concat(o.getValue3()),

                (sep, pw) -> {
                    pw.println("MANUAL_VALUE2".concat(sep).concat(String.valueOf(n)).concat(sep).concat("blablabla4"));
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

                OUTPUT_XLSX_PATH);

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

            System.out.println("csvToObject started...");

            Data data = new Data();
            data.setId(fileName(csvPath));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(splitChar);
                String key = split[0];
                String value = split[1];

                triConsumer.accept(key, value, data);
            }

            System.out.println("csvToObject successfully completed!");

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

        System.out.println("objToXlsx started...");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        fillTitleColumns(workbook, sheet, titleColumns);
        fillDataColumns(objects, sheet, dataColumns);
        resize(titleColumns, sheet);
        writeToFile(xlsxPath, workbook);

        System.out.println("objToXlsx successfully completed!");
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
                              BiFunction<Data, String, String> convertToCsv,
                              BiConsumer<String, PrintWriter> afterObjectsToScv) throws FileNotFoundException {

        System.out.println("objectsToCsv started...");

        File csvOutputFile = new File(csvFilePath);

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            beforeObjectsToScv.accept(separator, pw);
            objectsToCsv(objects, separator, pw, convertToCsv);
            afterObjectsToScv.accept(separator, pw);
        }

        System.out.println("objectsToCsv successfully completed!");

    }

    private static void objectsToCsv(List<Data> objects, String sep, PrintWriter pw, BiFunction<Data, String, String> convertToScv) {
        objects.stream()
                .map(data -> convertToScv.apply(data, sep))
                .forEach(s -> {
                    pw.println(s);
                    n ++;
                });
    }

    public static String getDateNow() {
        return LocalDateTime.now().format(new DateTimeFormatterBuilder()
                .parseCaseInsensitive().appendPattern("yyyyMMddHHmm").toFormatter(Locale.ENGLISH));
    }
}
