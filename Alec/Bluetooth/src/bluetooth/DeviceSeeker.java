package bluetooth;

import javax.bluetooth.*;
import java.io.IOException;

public class DeviceSeeker implements DiscoveryListener {
    private final Object inquiryCompletedEvent;
    private String deviceName = null;
    private String bluetoothAddress = null;

    public DeviceSeeker() {
        inquiryCompletedEvent = new Object();
    }

    public DeviceSeeker(Object inquiryCompletedEvent) {
        this.inquiryCompletedEvent = inquiryCompletedEvent;
    }

    public DeviceSeeker(Object inquiryCompletedEvent, String str, boolean isBluetoothAddress){
        this.inquiryCompletedEvent = inquiryCompletedEvent;
        if (isBluetoothAddress) bluetoothAddress = str.toLowerCase();
        else deviceName = str.toLowerCase();
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        try {
            String friendlyName = btDevice.getFriendlyName(false);
            String blthAddress = btDevice.getBluetoothAddress();
            if (deviceName != null) {
                if (deviceName.equals(friendlyName.toLowerCase())) {
                    System.out.printf("    – %s: (%s)\n", friendlyName, blthAddress);
                }
            } else if (bluetoothAddress != null) {
                if (bluetoothAddress.equals(blthAddress.toLowerCase())) {
                    System.out.printf("    – %s: (%s)\n", friendlyName, blthAddress);
                }
            } else {
                System.out.printf("    – %s: (%s)\n", friendlyName, blthAddress);
            }

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
                System.out.println("\t\t– Service without name.");
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
        System.out.println("=============\t\tProcess Done\t =============");
        synchronized (inquiryCompletedEvent) {
            inquiryCompletedEvent.notifyAll();
        }
    }
}
