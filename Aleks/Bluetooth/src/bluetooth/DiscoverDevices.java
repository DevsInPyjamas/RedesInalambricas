package bluetooth;


import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class DiscoverDevices {

    private static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args) throws IOException {
        /* Show local device bluetooth information (Exercise 2.1) */
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Local Address: " + localDevice.getBluetoothAddress());
        System.out.println("Device Class: " + localDevice.getDeviceClass());
        System.out.println("Friendly Name: " + localDevice.getFriendlyName());
        System.out.println("Discoverable Mode: " + localDevice.getDiscoverable());

        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Are you going to search all near devices (true) or 1 specific device (false)? ");
        Boolean input = Boolean.valueOf(br.readLine());
        if (input) {
            /*
             * Search all near devices
             */
            System.out.println("Searching all near devices");
            discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("============= \tSearching near devices\t =============");
        } else {
            /*
             * Search specific device
             */
            System.out.print("Are you searching a device by bluetooth address? (answer with true or false): ");
            boolean isAddress = Boolean.valueOf(br.readLine());
            if (isAddress) {
                System.out.print("Tell me the bluetooth address to filter (without : between bits): ");
            } else {
                System.out.print("Tell me the device name to filter: ");
            }
            String input2 = br.readLine();
            discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT, input2, isAddress);
            System.out.println("============= \tSearching device\t =============");
        }
        agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        synchronized (INQUIRY_COMPLETED_EVENT) {
            try {
                INQUIRY_COMPLETED_EVENT.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ((DeviceAndServiceDiscoverer) discoveryListener).printAllDevices();
    }
}
