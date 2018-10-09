import javax.bluetooth.*;
import java.io.IOException;

public class main
{
    static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args)
    {
        try
        {
            LocalDevice ld = LocalDevice.getLocalDevice();

            //Exercise 1
            System.out.println("Local Device: " + ld.getBluetoothAddress());
            System.out.println("Device Class: " + ld.getDeviceClass());
            System.out.println("Friendly Name: " + ld.getFriendlyName());
            System.out.println("Discoverable Mode: " + ld.getDiscoverable());
            System.out.println("BlueCove version: " + ld.getProperty("bluecove"));

            //Exercise 2
            DiscoveryAgent agent = ld.getDiscoveryAgent();
            DiscoveryListener listener = new DiscoveryListener()
            {
                @Override
                public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass)
                {
                    try
                    {
                        System.out.printf(" > %s (%s)\n", remoteDevice.getFriendlyName(false), remoteDevice.getBluetoothAddress());
                    }catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void servicesDiscovered(int i, ServiceRecord[] serviceRecords)
                {
                    for(ServiceRecord record: serviceRecords)
                    {
                        DataElement de = record.getAttributeValue(0x0100);

                        if(de != null)
                        {
                            System.out.println((String)de.getValue());
                        }else
                        {
                            System.out.println("-Service without name-");
                        }

                        System.out.println(record.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
                    }
                }

                @Override
                public void serviceSearchCompleted(int i, int i1)
                {
                    synchronized(INQUIRY_COMPLETED_EVENT)
                    {
                        INQUIRY_COMPLETED_EVENT.notifyAll();
                    }
                }

                @Override
                public void inquiryCompleted(int i)
                {
                    System.out.println("========== Completado ==========");

                    synchronized(INQUIRY_COMPLETED_EVENT)
                    {
                        INQUIRY_COMPLETED_EVENT.notifyAll();
                    }
                }
            };

            System.out.println("========== Buscando dispositivos ==========");
            agent.startInquiry(DiscoveryAgent.GIAC, listener);

            try
            {
                INQUIRY_COMPLETED_EVENT.wait();
            }catch(Exception e)
            {
                e.printStackTrace();
            }

        }catch(BluetoothStateException e)
        {
            e.printStackTrace();
        }
    }
}
