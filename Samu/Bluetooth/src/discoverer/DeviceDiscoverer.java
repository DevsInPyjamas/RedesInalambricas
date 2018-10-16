package discoverer;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDiscoverer implements DiscoveryListener {
    private Object INQUIRY_COMPLETED_EVENT;
    private String btAddress, deviceFriendlyName;
    private List<RemoteDevice> cachedDevices;

    public List<RemoteDevice> getCachedDevices() {
        return cachedDevices;
    }

    public DeviceDiscoverer(Object INQUIRY_COMPLETED_EVENT, String inputString, boolean isBluetoothAddress){
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.btAddress = isBluetoothAddress ? inputString.toLowerCase() : null;
        this.deviceFriendlyName = isBluetoothAddress ? null : inputString.toLowerCase();
        cachedDevices = new ArrayList<>();
    }

    public DeviceDiscoverer(Object INQUIRY_COMPLETED_EVENT){
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.btAddress = this.deviceFriendlyName = null;
        cachedDevices = new ArrayList<>();
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        try {
            String friendlyName = remoteDevice.getFriendlyName(false).toLowerCase();
            String address = remoteDevice.getBluetoothAddress().toLowerCase();
            if(deviceFriendlyName == null && btAddress == null) {
                cachedDevices.add(remoteDevice);
            } else if(friendlyName.equals(deviceFriendlyName) || address.equals(btAddress)){
                cachedDevices.add(remoteDevice);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void servicesDiscovered(int i, ServiceRecord[] serviceRecords) {
        for (ServiceRecord record : serviceRecords) {
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

    public void printDevices(){
        for (RemoteDevice rd: cachedDevices) {
            try {
                System.out.printf("    %s (%s)\n", rd.getFriendlyName(false), rd.getBluetoothAddress());
            } catch (IOException e) {
                System.out.println("    Unnamed device");
            }
        }
    }
}
