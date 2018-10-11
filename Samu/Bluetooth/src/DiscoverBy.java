import discoverer.DeviceDiscoverer;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;

public class DiscoverBy {
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
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean isBluetoothAddress;

            System.out.println(" Will you use a device name or a bluetooth address?(1 for device name, 2 for address)");
            String lineRead = br.readLine();
            while(!lineRead.equals("1") && !lineRead.equals("2")){
                System.out.println(" Please, introduce '1' to search by device name or '2' to search by bluetooth address.");
                lineRead = br.readLine();
            }
            isBluetoothAddress = lineRead.equals("2");
            if(isBluetoothAddress) {
                System.out.println(" Please, introduce bluetooth address:");
            } else {
                System.out.println(" Please, introduce device name:");
            }
            lineRead = br.readLine();

            System.out.println("\n=====Finding device=====");
            da.startInquiry(DiscoveryAgent.GIAC, new DeviceDiscoverer(INQUIRY_COMPLETED_EVENT, lineRead, isBluetoothAddress));
            synchronized (INQUIRY_COMPLETED_EVENT) {
                try {
                    INQUIRY_COMPLETED_EVENT.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (BluetoothStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}