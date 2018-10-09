package bluetooth;

import sun.nio.ch.IOUtil;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class DiscoverBy {

    private static final Object inquiryCompletedEvent = new Object();

    public static void main(String[] args) throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        System.out.print("Are you searching a device by name? (answer with true or false): ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean isAddress = Boolean.valueOf(br.readLine());
        if (isAddress) {
            System.out.print("Tell me the bluetooth address to filter (without : between bits): ");
        } else {
            System.out.print("Tell me the device name to filter: ");
        }
        String input = br.readLine();
        DiscoveryListener discoveryListener = new DeviceSeeker(inquiryCompletedEvent, input, isAddress);
        System.out.println("============= \tSearching devices\t =============");
        agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        synchronized (inquiryCompletedEvent) {
            try {
                inquiryCompletedEvent.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
