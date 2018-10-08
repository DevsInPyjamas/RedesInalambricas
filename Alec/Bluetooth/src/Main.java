import javax.bluetooth.*;
import java.io.IOException;

public class Main {
    private static final Object inquiryCompletedEvent = new Object();
    public static void main(String[] args) throws BluetoothStateException, InterruptedException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Local Address: " + localDevice.getBluetoothAddress());
        System.out.println("Device Class: " + localDevice.getDeviceClass());
        System.out.println("Friendly Name: " + localDevice.getFriendlyName());
        System.out.println("Discoverable Mode: " + localDevice.getDiscoverable());

        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                try {
                    System.out.printf("    – %s (%s)\n", btDevice.getFriendlyName(false), btDevice.getBluetoothAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (ServiceRecord s : servRecord) {
                    DataElement serviceName = s.getAttributeValue(0x0100);
                    if (serviceName != null) {
                        System.out.println((String) serviceName.getValue());
                    } else {
                        System.out.println("    – Service without name.");
                    }
                    System.out.println(s.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.println("    " + respCode);
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                System.out.println("============= Process Done... =============");
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }
        };
        System.out.println("============= Searching devices... =============");
        agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
        synchronized (inquiryCompletedEvent) {
            try {
                inquiryCompletedEvent.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (RemoteDevice device : agent.retrieveDevices(DiscoveryAgent.CACHED)) {
            agent.searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1002)}, device, discoveryListener);
            try {
                System.out.println("  > " + device.getFriendlyName(false));
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (inquiryCompletedEvent) {
                inquiryCompletedEvent.wait();
            }
            System.out.println();

        }
    }
}
