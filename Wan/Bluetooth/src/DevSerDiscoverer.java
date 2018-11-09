import javax.bluetooth.*;
import java.io.IOException;
import java.util.*;

public class DevSerDiscoverer implements DiscoveryListener
{
    private final Object INQUIRY_COMPLETED_EVENT;
    private String deviceName, bluetoothAddress;
    private boolean exist = false;
    private List<RemoteDevice> cachedDevices;
    private List<Map<String, String>> urlsDevices = new ArrayList<>();

    public DevSerDiscoverer()
    {
        INQUIRY_COMPLETED_EVENT = new Object();
    }

    public DevSerDiscoverer(Object INQUIRY_COMPLETED_EVENT)
    {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        this.deviceName = this.bluetoothAddress = null;
        cachedDevices = new ArrayList<>();
    }

    public DevSerDiscoverer(Object INQUIRY_COMPLETED_EVENT, String input, boolean isDeviceName)
    {
        this.INQUIRY_COMPLETED_EVENT = INQUIRY_COMPLETED_EVENT;
        bluetoothAddress = (isDeviceName) ? null : input.toLowerCase();
        deviceName = (isDeviceName) ? input.toLowerCase() : null;
        cachedDevices = new ArrayList<>();
    }

    public List<RemoteDevice> getCachedDevices()
    {
        return cachedDevices;
    }

    public List<Map<String, String>> getUrls()
    {
        return urlsDevices;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass)
    {
        try
        {
            String friendlyName = remoteDevice.getFriendlyName(false).toLowerCase();
            String blueAddress = remoteDevice.getBluetoothAddress().toLowerCase();

            if(deviceName == null && bluetoothAddress == null)
            {
                cachedDevices.add(remoteDevice);
                exist = true;
            }else if(friendlyName.equals(deviceName) || blueAddress.equals(bluetoothAddress))
            {
                cachedDevices.add(remoteDevice);
                exist = true;
            }

        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] rec)
    {
        Map<String, String> urls = new HashMap<>();
        for(ServiceRecord record: rec)
        {
            DataElement service = record.getAttributeValue(0x0100);

            if(service != null)
            {
                System.out.println(" > " + service.getValue());
                urls.put((String) service.getValue(), record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
            }else
            {
                System.out.println(" > UNNAMED SERVICE");
            }

            System.out.println("   > Service URL: " + record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
        }
        urlsDevices.add(urls);
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode)
    {
        if(respCode == DiscoveryListener.SERVICE_SEARCH_NO_RECORDS)
        {
            System.out.println("> NO SERVICES FOUND IN DEVICE...\n");
        }

        synchronized(INQUIRY_COMPLETED_EVENT)
        {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }

    @Override
    public void inquiryCompleted(int i)
    {
        System.out.println("\nPROCESS DONE\n");

        if(!exist)
        {
            System.out.println("> NO DEVICE FOUND...");
        }

        synchronized(INQUIRY_COMPLETED_EVENT)
        {
            INQUIRY_COMPLETED_EVENT.notifyAll();
        }
    }

    public void printDevices()
    {
        System.out.println("\n============ DEVICES FOUND ===========\n");

        int numDevices = 1;
        for(RemoteDevice dev: cachedDevices)
        {
            try
            {
                System.out.println(numDevices + ". " + dev.getFriendlyName(false) + " (" + dev.getBluetoothAddress() + ")");
            }catch(IOException e)
            {
                System.out.println(numDevices + ". UNNAMED DEVICE");
            }
            numDevices++;
        }
        System.out.println("\n======================================\n");
    }
}