package bluetooth;

import javax.bluetooth.*;
import java.io.IOException;

public class DeviceAndServiceDiscoverer implements DiscoveryListener {
    private final Object INQUIRY_COMPLETED_EVENT;
    private String deviceName;
    private String bluetoothAddress;
    private boolean found;

    public DeviceAndServiceDiscoverer() {
        INQUIRY_COMPLETED_EVENT = new Object();
    }

    public DeviceAndServiceDiscoverer(Object INQUIRY_COMPLETED_EVENT) {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.deviceName = this.bluetoothAddress = null;
    }

    public DeviceAndServiceDiscoverer(Object INQUIRY_COMPLETED_EVENT, String str, boolean isBluetoothAddress) {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        bluetoothAddress = (isBluetoothAddress) ? str.toLowerCase() : null;
        deviceName = (isBluetoothAddress) ? null : str.toLowerCase();
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        try {
            String friendlyName = btDevice.getFriendlyName(false).toLowerCase();
            String blthAddress = btDevice.getBluetoothAddress().toLowerCase();
            if (deviceName == null && bluetoothAddress == null) {
                System.out.printf("    – %s: (%s)\n", friendlyName, blthAddress);
                found = true;
            } else if (friendlyName.equals(deviceName) ||
                    blthAddress.equals(bluetoothAddress)) {
                System.out.printf("    – %s: (%s)\n", friendlyName, blthAddress);
                found = true;
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
                System.out.println("    > " + serviceName.getValue());
            } else {
                System.out.println("    > Service without name.");
            }
            System.out.println(s.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        if (respCode == DiscoveryListener.SERVICE_SEARCH_NO_RECORDS) {
            System.err.println("    > No services found in device...");
        }
        synchronized (INQUIRY_COMPLETED_EVENT) {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }

    @Override
    public void inquiryCompleted(int discType) {
        System.out.println("=============\t\tProcess Done\t =============");
        if (!found) {
            System.err.println("No devices found...");
        }
        synchronized (INQUIRY_COMPLETED_EVENT) {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }
}
