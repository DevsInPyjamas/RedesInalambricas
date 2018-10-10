import javax.bluetooth.*;
import java.io.IOException;

public class Main {
    static final Object INQUIRY_COMPLETED_EVENT = new Object();
    public static void main(String[] args) {
        LocalDevice device = null;
        try {
            device = LocalDevice.getLocalDevice();

            // Ex 1
            System.out.println("Local Address: " + device.getBluetoothAddress());
            System.out.println("Device Class: " + device.getDeviceClass());
            System.out.println("Discoverable: " + device.getDiscoverable());
            System.out.println("Friendly name: " + device.getFriendlyName());
            System.out.println("BlueCove version: " + LocalDevice.getProperty("bluecove"));

            // Ex 2
            DiscoveryAgent da = device.getDiscoveryAgent();
            DiscoveryListener dl = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
                    try {
                        System.out.printf("- %s (%s)\n", remoteDevice.getFriendlyName(false), remoteDevice.getBluetoothAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void servicesDiscovered(int i, ServiceRecord[] serviceRecords) {
                    for (ServiceRecord record :
                            serviceRecords) {
                        DataElement de = record.getAttributeValue(0x0100);
                        if(de!=null){
                            System.out.printf("- %s\n", (String)de.getValue());
                        } else {
                            System.out.println("- Service without name -");
                        }
                        System.out.println(record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
                    }
                }

                @Override
                public void serviceSearchCompleted(int i, int i1) {
                    synchronized (INQUIRY_COMPLETED_EVENT){
                        INQUIRY_COMPLETED_EVENT.notifyAll();
                    }
                }

                @Override
                public void inquiryCompleted(int i) {
                    synchronized (INQUIRY_COMPLETED_EVENT){
                        INQUIRY_COMPLETED_EVENT.notifyAll();
                    }
                }
            };
            da.startInquiry(DiscoveryAgent.GIAC, dl);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                try {
                    INQUIRY_COMPLETED_EVENT.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }
}
