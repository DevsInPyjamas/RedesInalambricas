package bluetooth;

import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class BluetoothSocket {
    private static final Object inquiryCompletedEvent = new Object();
    public static void main(String[] args) throws IOException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DeviceAndServiceDiscoverer discoveryListener;
        List<RemoteDevice> cachedDevices;
        String serverBluetoothAddress;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("You know the name or bluetooth address from the server? (true if the answer is yes): ");
        boolean theUserKnowsTheBluetoothAddress = Boolean.valueOf(br.readLine());
        if (!theUserKnowsTheBluetoothAddress) {
            System.out.print("You know the name of the device to connect to? ");
            boolean theUserKnowsTheDeviceName = Boolean.valueOf(br.readLine());
            if (theUserKnowsTheDeviceName) {
                System.out.print("What is the device name? ");
                discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent,
                        br.readLine(), false);
                System.out.println("============= \tSearching near devices\t =============");
                agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.wait();
                }
                cachedDevices = discoveryListener.getCachedDevices();
                if (cachedDevices.isEmpty()) {
                    System.err.println("Device not found...");
                    System.exit(-1);
                }
                serverBluetoothAddress = cachedDevices.get(0).getBluetoothAddress();
            } else {
                System.out.println("============= \tSearching near devices\t =============");
                discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent);
                agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.wait();
                }
                cachedDevices = discoveryListener.getCachedDevices();
                if (cachedDevices.isEmpty()) {
                    System.err.println("No devices found...");
                    System.exit(-1);
                }
                discoveryListener.printAllDevices();
                System.out.print("Tell me the number of the device you are going to connect: ");
                int indexOfDevice = Integer.valueOf(br.readLine()) - 1;
                serverBluetoothAddress = cachedDevices.get(indexOfDevice).getBluetoothAddress();
            }
        } else {
            System.out.print("Tell me the bluetooth address: ");
            serverBluetoothAddress = br.readLine();
        }
    }
}
