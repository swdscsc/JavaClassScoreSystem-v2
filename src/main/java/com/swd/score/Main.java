package com.swd.score;

import com.swd.score.model.Course;
import com.swd.score.model.Student;
import com.swd.score.service.ScoreService;

import java.util.List;
import java.util.Scanner;

/** 控制台交互入口。运行后会自动在本地生成 score.db 并建表。 */
public class Main {
    public static void main(String[] args) throws Exception {
        ScoreService svc = new ScoreService();
        Scanner sc = new Scanner(System.in, "UTF-8");
        System.out.println("=== 学生成绩管理系统 v2 (SQLite) ===");

        while (true) {
            System.out.println();
            System.out.println("1.新增学生  2.新增课程  3.录入成绩  4.学生列表");
            System.out.println("5.课程列表  6.班级平均分  7.课程平均分  8.绩点排名");
            System.out.println("9.导出CSV   0.退出");
            System.out.print("请选择> ");
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("学号> "); String no = sc.nextLine().trim();
                        System.out.print("姓名> "); String name = sc.nextLine().trim();
                        System.out.print("性别> "); String gender = sc.nextLine().trim();
                        System.out.print("班级> "); String cls = sc.nextLine().trim();
                        svc.addStudent(new Student(no, name, gender, cls));
                        System.out.println("已添加学生: " + name);
                    }
                    case "2" -> {
                        System.out.print("课程代码> "); String code = sc.nextLine().trim();
                        System.out.print("课程名> "); String cname = sc.nextLine().trim();
                        System.out.print("学分> "); double credit = Double.parseDouble(sc.nextLine().trim());
                        svc.addCourse(new Course(code, cname, credit));
                        System.out.println("已添加课程: " + cname);
                    }
                    case "3" -> {
                        System.out.print("学号> "); String no = sc.nextLine().trim();
                        System.out.print("课程代码> "); String code = sc.nextLine().trim();
                        System.out.print("成绩> "); double v = Double.parseDouble(sc.nextLine().trim());
                        System.out.println(svc.recordScore(no, code, v));
                    }
                    case "4" -> {
                        for (Student s : svc.listStudents())
                            System.out.printf("%d  %s  %s  %s  %s%n",
                                    s.getId(), s.getStudentNo(), s.getName(), s.getGender(), s.getClassName());
                    }
                    case "5" -> {
                        for (Course c : svc.listCourses())
                            System.out.printf("%d  %s  %s  %.1f学分%n",
                                    c.getId(), c.getCode(), c.getName(), c.getCredit());
                    }
                    case "6" -> {
                        System.out.print("班级名> "); String cls = sc.nextLine().trim();
                        System.out.printf("班级平均分: %.2f%n", svc.classAverage(cls));
                    }
                    case "7" -> {
                        System.out.print("课程代码> "); String code = sc.nextLine().trim();
                        double a = svc.courseAverage(code);
                        System.out.println(a < 0 ? "未找到课程" : String.format("课程平均分: %.2f", a));
                    }
                    case "8" -> {
                        List<ScoreService.RankEntry> ranks = svc.ranking();
                        int i = 1;
                        for (ScoreService.RankEntry e : ranks)
                            System.out.printf("%d. %s  GPA=%.2f%n", i++, e.name, e.gpa);
                    }
                    case "9" -> {
                        System.out.print("导出路径(默认 score.csv)> ");
                        String p = sc.nextLine().trim();
                        svc.exportCsv(p.isEmpty() ? "score.csv" : p);
                        System.out.println("已导出 " + (p.isEmpty() ? "score.csv" : p));
                    }
                    case "0" -> { System.out.println("再见"); return; }
                    default -> System.out.println("无效输入");
                }
            } catch (Exception e) {
                System.out.println("操作失败: " + e.getMessage());
            }
        }
    }
}
