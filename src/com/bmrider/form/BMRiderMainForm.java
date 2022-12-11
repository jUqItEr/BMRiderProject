package com.bmrider.form;

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

public class BMRiderMainForm extends JFrame {
    private JPanel pnlMain;
    private JTabbedPane tabMain;
    private JXMapViewer jXMapViewer;
    private JTabbedPane tabSubProcess;
    private JTable tblProcessNew;
    private JTable tblProcessCompleted;

    public BMRiderMainForm() {
        initializeComponents();

        try {
            getMyGeoLocation();
        } catch (GeoIp2Exception | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createUIComponents() {

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
        this.setContentPane(pnlMain);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setLayout(null);
        this.setResizable(false);
        this.setTitle("배민커넥트");
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

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new BMRiderMainForm().setVisible(true));
    }
}
