package bluetooth;

import javax.bluetooth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BluetoothSocket {
    private static final Object inquiryCompletedEvent = new Object();
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
            System.out.println("You know the name of the device to connect to? ");
            boolean theUserKnowsTheDeviceName = Boolean.valueOf(br.readLine());
            if (theUserKnowsTheDeviceName) {
                discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent,
                        br.readLine(), false);
                agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.wait();
                }
                cachedDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
                // check if the device is contained in the data structure, then, take the bluetooth address.
            } else {
                // Search near devices, add them into a dictionary, then, ask the user what is the
                // device to connect to and just get the value from the key. (is better a dictionary or an array???)
            }
        } else {
            System.out.print("Tell me the bluetooth address: ");
            deviceAddress = br.readLine();
        }


    }
}
