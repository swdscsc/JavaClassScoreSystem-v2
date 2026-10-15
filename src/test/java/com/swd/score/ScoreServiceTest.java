package com.swd.score;

import com.swd.score.model.ScoreView;
import com.swd.score.service.ScoreService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 业务层纯函数单元测试（不依赖数据库）。 */
class ScoreServiceTest {

    private static List<Double> scores(double... v) {
        List<Double> out = new ArrayList<>();
        for (double x : v) out.add(x);
        return out;
    }

    private static ScoreView row(String no, double score) {
        return new ScoreView(no, "n" + no, "计科2301", "Java", score, "2026-06-20");
    }

    @Test
    void averageEmpty() { assertEquals(0.0, ScoreService.average(List.of())); }

    @Test
    void averageNormal() { assertEquals(85.0, ScoreService.average(scores(80, 90))); }

    @Test
    void gpaBoundary() {
        assertEquals(4.0, ScoreService.toGpaPoint(90));
        assertEquals(3.7, ScoreService.toGpaPoint(85));
        assertEquals(3.0, ScoreService.toGpaPoint(78));
        assertEquals(1.0, ScoreService.toGpaPoint(60));
        assertEquals(0.0, ScoreService.toGpaPoint(59));
    }

    @Test
    void gpa4Mixed() {
        // (4.0 + 3.7 + 0.0) / 3 = 2.57
        assertEquals(2.57, ScoreService.gpa4(scores(92, 86, 40)));
    }

    @Test
    void averageByStudentKeepsOrder() {
        List<ScoreView> rows = List.of(row("A", 80), row("B", 90), row("A", 100));
        Map<String, Double> avg = ScoreService.averageByStudent(rows);
        assertEquals(90.0, avg.get("A"));
        assertEquals(90.0, avg.get("B"));
    }

    @Test
    void rankOfTies() {
        List<ScoreView> rows = List.of(row("A", 90), row("B", 90), row("C", 70));
        assertEquals(1, ScoreService.rankOf(rows, "A"));
        assertEquals(1, ScoreService.rankOf(rows, "B")); // 同分并列第 1
        assertEquals(3, ScoreService.rankOf(rows, "C"));
    }
}
