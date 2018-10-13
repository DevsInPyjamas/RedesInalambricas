package bluetooth;

import javax.bluetooth.*;
import java.io.IOException;

public class DeviceAndServiceDiscoverer implements DiscoveryListener {
    private final Object INQUIRY_COMPLETED_EVENT;
    private String deviceName;
    private String bluetoothAddress;
    private boolean found;

    public DeviceAndServiceDiscoverer(Object INQUIRY_COMPLETED_EVENT) {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.deviceName = this.bluetoothAddress = null;
    }

    public DeviceAndServiceDiscoverer(Object INQUIRY_COMPLETED_EVENT, String str, boolean isBluetoothAddress) {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        bluetoothAddress = (isBluetoothAddress) ? str : null;
        deviceName = (isBluetoothAddress) ? null : str;
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        try {
            String friendlyName = btDevice.getFriendlyName(false);
            String blthAddress = btDevice.getBluetoothAddress();
            if (deviceName == null && bluetoothAddress == null) {
                System.out.printf("    – %s: (%s)\n", friendlyName, blthAddress);
                found = true;
            } else if (friendlyName.equalsIgnoreCase(deviceName) ||
                    blthAddress.equalsIgnoreCase(bluetoothAddress)) {
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
            System.out.println("    > Service URL: " + s.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        if (respCode == DiscoveryListener.SERVICE_SEARCH_NO_RECORDS) {
            System.out.println("    > No services found in device...");
        }
        synchronized (INQUIRY_COMPLETED_EVENT) {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }

    @Override
    public void inquiryCompleted(int discType) {
        System.out.println("=============\t\tProcess Done\t =============");
        if (!found) {
            System.out.println("No devices found...");
        }
        synchronized (INQUIRY_COMPLETED_EVENT) {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }
}
