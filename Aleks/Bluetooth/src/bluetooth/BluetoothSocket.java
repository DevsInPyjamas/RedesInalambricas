package bluetooth;

import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BluetoothSocket {
    private static final Object INQUIRY_COMPLETED_EVENT = new Object();
    public static void main(String[] args) throws IOException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener;
        String deviceAddress;
        RemoteDevice[] cachedDevices;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("You know the name or bluetooth address from the server? (true if the answer is yes): ");
        boolean theUserKnowsTheBluetoothAddress = Boolean.valueOf(br.readLine());
        if (!theUserKnowsTheBluetoothAddress) {
            System.out.print("You know the name of the device to connect to? (true if the answer is yes): ");
            boolean theUserKnowsTheDeviceName = Boolean.valueOf(br.readLine());
            if (theUserKnowsTheDeviceName) {
                System.out.print("What is the name of the device? ");
                String deviceName = br.readLine();
                discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT, deviceName,
                        false);
                agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
                synchronized (INQUIRY_COMPLETED_EVENT) {
                    INQUIRY_COMPLETED_EVENT.wait();
                }
                cachedDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
                deviceAddress = getBluetoothAddress(cachedDevices, deviceName);
                if(deviceAddress == null) {
                    System.err.println("Device not found...");
                    System.exit(-1);
                }
            } else {
                discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT);
                System.out.println("============= \tSearching near devices\t =============");
                agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
                synchronized (INQUIRY_COMPLETED_EVENT) {
                    try {
                        INQUIRY_COMPLETED_EVENT.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ((DeviceAndServiceDiscoverer) discoveryListener).printAllDevices();
                System.out.print("Tell me the number of the device to connect: ");
                int number = Integer.valueOf(br.readLine()) - 1;
                deviceAddress = agent.retrieveDevices(DiscoveryAgent.CACHED)[number].getBluetoothAddress();
            }
        } else {
            System.out.print("Tell me the bluetooth address: ");
            deviceAddress = br.readLine();
        }
    }

    private static String getBluetoothAddress(RemoteDevice[] cachedDevices, String deviceName) throws IOException {
        int i = 0;
        while(i < cachedDevices.length && !cachedDevices[i].getFriendlyName(false).equalsIgnoreCase(deviceName)) {
            i++;
        }
        if (i < cachedDevices.length) return cachedDevices[i].getBluetoothAddress(); else return null;
    }
}
