package com.swd.score.gui;

import com.swd.score.model.User;
import com.swd.score.service.ScoreService;

import javax.swing.*;
import java.awt.*;

/**
 * 登录窗口：输入用户名/口令，SHA-256 摘要校验。
 * 演示账号：teacher / 123456（教师），student / 123456（学生）。
 */
public class LoginFrame extends JFrame {

    private final JTextField userField = new JTextField(14);
    private final JPasswordField passField = new JPasswordField(14);
    private final ScoreService service = new ScoreService();

    public LoginFrame() {
        super("学生成绩管理系统 · 登录");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("学生成绩管理系统", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        add(title, g);

        g.gridwidth = 1;
        g.gridy = 1; g.gridx = 0; add(new JLabel("用户名:"), g);
        g.gridx = 1; add(userField, g);
        g.gridy = 2; g.gridx = 0; add(new JLabel("口  令:"), g);
        g.gridx = 1; add(passField, g);

        JButton loginBtn = new JButton("登 录");
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2;
        add(loginBtn, g);

        JLabel hint = new JLabel("演示账号: teacher / student，口令 123456", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(11f));
        hint.setForeground(Color.GRAY);
        g.gridy = 4;
        add(hint, g);

        Runnable doLogin = () -> {
            User u = service.login(userField.getText(), new String(passField.getPassword()));
            if (u == null) {
                JOptionPane.showMessageDialog(this, "用户名或口令错误",
                        "登录失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new MainFrame(u).setVisible(true);
            dispose();
        };
        loginBtn.addActionListener(e -> doLogin.run());
        passField.addActionListener(e -> doLogin.run());
        getRootPane().setDefaultButton(loginBtn);

        setSize(380, 280);
        setLocationRelativeTo(null);
    }
}
