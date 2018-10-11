import discoverer.DeviceDiscoverer;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;

public class DiscoverAll {
    static final Object INQUIRY_COMPLETED_EVENT = new Object();//remove
    public static void main(String[] args) {
        LocalDevice device = null;
        try {
            device = LocalDevice.getLocalDevice();

            // Ex 1
            System.out.println("=====Device data=====");
            System.out.println("Local Address: " + device.getBluetoothAddress());
            System.out.println("Device Class: " + device.getDeviceClass());
            System.out.println("Discoverable: " + device.getDiscoverable());
            System.out.println("Friendly name: " + device.getFriendlyName());
            System.out.println("BlueCove version: " + LocalDevice.getProperty("bluecove"));

            // Ex 2
            DiscoveryAgent da = device.getDiscoveryAgent();
            System.out.println("\n=====Finding devices=====");
            da.startInquiry(DiscoveryAgent.GIAC, new DeviceDiscoverer(INQUIRY_COMPLETED_EVENT));
            synchronized (INQUIRY_COMPLETED_EVENT) {
                try {
                    INQUIRY_COMPLETED_EVENT.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }
}
