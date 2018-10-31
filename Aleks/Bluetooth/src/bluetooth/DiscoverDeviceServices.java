package bluetooth;

import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DiscoverDeviceServices {

    private static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args) throws IOException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        List<RemoteDevice> cachedDevices;
        DiscoveryListener discoveryListener;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Are you going to search all services from near devices (true) or services from " +
                "1 specific device (false)? ");
        Boolean searchNearDevices = Boolean.valueOf(br.readLine());
        String deviceName;
        String bluetoothAddress;
        System.out.print("Are you going to search Serial ports too? (true if the answer is yes): ");
        boolean searchSerialPortsToo = Boolean.valueOf(br.readLine());

        if (searchNearDevices) {
            discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("============= \tSearching near devices\t =============");
        } else {
            System.out.print("Are you searching a device by bluetooth address? (answer with true or false): ");
            boolean isAddress = Boolean.valueOf(br.readLine());
            if (isAddress) {
                System.out.print("Tell me the bluetooth address to filter (without : between bits): ");
                bluetoothAddress = br.readLine();
                discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT, bluetoothAddress, true);
            } else {
                System.out.print("Tell me the device name to filter: ");
                deviceName = br.readLine();
                discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT, deviceName, false);
            }
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
        cachedDevices = ((DeviceAndServiceDiscoverer) discoveryListener).getCachedDevices();
        UUID[] uuids;
        if (searchSerialPortsToo) {
            uuids = new UUID[] {
                new UUID(0x1002), new UUID(0x1101)
            };
        } else {
            uuids = new UUID[] { new UUID(0x1002)};
        }
        for (RemoteDevice device : cachedDevices) {
                agent.searchServices(new int[]{0x0100}, uuids, device,
                        discoveryListener);
                try {
                    System.out.println("  > " + device.getFriendlyName(false));

                } catch (IOException e) {
                    System.out.println("  > Unnamed device");
                }
                synchronized (INQUIRY_COMPLETED_EVENT) {
                    INQUIRY_COMPLETED_EVENT.wait();
                }
            System.out.println();
        }
        ((DeviceAndServiceDiscoverer) discoveryListener).printAllServices();
    }
}
