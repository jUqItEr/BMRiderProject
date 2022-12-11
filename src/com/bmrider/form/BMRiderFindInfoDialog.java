package com.bmrider.form;

import com.bmrider.base.JRoundedPasswordField;
import com.bmrider.base.JRoundedTextField;
import com.bmrider.database.DBConnection;
import com.bmrider.security.HashAlgorithm;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class BMRiderFindInfoDialog extends JDialog {
    private JPanel pnlContent;
    private JRoundedTextField txtName;
    private JRoundedTextField txtPhoneNumber;
    private JButton btnFindId;
    private JRoundedTextField txtId;
    private JRoundedPasswordField txtPassword;
    private JRoundedPasswordField txtPasswordConfirm;
    private JButton btnFindPassword;

    public BMRiderFindInfoDialog() {
        initializeComponents();
    }

    private void createUIComponents() {
        txtId = new JRoundedTextField(5);
        txtName = new JRoundedTextField(5);
        txtPhoneNumber = new JRoundedTextField(5);

        txtPassword = new JRoundedPasswordField(5);
        txtPasswordConfirm = new JRoundedPasswordField(5);
    }

    private void initializeComponents() {
        btnFindId.addActionListener(e -> btnFindIdClickListener());
        btnFindPassword.addActionListener(e -> btnFindPasswordClickListener());

        // ESCAPE 시 onCancel() 호출
        pnlContent.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.setTitle("ID/패스워드 찾기");
        this.setContentPane(pnlContent);
        this.setModal(true);

        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void btnFindIdClickListener() {
        String riderName = txtName.getText();
        String riderPhone = txtPhoneNumber.getText();

        if (riderName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "이름을 입력해주세요.",
                    this.getTitle(),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        } else if (riderPhone.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "휴대폰 번호를 입력해주세요.",
                    this.getTitle(),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            String query = "SELECT RIDER_ID FROM RIDER WHERE RIDER_NAME=? AND RIDER_PHONE=?";
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs;

            pstmt.setString(1, riderName);
            pstmt.setString(2, riderPhone);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String riderId = rs.getString(1);

                JOptionPane.showMessageDialog(
                        this,
                        String.format("찾으시는 아이디는 [%s]입니다.", riderId),
                        "Oracle Manager",
                        JOptionPane.INFORMATION_MESSAGE
                );
                txtName.setText("");
                txtPhoneNumber.setText("");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "입력하신 정보에 해당하는 아이디가 없습니다.",
                        "Oracle Manager",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void btnFindPasswordClickListener() {
        String riderId = txtId.getText();
        String riderPwd = String.valueOf(txtPassword.getPassword());
        String riderPwdConfirm = String.valueOf(txtPasswordConfirm.getPassword());

        if (riderId.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "아이디를 입력해주세요.",
                    this.getTitle(),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        } else if (riderPwd.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "새 비밀번호를 입력해주세요.",
                    this.getTitle(),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        } else if (riderPwdConfirm.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "새 비밀번호 확인를 입력해주세요.",
                    this.getTitle(),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        if (!riderPwd.equals(riderPwdConfirm)) {
            JOptionPane.showMessageDialog(
                    this,
                    "입력하신 비밀번호와 확인 비밀번호가 다릅니다.",
                    "Oracle Manager",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            String selectQuery = "SELECT RIDER_PASSWORD1, RIDER_PASSWORD2 FROM RIDER WHERE RIDER_ID='" + riderId + "'";
            String updateQuery = "UPDATE RIDER SET RIDER_PASSWORD1=?, RIDER_PASSWORD2=? WHERE RIDER_ID=?";
            String pwdMd5 = HashAlgorithm.makeHash(riderPwd, "md5");
            String pwdSha1 = HashAlgorithm.makeHash(riderPwd, "sha1");
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            PreparedStatement pstmt = conn.prepareStatement(updateQuery);
            ResultSet rs;

            rs = stmt.executeQuery(selectQuery);

            if (rs.next()) {
                String md5 = rs.getString(1);
                String sha1 = rs.getString(2);

                if (pwdMd5.equals(md5) && pwdSha1.equals(sha1)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "입력하신 비밀번호가 이전 비밀번호와 같습니다.",
                            "Oracle Manager",
                            JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    pstmt.setString(1, pwdMd5);
                    pstmt.setString(2, pwdSha1);
                    pstmt.setString(3, riderId);

                    if (pstmt.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(
                                this,
                                "비밀번호가 변경되었습니다.",
                                "Oracle Manager",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        txtId.setText("");
                        txtPassword.setText("");
                        txtPasswordConfirm.setText("");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "입력하신 아이디가 존재하지 않습니다.",
                        "Oracle Manager",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            rs.close();
            stmt.close();
            pstmt.close();
            conn.close();
        } catch (SQLException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    private void onCancel() {
        dispose();
    }
}
