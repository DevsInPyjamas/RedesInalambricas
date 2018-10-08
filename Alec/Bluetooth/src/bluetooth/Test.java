package bluetooth;
import javax.bluetooth.*;
import java.io.IOException;


public class Test {
    public Test() {

    }

    public static class Main {

        public static void main(String... args) throws Throwable {
            LocalDevice local = LocalDevice.getLocalDevice();
            System.out.println(local.getBluetoothAddress());
            System.out.println(local.getFriendlyName());
            System.out.println(local.getDiscoverable());

            DiscoveryAgent agent = local.getDiscoveryAgent();
            DiscoveryListener listener = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    try {
                        System.out.printf("  > %s (%s)\n", btDevice.getFriendlyName(false), btDevice.getBluetoothAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void inquiryCompleted(int discType) {
                    System.out.println(" === Hecho === ");
                    synchronized(this) {
                        this.notifyAll();
                    }
                }

                @Override
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                    for(ServiceRecord serviceRecord : servRecord) {
                        DataElement serviceName = serviceRecord.getAttributeValue(0x0100);
                        if(serviceName == null) {
                            System.out.println("    - Servicio sin nombre");
                        } else {
                            System.out.println("    - " + serviceName.getValue());
                        }

                        System.out.println("    - " + serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
                    }
                }

                @Override
                public void serviceSearchCompleted(int transID, int respCode) {
                    System.out.println("    " + respCode);
                    synchronized(this) {
                        this.notifyAll();
                    }
                }
            };

            System.out.println(" === Buscando dispositivos === ");
            agent.startInquiry(DiscoveryAgent.GIAC, listener);

            synchronized(listener) {
                listener.wait();
            }

            for(RemoteDevice device : agent.retrieveDevices(DiscoveryAgent.CACHED)) {
                agent.searchServices(new int[] { 0x0100 }, new UUID[] { new UUID(0x1002) }, device, listener);
                System.out.println("  > " + device.getFriendlyName(false));
                synchronized(listener) {
                    listener.wait();
                }
                System.out.println();
            }
        }
    }


}


