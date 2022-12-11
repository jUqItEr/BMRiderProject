package com.bmrider.form;

import com.bmrider.base.JRoundedPasswordField;
import com.bmrider.base.JRoundedTextField;
import com.bmrider.database.DBConnection;
import com.bmrider.security.HashAlgorithm;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Location;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class BMRiderMainForm extends JFrame {
    private JPanel pnlMain;
    private JTabbedPane tabMain;
    private JXMapViewer jXMapViewer;
    private JTabbedPane tabSubProcess;
    private JButton btnWithdrawal;
    private JPanel pnlLogo;
    private JRoundedTextField txtRiderId;
    private JRoundedPasswordField txtRiderPassword;
    private JRoundedPasswordField txtRiderPasswordConfirm;
    private JRoundedTextField txtRiderName;
    private JRoundedTextField txtRiderAddress;
    private JRoundedTextField txtRiderPhoneNumber;
    private JButton btnChange;
    private JTable tblProcess;
    private JPanel tabProcess;
    private JScrollPane scrollPaneProcess;
    private JButton btnRefresh;
    private JButton btnAccept;

    private JFrame _parent;
    private String _riderId;

    private Object[][] tableList;

    private final String[] indexes = {
            "비밀번호를", "비밀번호 확인을", "이름을", "주소를", "휴대폰 번호를"
    };

    private final HashMap<String, Integer> _dict;

    public BMRiderMainForm() {
        this._dict = new HashMap<>();

        this._dict.put("미정", -1);
        this._dict.put("대기 중", 0);
        this._dict.put("배달 중", 1);
        this._dict.put("완료", 2);

        initializeComponents();

        try {
            getMyGeoLocation();
        } catch (GeoIp2Exception | IOException ex) {
            ex.printStackTrace();
        }
    }

    public BMRiderMainForm(JFrame parent, String riderId) {
        this._dict = new HashMap<>();

        this._dict.put("미정", -1);
        this._dict.put("대기 중", 0);
        this._dict.put("배달 중", 1);
        this._dict.put("완료", 2);

        this._parent = parent;
        this._riderId = riderId;

        initializeComponents();

        try {
            getMyGeoLocation();
        } catch (GeoIp2Exception | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createUIComponents() {
        txtRiderAddress = new JRoundedTextField(5);
        txtRiderId = new JRoundedTextField(5);
        txtRiderName = new JRoundedTextField(5);
        txtRiderPassword = new JRoundedPasswordField(5);
        txtRiderPasswordConfirm = new JRoundedPasswordField(5);
        txtRiderPhoneNumber = new JRoundedTextField(5);

        pnlLogo = new JPanel() {
            final Image background = new ImageIcon("src/com/bmrider/res/drawable/bg_profile.png")
                    .getImage();

            @Override
            protected void paintComponent(Graphics g) {
                Dimension dim = getSize();

                super.paintComponent(g);
                g.drawImage(background, 0, 0, dim.width, dim.height, this);
            }
        };
    }
    /*
     * @description This Function will get your GeoLocation by IP and Map Data.
     * @author      jUqItEr (pyt773924@gmail.com)
     * @version     1.0.1
     * */
    private void getMyGeoLocation() throws IOException, GeoIp2Exception {
        // Get your IP from Amazonaws check ip homepage.
        // And then,
        URL url = new URL("http://checkip.amazonaws.com/");
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        File database = new File("src/com/bmrider/res/embedded/GeoLite2-City.mmdb");
        DatabaseReader reader = new DatabaseReader.Builder(database).build();
        InetAddress ipAddress = InetAddress.getByName(br.readLine());
        CityResponse response = reader.city(ipAddress);
        Location location = response.getLocation();

        loadMap(location.getLatitude(), location.getLongitude());
    }

    private void initializeComponents() {
        btnAccept.addActionListener(e -> btnAcceptClickListener());
        btnChange.addActionListener(e -> btnChangeClickListener());
        btnRefresh.addActionListener(e -> btnRefreshClickListener());
        btnWithdrawal.addActionListener(e -> btnWithdrawalClickListener());

        this.setContentPane(pnlMain);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setLayout(null);
        this.setResizable(false);
        this.setTitle("배민커넥트");

        loadRiderInformation();

        setTabProcess();
    }

    private void loadMap(double latitude, double longitude) {
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        GeoPosition geo = new GeoPosition(latitude, longitude);
        MouseInputListener mouseInputListener = new PanMouseInputListener(jXMapViewer);

        jXMapViewer.setTileFactory(tileFactory);
        jXMapViewer.setAddressLocation(geo);
        jXMapViewer.setZoom(3);
        jXMapViewer.addMouseListener(mouseInputListener);
        jXMapViewer.addMouseMotionListener(mouseInputListener);
        jXMapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(jXMapViewer));
    }

    private void loadRiderInformation() {
        try {
            String query = "SELECT RIDER_ADDRESS, RIDER_NAME, RIDER_PHONE FROM RIDER WHERE RIDER_ID='" +
                    this._riderId + "'";
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String address = rs.getString(1);
                String name = rs.getString(2);
                String phone = rs.getString(3);

                txtRiderAddress.setText(address);
                txtRiderName.setText(name);
                txtRiderPhoneNumber.setText(phone);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        txtRiderId.setText(this._riderId);
    }

    private void onRefresh() {
        final String[] headerNew = {
                "배달 번호", "배송 주소", "배달 상태", "결제 방법", "가게 이름", "메뉴 정보"
        };
        DefaultTableModel defaultTableModel = (DefaultTableModel)tblProcess.getModel();
        defaultTableModel.setRowCount(0);

        tableList = getProcessIncompleteList();

        for (Object[] data : tableList) {
            defaultTableModel.addRow(data);
        }
    }

    private void setTabProcess() {
        final String[] headerNew = {
                "배달 번호", "배송 주소", "배달 상태", "결제 방법", "가게 이름", "메뉴 정보"
        };

        tabProcess.setLayout(null);
        tableList = getProcessIncompleteList();

        DefaultTableModel defaultTableModel = new DefaultTableModel(tableList, headerNew);
        tblProcess.setModel(defaultTableModel);
        tblProcess.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblProcess.rowAtPoint(e.getPoint());
                int col = tblProcess.columnAtPoint(e.getPoint());

                if (col == 5) {
                    int number = Integer.parseInt(tableList[row][0].toString());
                    new DialogMenuInfo(number).setVisible(true);
                }
            }
        });
    }

    private Object[][] getProcessAllList() {
        ArrayList<ArrayList<Object>> tuples = new ArrayList<>();
        Object[][] content;

        try {
            String query = "SELECT * FROM DELIVERY";
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                ArrayList<Object> tuple = new ArrayList<>();
                int deliveryMethod = rs.getInt(3);
                int paymentMethod = rs.getInt(4);

                tuple.add(rs.getString(1));
                tuple.add(rs.getString(2));

                switch (deliveryMethod) {
                    case 0:
                        tuple.add("대기 중");
                        break;
                    case 1:
                        tuple.add("배달 중");
                        break;
                    case 2:
                        tuple.add("완료");
                        break;
                    default:
                        tuple.add("미정");
                        break;
                }
                switch (paymentMethod) {
                    case 0:
                        tuple.add("현금");
                        break;
                    case 1:
                        tuple.add("카드 결제");
                        break;
                    case 2:
                        tuple.add("만나서 현금 결제");
                        break;
                    case 3:
                        tuple.add("만나서 카드 결제");
                        break;
                    default:
                        tuple.add("미정");
                        break;
                }
                tuple.add(rs.getString(5));
                tuple.add("상세 보기");
                tuple.add(rs.getObject(6));

                tuples.add(tuple);
            }
            content = new Object[tuples.size()][7];

            for (int i = 0; i < tuples.size(); ++i) {
                content[i] = tuples.get(i).toArray(new Object[4]);
            }
            rs.close();
            stmt.close();
            conn.close();

            return content;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new Object[0][0];
    }
    
    private Object[][] getProcessIncompleteList() {
        ArrayList<ArrayList<Object>> tuples = new ArrayList<>();
        Object[][] base = tableList != null ? tableList : getProcessAllList();
        Object[][] result;

        for (Object[] datum : base) {
            ArrayList<Object> tuple = new ArrayList<>();
            String state = datum[2].toString();
            boolean isAppended = false;

            if (state.equals("대기 중") ||
                    (state.equals("배달 중") && Objects.requireNonNull(datum[6].toString()).equals(this._riderId))) {
                tuple.addAll(Arrays.asList(datum));
                isAppended = true;
            }
            if (isAppended) {
                tuples.add(tuple);
            }
        }
        result = new Object[tuples.size()][7];

        for (int i = 0; i < tuples.size(); ++i) {
            result[i] = tuples.get(i).toArray(new Object[4]);
        }
        return result;
    }

    private void btnAcceptClickListener() {
        int selectedIndex = tblProcess.getSelectedRow();

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "주문을 수락하려면 최소 한 행은 선택해야 합니다.",
                    "SYSTEM",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        DefaultTableModel model = (DefaultTableModel)tblProcess.getModel();
        Object[] vector = model.getDataVector()
                .elementAt(tblProcess.convertColumnIndexToModel(selectedIndex)).toArray();
        int state = this._dict.get(vector[2].toString());
        int number = Integer.parseInt(vector[0].toString());

        if (state == 1) {
            JOptionPane.showMessageDialog(
                    this,
                    "이미 배달 중인 상품은 수락할 수 없습니다.",
                    "SYSTEM",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            String query = "{call P_RIDER_DELIVERY_MANAGE(?,?,?) }";
            Connection conn = DBConnection.getConnection();
            CallableStatement cstmt = conn.prepareCall(query);

            // Make transaction!!!
            conn.setAutoCommit(false);

            cstmt.setInt(1, number);
            cstmt.setString(2, this._riderId);
            cstmt.setInt(3, 1);

            cstmt.executeQuery();

            conn.commit();

            cstmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        onRefresh();
    }

    private void btnChangeClickListener() {
        String pwd = String.valueOf(txtRiderPassword.getPassword());
        String pwdConfirm = String.valueOf(txtRiderPasswordConfirm.getPassword());
        String address = txtRiderAddress.getText();
        String name = txtRiderName.getText();
        String phone = txtRiderPhoneNumber.getText();
        String[] infos = {
                pwd, pwdConfirm, name, address, phone
        };

        for (int i = 0; i < infos.length; ++i) {
            if (infos[i].isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        String.format("%s 입력해주세요.", indexes[i]),
                        "SYSTEM",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        }
        if (!pwd.equals(pwdConfirm)) {
            JOptionPane.showMessageDialog(
                    this,
                    "입력한 비밀번호와 비밀번호 확인란이 다릅니다.",
                    "SYSTEM",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
         try {
             String query = "UPDATE RIDER SET RIDER_PASSWORD1=?, RIDER_PASSWORD2=?, RIDER_ADDRESS=?, RIDER_NAME=?, " +
                     "RIDER_PHONE=? WHERE RIDER_ID=?";
             String pwdMd5 = HashAlgorithm.makeHash(pwd, "md5");
             String pwdSha1 = HashAlgorithm.makeHash(pwd, "sha1");
             Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);

             pstmt.setString(1, pwdMd5);
             pstmt.setString(2, pwdSha1);
             pstmt.setString(3, address);
             pstmt.setString(4, name);
             pstmt.setString(5, phone);
             pstmt.setString(6, this._riderId);

             if (pstmt.executeUpdate() > 0) {
                 JOptionPane.showMessageDialog(
                         this,
                         "정보 수정이 완료되었습니다.",
                         "Oracle Manager",
                         JOptionPane.INFORMATION_MESSAGE
                 );
                 txtRiderPassword.setText("");
                 txtRiderPasswordConfirm.setText("");
             }
             pstmt.close();
             conn.close();
         } catch (SQLException | NoSuchAlgorithmException ex) {
             ex.printStackTrace();
         }
    }

    private void btnRefreshClickListener() {
        onRefresh();
    }

    private void btnWithdrawalClickListener() {
        boolean isTransactionValidate = false;
        int result = JOptionPane.showConfirmDialog(
                this,
                "정말로 회원 정보를 삭제하시겠습니까?",
                "SYSTEM",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM RIDER WHERE RIDER_ID=?";
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);

                pstmt.setString(1, this._riderId);

                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "회원이 삭제되었습니다.",
                            "Oracle Manager",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    isTransactionValidate = true;
                }
                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        if (isTransactionValidate) {
            this._parent.setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new BMRiderMainForm().setVisible(true));
    }
}
