package com.bmrider.form;

import com.bmrider.base.JRoundedPasswordField;
import com.bmrider.base.JRoundedTextField;
import com.bmrider.database.DBConnection;
import com.bmrider.security.HashAlgorithm;

import javax.swing.*;
import java.awt.event.*;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class BMRiderSignUpDialog extends JDialog {
    private JPanel pnlContent;
    private JButton btnOK;
    private JButton btnCancel;
    private JRoundedTextField txtSignUpId;
    private JRoundedPasswordField txtSignUpPassword;
    private JRoundedTextField txtSignUpName;
    private JRoundedTextField txtSignUpAddress;
    private JRoundedTextField txtSignUpPhoneNumber;
    private JRoundedPasswordField txtSignUpPasswordConfirm;

    private final String[] indexes = {
            "아이디를", "비밀번호를", "비밀번호 확인을", "이름을", "주소를", "휴대폰 번호를"
    };

    public BMRiderSignUpDialog() {
        initializeComponents();
    }

    private void createUIComponents() {
        txtSignUpAddress = new JRoundedTextField(5);
        txtSignUpId = new JRoundedTextField(5);
        txtSignUpName = new JRoundedTextField(5);
        txtSignUpPhoneNumber = new JRoundedTextField(5);

        txtSignUpPassword = new JRoundedPasswordField(5);
        txtSignUpPasswordConfirm = new JRoundedPasswordField(5);
    }

    private void initializeComponents() {
        btnOK.addActionListener(e -> onOK());
        btnCancel.addActionListener(e -> onCancel());

        // ESCAPE 시 onCancel() 호출
        pnlContent.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.setTitle("배민커넥트 회원가입");
        this.setContentPane(pnlContent);
        this.setModal(true);
        this.getRootPane().setDefaultButton(btnOK);

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

    private void onOK() {
        String riderAddress = txtSignUpAddress.getText();
        String riderId = txtSignUpId.getText();
        String riderName = txtSignUpName.getText();
        String riderPhone = txtSignUpPhoneNumber.getText();
        String riderPwd = String.valueOf(txtSignUpPassword.getPassword());
        String riderPwdConfirm = String.valueOf(txtSignUpPasswordConfirm.getPassword());
        String[] infos = {
                riderId, riderPwd, riderPwdConfirm, riderName, riderAddress, riderPhone
        };
        boolean isValidTransaction = false;

        for (int i = 0; i < infos.length; ++i) {
            if (infos[i].isEmpty()) {
                JOptionPane.showMessageDialog(this, String.format("%s 입력해주세요.", indexes[i]));
                return;
            }
        }
        if (!riderPwd.equals(riderPwdConfirm)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
            return;
        }

        try {
            String selectQuery = "SELECT RIDER_PASSWORD1, RIDER_PASSWORD2 FROM RIDER WHERE RIDER_ID='" + riderId + "'";
            String insertQuery = "INSERT INTO RIDER(RIDER_ID, RIDER_PASSWORD1, RIDER_PASSWORD2, RIDER_ADDRESS," +
                    "RIDER_NAME, RIDER_PHONE, RIDER_COUNT) VALUES(?, ?, ?, ?, ?, ?, ?)";
            String pwdMd5 = HashAlgorithm.makeHash(riderPwd, "md5");
            String pwdSha1 = HashAlgorithm.makeHash(riderPwd, "sha1");
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            ResultSet rs;

            rs = stmt.executeQuery(selectQuery);

            if (rs.next()) {
                JOptionPane.showMessageDialog(
                        this,
                        "이미 있는 아이디입니다.",
                        "Oracle Manager",
                        JOptionPane.ERROR_MESSAGE
                );
            } else {
                pstmt.setString(1, riderId);
                pstmt.setString(2, pwdMd5);
                pstmt.setString(3, pwdSha1);
                pstmt.setString(4, riderAddress);
                pstmt.setString(5, riderName);
                pstmt.setString(6, riderPhone);
                pstmt.setInt(7, 0);

                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "회원가입이 완료되었습니다.",
                            "Oracle Manager",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    isValidTransaction = true;
                }
            }
            rs.close();
            stmt.close();
            pstmt.close();
            conn.close();
        } catch (SQLException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        if (isValidTransaction) {
            dispose();
        }
    }

    private void onCancel() {
        // 필요한 경우 이곳에 코드 추가
        dispose();
    }
}
