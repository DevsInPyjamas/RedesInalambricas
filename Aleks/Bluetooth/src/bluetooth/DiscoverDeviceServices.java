package bluetooth;

import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Remote;
import java.util.Enumeration;

public class DiscoverDeviceServices {

    private static final Object inquiryCompletedEvent = new Object();

    public static void main(String[] args) throws IOException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        RemoteDevice[] cachedDevices;
        DiscoveryListener discoveryListener;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Are you going to search all near devices (true) or 1 specific device (false)? ");
        Boolean input = Boolean.valueOf(br.readLine());
        String deviceName = null;
        String bluetoothAddress = null;

        if (input) {
            discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent);
            System.out.println("============= \tSearching near devices\t =============");
        } else {
            System.out.print("Are you searching a device by bluetooth address? (answer with true or false): ");
            boolean isAddress = Boolean.valueOf(br.readLine());
            if (isAddress) {
                System.out.print("Tell me the bluetooth address to filter (without : between bits): ");
                bluetoothAddress = br.readLine();
                discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent, bluetoothAddress, true);
            } else {
                System.out.print("Tell me the device name to filter: ");
                deviceName = br.readLine();
                discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent, deviceName, false);
            }
            System.out.println("============= \tSearching device\t =============");
        }
        agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        synchronized (inquiryCompletedEvent) {
            try {
                inquiryCompletedEvent.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cachedDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
        if (cachedDevices != null) {
            for (RemoteDevice device : cachedDevices) {
                // 0x1002 all UUID
                // 0x1105 OBEX Object Push
                if (input) {
                    agent.searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1002)}, device,
                            discoveryListener);
                    System.out.println("  > " + device.getFriendlyName(false));
                    synchronized (inquiryCompletedEvent) {
                        inquiryCompletedEvent.wait();
                    }
                } else if (device.getBluetoothAddress().equalsIgnoreCase(bluetoothAddress) ||
                            device.getFriendlyName(false).equalsIgnoreCase(deviceName)){
                    agent.searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1002)}, device,
                            discoveryListener);
                    System.out.println("  > " + device.getFriendlyName(false));
                    synchronized (inquiryCompletedEvent) {
                        inquiryCompletedEvent.wait();
                    }
                }
                System.out.println();
            }
        }
    }
}
