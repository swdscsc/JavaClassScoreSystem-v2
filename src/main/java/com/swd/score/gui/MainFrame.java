package com.swd.score.gui;

import com.swd.score.excel.ExcelExporter;
import com.swd.score.model.ScoreView;
import com.swd.score.model.User;
import com.swd.score.service.ScoreService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 主界面（按角色区分权限）：
 * - teacher：查看全部成绩 + 录入成绩 + 导出 Excel；
 * - student：仅查看本人成绩（可导出本人成绩单）。
 */
public class MainFrame extends JFrame {

    private final User user;
    private final ScoreService service = new ScoreService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"学号", "姓名", "班级", "课程", "分数", "考试日期"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel statsLabel = new JLabel();

    public MainFrame(User user) {
        this.user = user;
        setTitle("学生成绩管理系统 — 当前用户: " + user.getDisplayName()
                + "（" + (user.isTeacher() ? "教师" : "学生") + "）");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // 顶部统计栏
        statsLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 0, 10));
        add(statsLabel, BorderLayout.NORTH);

        // 中部表格
        table.setRowHeight(24);
        table.setFont(table.getFont().deriveFont(13f));
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 底部按钮栏（角色权限）
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton refresh = new JButton("刷新");
        refresh.addActionListener(e -> reload());
        btns.add(refresh);
        if (user.isTeacher()) {
            JButton add = new JButton("录入成绩");
            add.addActionListener(e -> new AddScoreDialog(this).setVisible(true));
            btns.add(add);
        }
        JButton export = new JButton("导出 Excel");
        export.addActionListener(e -> doExport());
        btns.add(export);
        JButton logout = new JButton("退出登录");
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        btns.add(logout);
        add(btns, BorderLayout.SOUTH);

        setSize(860, 560);
        setLocationRelativeTo(null);
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        List<ScoreView> rows = user.isTeacher()
                ? service.allRows()
                : service.rowsForStudent(ownStudentNo());
        for (ScoreView v : rows) {
            model.addRow(new Object[]{v.getStuNo(), v.getStuName(), v.getClassName(),
                    v.getCourseName(), v.getScore(), v.getExamDate()});
        }
        if (user.isTeacher()) {
            Map<String, Double> avg = service.classAverages();
            StringBuilder sb = new StringBuilder("班级平均分: ");
            avg.forEach((k, v) -> sb.append(k).append("=").append(v).append("    "));
            statsLabel.setText(sb.toString().trim());
        } else {
            double gpa = ScoreService.gpa4(rows.stream().map(ScoreView::getScore).toList());
            double avg = ScoreService.average(rows.stream().map(ScoreView::getScore).toList());
            statsLabel.setText(String.format("本人课程数: %d    平均分: %.2f    GPA(4.0): %.2f",
                    rows.size(), avg, gpa));
        }
    }

    /** 学生视角默认以本人学号查询（演示账号 student 对应 2023083101）。 */
    private String ownStudentNo() { return "2023083101"; }

    private void doExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(
                user.isTeacher() ? "成绩总表.xlsx" : "我的成绩单.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Path out = chooser.getSelectedFile().toPath();
            List<ScoreView> rows = user.isTeacher()
                    ? service.allRows()
                    : service.rowsForStudent(ownStudentNo());
            new ExcelExporter().export(rows, service.classAverages(), out);
            JOptionPane.showMessageDialog(this, "导出成功:\n" + out.toAbsolutePath(),
                    "导出完成", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 教师录入成绩对话框。 */
    private class AddScoreDialog extends JDialog {
        private final JComboBox<String> stuBox = new JComboBox<>();
        private final JComboBox<String> courseBox = new JComboBox<>(
                new String[]{"Java 程序设计", "数据结构", "数据库原理", "操作系统"});
        private final JTextField scoreField = new JTextField(8);
        private final JTextField dateField = new JTextField(LocalDate.now().toString(), 12);

        AddScoreDialog(Frame owner) {
            super(owner, "录入成绩", true);
            setLayout(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(8, 10, 8, 10);
            g.fill = GridBagConstraints.HORIZONTAL;

            service.allRows().forEach(r -> {
                String item = r.getStuNo() + " · " + r.getStuName();
                if (stuBox.getItemCount() == 0 || !contains(stuBox, item)) stuBox.addItem(item);
            });

            g.gridx = 0; g.gridy = 0; add(new JLabel("学生:"), g);
            g.gridx = 1; add(stuBox, g);
            g.gridy = 1; g.gridx = 0; add(new JLabel("课程:"), g);
            g.gridx = 1; add(courseBox, g);
            g.gridy = 2; g.gridx = 0; add(new JLabel("分数:"), g);
            g.gridx = 1; add(scoreField, g);
            g.gridy = 3; g.gridx = 0; add(new JLabel("考试日期:"), g);
            g.gridx = 1; add(dateField, g);

            JButton ok = new JButton("保 存");
            g.gridy = 4; g.gridx = 0; g.gridwidth = 2; add(ok, g);
            ok.addActionListener(e -> {
                String item = String.valueOf(stuBox.getSelectedItem());
                String stuNo = item.split(" · ")[0];
                try {
                    double value = Double.parseDouble(scoreField.getText().trim());
                    if (value < 0 || value > 100) throw new NumberFormatException();
                    boolean okFlag = service.addScore(
                            stuNo, String.valueOf(courseBox.getSelectedItem()), value, dateField.getText().trim());
                    if (okFlag) {
                        JOptionPane.showMessageDialog(this, "保存成功", "完成", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        reload();
                    } else {
                        JOptionPane.showMessageDialog(this, "保存失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "请输入 0-100 的数字", "输入有误", JOptionPane.WARNING_MESSAGE);
                }
            });
            setSize(380, 320);
            setLocationRelativeTo(owner);
        }

        private boolean contains(JComboBox<String> box, String item) {
            for (int i = 0; i < box.getItemCount(); i++) {
                if (item.equals(box.getItemAt(i))) return true;
            }
            return false;
        }
    }
}
