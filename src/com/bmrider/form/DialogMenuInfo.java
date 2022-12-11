package com.bmrider.form;

import com.bmrider.database.DBConnection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DialogMenuInfo extends JDialog {
    private final Connection conn = DBConnection.getConnection();

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton btnNext;
    private JPanel pnlMain;
    public JLabel imgLabel;
    private JTextField 메뉴번호;
    private JTextField 메뉴이름;
    private JTextField 메뉴가격;
    private JTextField 주문수량;
    private JLabel l_메뉴번호;
    private JLabel l_메뉴이름;
    private JLabel l_메뉴가격;
    private JLabel l_주문수량;
    private JButton btnPrev;

    public static int _deliveryNumber = 0;
    public static int _idx = 0;
    public ArrayList<Map<Object, Object>> menuList = new ArrayList<Map<Object, Object>>();

    public DialogMenuInfo(int DeliveryNumber) {

        this._deliveryNumber = DeliveryNumber;
        setContentPane(contentPane);
        setModal(true);
        setBounds(0, 0, 400, 400);
        getRootPane().setDefaultButton(buttonOK);

        btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_idx == 0) {
                    JOptionPane.showMessageDialog(null,
                            "첫 페이지 입니다!", "Message",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    _idx -= 1;
                    setMenu(_idx);
                }

            }
        });
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_idx == menuList.size() - 1) {
                    JOptionPane.showMessageDialog(null,
                            "마지막 페이지입니다!", "Message",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    _idx += 1;
                    setMenu(_idx);
                }

            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initializeComponents();
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        DialogMenuInfo dialog = new DialogMenuInfo(_deliveryNumber);
        dialog.pack();
        dialog.setVisible(true);

        System.exit(0);
    }

    private void initializeComponents() {
        imgLabel.setText("");
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select MENU_JSON from DELIVERY where DELIVERY_NUMBER='" + _deliveryNumber + "'");
            rs.next();
            String json = rs.getString(1);
            json = json.replaceAll(" ", "");
            json = json.substring(1); // { 제거
            json = json.substring(0, json.length() - 1); // } 제거
            rs.close();

            var list = json.split(",");
            for (int i = 0; i < list.length; i++) {
                var row = list[i].split("=");
                System.out.println(row);
                int menuNumber = Integer.parseInt(row[0]);
                String menuCount = row[1];
                rs = stmt.executeQuery("select * from MENU where MENU_NUMBER='" + menuNumber + "'");
                while (rs.next()) {
                    Map<Object, Object> obj = new HashMap<>();
                    obj.put("MENU_NUMBER", rs.getInt(1));
                    obj.put("MENU_NAME", rs.getString(2));
                    obj.put("MENU_PRICE", rs.getString(3));
                    obj.put("MENU_PHOTO", rs.getString(4));
                    obj.put("MENU_COUNT", menuCount);
                    menuList.add(obj);
                    if (_idx == 0) {
                        setMenu(_idx);
                    }
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void setMenu(int idx) {
        var obj = menuList.get(idx);
        메뉴번호.setText(obj.get("MENU_NUMBER").toString());
        메뉴이름.setText(obj.get("MENU_NAME").toString());
        메뉴가격.setText(obj.get("MENU_PRICE").toString());

        var menuCount = obj.get("MENU_COUNT").toString();
        주문수량.setText(menuCount);

        var menuPhoto = obj.get("MENU_PHOTO").toString();
        try {
            URL url = new URL(menuPhoto);
            var image = ImageIO.read(url);
            var rs_img = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            ImageIcon bsImg = new ImageIcon(rs_img);
            imgLabel.setIcon(bsImg);
            imgLabel.setBounds(30, 30, 100, 100);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
