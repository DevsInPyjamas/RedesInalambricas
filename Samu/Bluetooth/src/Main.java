import discoverer.DeviceDiscoverer;

import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {
    static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args) throws InterruptedException, IOException {
        LocalDevice device = null;
        try {
            device = LocalDevice.getLocalDevice();
            List<RemoteDevice> cachedDevices;

            // Ex 1
            System.out.println("=====Device data=====");
            System.out.println("Local Address: " + device.getBluetoothAddress());
            System.out.println("Device Class: " + device.getDeviceClass());
            System.out.println("Discoverable: " + device.getDiscoverable());
            System.out.println("Friendly name: " + device.getFriendlyName());
            System.out.println("BlueCove version: " + LocalDevice.getProperty("bluecove"));

            // Ex 2, 3, 4, 5
            DiscoveryAgent da = device.getDiscoveryAgent();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            boolean isBluetoothAddress, globalSearch, searchSerialPortsToo;
            DeviceDiscoverer deviceDiscoverer;

            System.out.println(" Would you like to find one or every device?(1 for single, 2 for all)");
            String lineRead = br.readLine();
            lineRead = getSystemResponse(br, lineRead, "Please, introduce 1 for single search or 2 for global search.");
            globalSearch = lineRead.equals("2");
            if(!globalSearch) {
                System.out.println(" Will you use a device name or a bluetooth address?(1 for device name, 2 for address)");
                lineRead = br.readLine();
                lineRead = getSystemResponse(br, lineRead, "Please, introduce 1 or 2");
                isBluetoothAddress = lineRead.equals("2");
                if (isBluetoothAddress) {
                    System.out.println(" Please, introduce bluetooth address:");
                } else {
                    System.out.println(" Please, introduce device name:");
                }
                lineRead = br.readLine();

                deviceDiscoverer = new DeviceDiscoverer(INQUIRY_COMPLETED_EVENT, lineRead, isBluetoothAddress);
            } else {
                deviceDiscoverer = new DeviceDiscoverer(INQUIRY_COMPLETED_EVENT);
            }

            System.out.println("Would you like to search serial ports too?(Y for yes, anything for no)");
            searchSerialPortsToo = br.readLine().toUpperCase().equals("Y");

            System.out.println("\n=====Finding device=====");
            da.startInquiry(DiscoveryAgent.GIAC, deviceDiscoverer);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                try {
                    INQUIRY_COMPLETED_EVENT.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cachedDevices = deviceDiscoverer.getCachedDevices();
            UUID[] uuids;
            if (searchSerialPortsToo) {
                uuids = new UUID[] {
                        new UUID(0x1002), new UUID(0x1101)
                };
            } else {
                uuids = new UUID[] { new UUID(0x1002)};
            }
            for (RemoteDevice rd : cachedDevices) {
                da.searchServices(new int[]{0x0100}, uuids, rd, deviceDiscoverer);
                synchronized (INQUIRY_COMPLETED_EVENT) {
                    INQUIRY_COMPLETED_EVENT.wait();
                }
            }


        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }

    private static String getSystemResponse(BufferedReader br, String lineRead, String systemResponse) throws IOException {
        while (!lineRead.equals("1") && !lineRead.equals("2")) {
            System.out.printf(" %s\n", systemResponse);
            lineRead = br.readLine();
        }
        return lineRead;
    }

}
