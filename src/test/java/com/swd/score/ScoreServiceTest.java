package com.swd.score.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 核心纯函数（GPA 分段映射）单元测试。 */
public class ScoreServiceTest {

    @Test
    void scoreToPoint_upperBound() {
        assertEquals(4.0, ScoreService.scoreToPoint(95), 0.001);
        assertEquals(4.0, ScoreService.scoreToPoint(100), 0.001);
    }

    @Test
    void scoreToPoint_midRanges() {
        assertEquals(3.7, ScoreService.scoreToPoint(85), 0.001);
        assertEquals(3.0, ScoreService.scoreToPoint(80), 0.001);
        assertEquals(2.0, ScoreService.scoreToPoint(68), 0.001);
    }

    @Test
    void scoreToPoint_passAndFail() {
        assertEquals(1.0, ScoreService.scoreToPoint(60), 0.001);
        assertEquals(0.0, ScoreService.scoreToPoint(59), 0.001);
        assertEquals(0.0, ScoreService.scoreToPoint(0), 0.001);
    }
}
