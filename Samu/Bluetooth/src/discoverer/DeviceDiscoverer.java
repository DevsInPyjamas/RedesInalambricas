package discoverer;

import javax.bluetooth.*;
import java.io.IOException;

public class DeviceDiscoverer implements DiscoveryListener {
    private Object INQUIRY_COMPLETED_EVENT;
    private String btAddress, deviceFriendlyName;


    public DeviceDiscoverer(Object INQUIRY_COMPLETED_EVENT, String inputString, boolean isBluetoothAddress){
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.btAddress = isBluetoothAddress ? inputString.toLowerCase() : null;
        this.deviceFriendlyName = isBluetoothAddress ? null : inputString.toLowerCase();
    }

    public DeviceDiscoverer(Object INQUIRY_COMPLETED_EVENT){
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.btAddress = this.deviceFriendlyName = null;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        try {
            String friendlyName = remoteDevice.getFriendlyName(false).toLowerCase();
            String address = remoteDevice.getBluetoothAddress().toLowerCase();
            if(deviceFriendlyName == null && btAddress == null) {
                System.out.printf("- %s (%s)\n", friendlyName, address);
            } else if(friendlyName.equals(deviceFriendlyName) || address.equals(btAddress)){
                System.out.printf("- %s (%s)\n", friendlyName, address);
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
}
