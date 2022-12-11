package com.bmrider.form;

import com.bmrider.base.JRoundedPasswordField;
import com.bmrider.base.JRoundedTextField;
import com.bmrider.database.DBConnection;
import com.bmrider.security.HashAlgorithm;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

public class BMRiderLoginForm extends JFrame {
    private JPanel pnlMain;
    private JRoundedTextField txtLoginId;
    private JRoundedPasswordField txtLoginPassword;
    private JButton btnLogin;
    private JPanel pnlImageSection;
    private JLabel lblSignUp;
    private JCheckBox chkBoxAutoLogin;
    private JButton btnFindInfo;

    public BMRiderLoginForm() {
        initializeComponents();
    }

    private void createUIComponents() {
        pnlImageSection = new JPanel() {
            final Image background = new ImageIcon("src/com/bmrider/res/drawable/bg_background.png")
                                                   .getImage();

            @Override
            protected void paintComponent(Graphics g) {
                Dimension dim = getSize();

                super.paintComponent(g);
                g.drawImage(background, 0, 0, dim.width, dim.height, this);
            }
        };

        txtLoginId = new JRoundedTextField(5);
        txtLoginPassword = new JRoundedPasswordField(5);
    }

    private void initializeComponents() {
        // User Elements.
        btnFindInfo.addActionListener(e -> btnFindInfoClickListener());
        btnLogin.addActionListener(e -> btnLoginClickListener());

        lblSignUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new BMRiderSignUpDialog();
            }
        });

        // Main Attributes.
        this.setContentPane(pnlMain);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.pack();

        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(null);
        this.setTitle("배민커넥트 로그인");

        this.getRootPane().setDefaultButton(btnLogin);
    }

    /*
     * @description Login Button
     * @author      jUqItEr (pyt773924@gmail.com)
     * */
    private void btnFindInfoClickListener() {
        new BMRiderFindInfoDialog();
    }

    /*
     * @description Login Button
     * @author      jUqItEr (pyt773924@gmail.com)
     * */
    private void btnLoginClickListener() {
        String riderId = txtLoginId.getText();
        String riderPwd = String.valueOf(txtLoginPassword.getPassword());

        if (riderId.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "아이디를 입력해주세요."
            );
            return;
        }  else if (riderPwd.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "비밀번호를 입력해주세요."
            );
            return;
        }

        try {
            String query = "SELECT RIDER_PASSWORD1, RIDER_PASSWORD2 FROM RIDER WHERE RIDER_ID=?";
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs;

            pstmt.setString(1, riderId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String md5 = rs.getString(1);
                String sha1 = rs.getString(2);
                String pwdMd5 = HashAlgorithm.makeHash(riderPwd, "md5");
                String pwdSha1 = HashAlgorithm.makeHash(riderPwd, "sha1");

                if (pwdMd5.equals(md5) && pwdSha1.equals(sha1)) {
                    new BMRiderMainForm(this, riderId);
                    this.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "비밀번호가 틀렸습니다."
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "아이디가 존재하지 않습니다."
                );
            }
            rs.close();
            pstmt.close();
            conn.close();
        } catch (SQLException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /*
         * @description Get Nimbus Look And Feel.
         * @author      jUqItEr (pyt773924@gmail.com)
         * */
        try {
            // Load the FontUIResources for default Java Swing UI.
            Enumeration<Object> keys = UIManager.getDefaults().keys();

            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);

                if (value instanceof FontUIResource) {
                    UIManager.put(key, new FontUIResource("Malgun Gothic", Font.PLAIN, 12));
                }
            }

            // Set Nimbus Theme.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            // Set the FontUIResources for Nimbus Theme.
            UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
            keys = uiDefaults.keys();

            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);

                if (value instanceof FontUIResource) {
                    uiDefaults.put(key, new FontUIResource("Malgun Gothic", Font.PLAIN, 12));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        EventQueue.invokeLater(() -> new BMRiderLoginForm().setVisible(true));
    }
}
