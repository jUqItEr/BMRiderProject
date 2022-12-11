package com.bmrider.form;

import com.bmrider.base.JRoundedPasswordField;
import com.bmrider.base.JRoundedTextField;
import com.bmrider.database.DBConnection;
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
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.sql.*;

public class BMRiderMainForm extends JFrame {
    private JPanel pnlMain;
    private JTabbedPane tabMain;
    private JXMapViewer jXMapViewer;
    private JTabbedPane tabSubProcess;
    private JTable tblProcessNew;
    private JTable tblProcessCompleted;
    private JButton btnWithdrawal;
    private JPanel pnlLogo;
    private JRoundedTextField txtRiderId;
    private JRoundedPasswordField txtRiderPassword;
    private JRoundedPasswordField txtRiderPasswordConfirm;
    private JRoundedTextField txtRiderName;
    private JRoundedTextField txtRiderAddress;
    private JRoundedTextField txtRiderPhoneNumber;
    private JButton btnChange;

    private String _riderId;

    public BMRiderMainForm() {
        initializeComponents();

        try {
            getMyGeoLocation();
        } catch (GeoIp2Exception | IOException ex) {
            ex.printStackTrace();
        }
    }

    public BMRiderMainForm(String riderId) {
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
        btnChange.addActionListener(e -> btnChangeClickListener());
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

    private void btnChangeClickListener() {
        txtRiderPassword.setText("");
        txtRiderPasswordConfirm.setText("");
    }

    private void btnWithdrawalClickListener() {
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

                conn.setAutoCommit(false);

                pstmt.setString(1, this._riderId);

                if (pstmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "회원이 삭제되었습니다.",
                            "Oracle Manager",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
                conn.commit();

                pstmt.close();
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            new BMRiderLoginForm();
            this.setVisible(false);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new BMRiderMainForm().setVisible(true));
    }
}
