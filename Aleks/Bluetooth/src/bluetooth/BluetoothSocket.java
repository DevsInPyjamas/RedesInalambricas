package bluetooth;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class BluetoothSocket {
    private static final Object INQUIRY_COMPLETED_EVENT = new Object();
    public static void main(String[] args) throws IOException {
        boolean[] ptrBool = {false};
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String bluetoothURL = searchDevices(bufferedReader, agent);
        StreamConnection service = (StreamConnection) Connector.open(bluetoothURL);
        System.out.println("connected to server!");

        Scanner sc = new Scanner(System.in);
        BufferedReader br = new BufferedReader(new InputStreamReader(service.openInputStream()));
        BufferedOutputStream bo = new BufferedOutputStream(service.openOutputStream());

        Thread t = new Thread(() -> {
            System.out.print(" > ");
            while(!ptrBool[0] && sc.hasNext()) {
                try {
                    String linea = sc.nextLine();
                    bo.write(linea.getBytes());
                    bo.write('\n');
                    bo.flush();
                    System.out.print(" > ");
                    ptrBool[0] = linea.equals("exit");
                } catch (IOException e) {
                    ptrBool[0] = false;
                    e.printStackTrace();
                }
            }
        }, "Terminal reader");
        t.start();

        String linea = br.readLine();
        while(!ptrBool[0] && linea!=null){
            System.out.println(linea);
            linea = br.readLine();
            if("exit".equals(linea)) {
                ptrBool[0] = true;
                bo.write("exit\n".getBytes());
                bo.flush();
            }
        }

        br.close();
        bo.close();
        service.close();
        sc.close();
    }

    private static String searchDevices(BufferedReader br, DiscoveryAgent agent) {
        String serverBluetoothAddress = null;
        List<RemoteDevice> cachedDevices;
        try {
            DiscoveryListener discoveryListener;
            discoveryListener = new DeviceAndServiceDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("============= \tSearching near devices\t =============");
            agent.startInquiry(DiscoveryAgent.GIAC, discoveryListener);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                INQUIRY_COMPLETED_EVENT.wait();
            }
            ((DeviceAndServiceDiscoverer) discoveryListener).printAllDevices();
            System.out.print("Tell me the device to connect to: ");
            int devicePos = Integer.parseInt(br.readLine()) - 1;
            cachedDevices = ((DeviceAndServiceDiscoverer) discoveryListener).getCachedDevices();
            RemoteDevice device = cachedDevices.get(devicePos);
            agent.searchServices(new int[]{0x0100}, new UUID[] { new UUID(0x1002)}, device,
                    discoveryListener);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                INQUIRY_COMPLETED_EVENT.wait();
            }
            System.out.println();
            serverBluetoothAddress = ((DeviceAndServiceDiscoverer) discoveryListener).allUrls().get(0).get("chat");
            if (serverBluetoothAddress == null) {
                System.err.println("There is no server in that device...");
                System.exit(-1);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return serverBluetoothAddress;
    }


}
