import bluetooth.DeviceSeeker;

import javax.bluetooth.*;
import java.io.IOException;

public class Main {

    private static final Object inquiryCompletedEvent = new Object();

    public static void main(String[] args) throws BluetoothStateException, InterruptedException {
        /* Show local device bluetooth information (Exercise 2.1) */
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Local Address: " + localDevice.getBluetoothAddress());
        System.out.println("Device Class: " + localDevice.getDeviceClass());
        System.out.println("Friendly Name: " + localDevice.getFriendlyName());
        System.out.println("Discoverable Mode: " + localDevice.getDiscoverable());
        /*
        * Discover all near devices (Exercise 3.2)
        * */
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener = new DeviceSeeker(inquiryCompletedEvent);
        System.out.println("============= \tSearching devices\t =============");
        agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        synchronized (inquiryCompletedEvent) {
            try {
                inquiryCompletedEvent.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /* Discover a device using Name or Bluetooth address (exercise 3.3) */

        /* SERVICE DISCOVERY FOR ALL NEAR DEVICES (exercise 4.5) */
        RemoteDevice[] cachedDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
        if (cachedDevices != null) {
            for (RemoteDevice device : cachedDevices) {
                agent.searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1002)}, device, discoveryListener);
                try {
                    System.out.println("\t> " + device.getFriendlyName(false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.wait();
                }
                System.out.println();
            }
        } else {
            System.err.println("No devices found");
        }
    }
}
