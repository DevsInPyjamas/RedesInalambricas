package bluetooth;


import javax.bluetooth.*;


public class LocalInfoAndAllNearDevices {

    private static final Object inquiryCompletedEvent = new Object();

    public static void main(String[] args) throws BluetoothStateException, InterruptedException {
        /* Show local device bluetooth information (Exercise 2.1) */
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Local Address: " + localDevice.getBluetoothAddress());
        System.out.println("Device Class: " + localDevice.getDeviceClass());
        System.out.println("Friendly Name: " + localDevice.getFriendlyName());
        System.out.println("Discoverable Mode: " + localDevice.getDiscoverable());
        /*
        * Discover all near devices (Exercise 3.2)
        * */
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        DiscoveryListener discoveryListener = new DeviceAndServiceDiscoverer(inquiryCompletedEvent);
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
