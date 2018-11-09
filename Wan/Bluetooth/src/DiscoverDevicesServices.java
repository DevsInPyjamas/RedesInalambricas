import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DiscoverDevicesServices {
    private static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args) throws IOException, InterruptedException {
        LocalDevice ld = LocalDevice.getLocalDevice();

        System.out.println("\n----------------------------------------------------");

        System.out.println("Local Device: " + ld.getBluetoothAddress());
        System.out.println("Device Class: " + ld.getDeviceClass());
        System.out.println("Friendly Name: " + ld.getFriendlyName());
        System.out.println("Discoverable Mode: " + ld.getDiscoverable());
        System.out.println("BlueCove version: " + ld.getProperty("bluecove"));

        System.out.println("----------------------------------------------------\n");

        DiscoveryAgent agent = ld.getDiscoveryAgent();
        DiscoveryListener dl;
        String bluetoothAddress, deviceName;
        List<RemoteDevice> cachedDevices;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Do you want to search all near devices (true) or an specific one (false)?:");
        Boolean allDevices = Boolean.valueOf(br.readLine());
        System.out.println("Do you also want to search Serial Ports? (true if the answer is yes)");
        Boolean serialPortsSearch = Boolean.valueOf(br.readLine());

        if (allDevices) {
            dl = new DevSerDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("\nSearching all nearby devices...\n");
        } else {
            System.out.println("Do you want to search the device by its name? (true or false):");
            boolean isName = Boolean.valueOf(br.readLine());

            if (isName) {
                System.out.println("\nWrite here the device name: ");
                deviceName = br.readLine();
                dl = new DevSerDiscoverer(INQUIRY_COMPLETED_EVENT, deviceName, true);
            } else {
                System.out.println("\nA search using the bluetooth address is going to be used.");
                System.out.println("Write here the bluetooth address:");
                bluetoothAddress = br.readLine();
                dl = new DevSerDiscoverer(INQUIRY_COMPLETED_EVENT, bluetoothAddress, false);
            }

            System.out.println("\nSearching device...\n");
        }

        agent.startInquiry(DiscoveryAgent.GIAC, dl);

        synchronized (INQUIRY_COMPLETED_EVENT) {
            try {
                INQUIRY_COMPLETED_EVENT.wait();
            } catch (Exception e) {
            }
        }

        ((DevSerDiscoverer) dl).printDevices();
        cachedDevices = ((DevSerDiscoverer) dl).getCachedDevices();

        if (cachedDevices != null) {
            UUID[] uuids;
            if (serialPortsSearch) {
                uuids = new UUID[]{new UUID(0x1002), new UUID(0x1101)};
            } else {
                uuids = new UUID[]{new UUID(0x1002)};
            }

            System.out.println("\n============ SERVICES ===========\n");
            int numServices = 1;

            for (RemoteDevice dev : cachedDevices) {
                agent.searchServices(new int[]{0x0100}, uuids, dev, dl);

                try {
                    System.out.println(numServices + ". " + dev.getFriendlyName(false));
                } catch (IOException e) {
                    System.out.println(numServices + ". UNNAMED DEVICE");
                }

                numServices++;

                synchronized (INQUIRY_COMPLETED_EVENT) {
                    INQUIRY_COMPLETED_EVENT.wait();
                }
                System.out.println();
            }
            System.out.println("\n====================================\n");
        }
    }
}