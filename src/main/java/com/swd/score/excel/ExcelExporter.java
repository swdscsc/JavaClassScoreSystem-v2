package com.swd.score.excel;

import com.swd.score.model.ScoreView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Excel 导出（Apache POI · XSSF / .xlsx）。
 * Sheet1「成绩明细」：学号/姓名/班级/课程/分数/考试日期；
 * Sheet2「班级统计」：各班平均分 + 总平均。
 */
public class ExcelExporter {

    public void export(List<ScoreView> rows, Map<String, Double> classAverages, Path out)
            throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(wb);
            CellStyle scoreStyle = scoreStyle(wb);

            // ---- Sheet1 成绩明细 ----
            Sheet s1 = wb.createSheet("成绩明细");
            String[] head = {"学号", "姓名", "班级", "课程", "分数", "考试日期"};
            Row h = s1.createRow(0);
            for (int i = 0; i < head.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(head[i]);
                c.setCellStyle(headerStyle);
            }
            int r = 1;
            for (ScoreView v : rows) {
                Row row = s1.createRow(r++);
                row.createCell(0).setCellValue(v.getStuNo());
                row.createCell(1).setCellValue(v.getStuName());
                row.createCell(2).setCellValue(v.getClassName());
                row.createCell(3).setCellValue(v.getCourseName());
                Cell sc = row.createCell(4);
                sc.setCellValue(v.getScore());
                sc.setCellStyle(scoreStyle);
                row.createCell(5).setCellValue(v.getExamDate());
            }
            for (int i = 0; i < head.length; i++) s1.autoSizeColumn(i);

            // ---- Sheet2 班级统计 ----
            Sheet s2 = wb.createSheet("班级统计");
            Row h2 = s2.createRow(0);
            String[] head2 = {"班级", "平均分"};
            for (int i = 0; i < head2.length; i++) {
                Cell c = h2.createCell(i);
                c.setCellValue(head2[i]);
                c.setCellStyle(headerStyle);
            }
            int r2 = 1;
            for (Map.Entry<String, Double> e : classAverages.entrySet()) {
                Row row = s2.createRow(r2++);
                row.createCell(0).setCellValue(e.getKey());
                Cell c = row.createCell(1);
                c.setCellValue(e.getValue());
                c.setCellStyle(scoreStyle);
            }
            Row total = s2.createRow(r2++);
            total.createCell(0).setCellValue("全体平均");
            Cell c = total.createCell(1);
            c.setCellValue(round2(rows.stream().mapToDouble(ScoreView::getScore).average().orElse(0)));
            c.setCellStyle(scoreStyle);
            s2.autoSizeColumn(0);
            s2.autoSizeColumn(1);

            Files.createDirectories(out.toAbsolutePath().getParent());
            try (OutputStream os = Files.newOutputStream(out)) {
                wb.write(os);
            }
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        Font bold = wb.createFont();
        bold.setBold(true);
        st.setFont(bold);
        st.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        st.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        st.setAlignment(HorizontalAlignment.CENTER);
        st.setBorderBottom(BorderStyle.THIN);
        return st;
    }

    private CellStyle scoreStyle(Workbook wb) {
        CellStyle st = wb.createCellStyle();
        DataFormat fmt = wb.createDataFormat();
        st.setDataFormat(fmt.getFormat("0.0"));
        return st;
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
