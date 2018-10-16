package bluetooth;

import javax.bluetooth.*;
import java.io.IOException;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;

public class DeviceAndServiceDiscoverer implements DiscoveryListener {
    private final Object INQUIRY_COMPLETED_EVENT;
    private String deviceName;
    private String bluetoothAddress;
    private boolean found;
    private int devicesDiscovered;
    private List<RemoteDevice> cachedDevices;

    public DeviceAndServiceDiscoverer(Object INQUIRY_COMPLETED_EVENT) {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.deviceName = this.bluetoothAddress = null;
        devicesDiscovered = 0;
        cachedDevices = new ArrayList<>();
    }

    public DeviceAndServiceDiscoverer(Object INQUIRY_COMPLETED_EVENT, String str, boolean isBluetoothAddress) {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        bluetoothAddress = (isBluetoothAddress) ? str : null;
        deviceName = (isBluetoothAddress) ? null : str;
        devicesDiscovered = 0;
        cachedDevices = new ArrayList<>();
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        String friendlyName;
        try {
            friendlyName = btDevice.getFriendlyName(false);
        } catch (IOException e) {
            friendlyName = "Unnamed device";
        }
        String blthAddress = btDevice.getBluetoothAddress();
        if (deviceName == null && bluetoothAddress == null) {
            devicesDiscovered++;
            cachedDevices.add(btDevice);
            found = true;
        } else if (friendlyName.equalsIgnoreCase(deviceName) ||
                blthAddress.equalsIgnoreCase(bluetoothAddress)) {
            devicesDiscovered++;
            cachedDevices.add(btDevice);
            found = true;
        }
    }

    public void printAllDevices() {
        int devices = 1;
        for(RemoteDevice device : cachedDevices) {
            try {
                System.out.printf("    %d. %s: (%s)\n",devices, device.getFriendlyName(false), device.getBluetoothAddress());
            } catch (IOException e) {
                System.out.printf("    %d. Unnamed device\n", devices);
            }
            devices++;
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
        System.out.println("Found " + devicesDiscovered + " devices:");
        if (!found) {
            System.out.println("No devices found...");
        }
        synchronized (INQUIRY_COMPLETED_EVENT) {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }

    public List<RemoteDevice> getCachedDevices() {
        return cachedDevices;
    }
}
