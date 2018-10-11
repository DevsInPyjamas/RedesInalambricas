package bluetooth;

import javax.bluetooth.*;
import java.io.IOException;
import java.rmi.Remote;
import java.util.Enumeration;

public class DiscoverDeviceServices {

    private static final Object inquiryCompletedEvent = new Object();

    public static void main(String[] args) throws IOException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent);
        agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        synchronized (inquiryCompletedEvent) {
            try {
                inquiryCompletedEvent.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        RemoteDevice[] cachedDevices = agent.retrieveDevices(DiscoveryAgent.CACHED);
        if (cachedDevices != null) {
            for (RemoteDevice device : cachedDevices) {
                // 0x1002 all UUID
                // 0x1105 OBEX Object Push
                agent.searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1002)}, device,
                        discoveryListener);
                System.out.println("  > " + device.getFriendlyName(false));
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.wait();
                }
                System.out.println();
            }
        }
    }
}
