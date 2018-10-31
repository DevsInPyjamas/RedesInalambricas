import discoverer.DeviceDiscoverer;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args) {
        lookForServer();
    }

    public static void lookForServer() {
        LocalDevice device;

        try {
            boolean[] ptrBool = {false};
            device = LocalDevice.getLocalDevice();
            DiscoveryAgent da = device.getDiscoveryAgent();
            DeviceDiscoverer deviceDiscoverer = new DeviceDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("=====LOOKING FOR DEVICES=====");
            da.startInquiry(DiscoveryAgent.GIAC, deviceDiscoverer);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                try {
                    INQUIRY_COMPLETED_EVENT.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            UUID[] uuids = new UUID[]{new UUID(0x1002)};
            deviceDiscoverer.printDevices();
            System.out.println("Which device number would you like to connect?: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int input = Integer.parseInt(br.readLine());
            List<RemoteDevice> cachedDevices = deviceDiscoverer.getCachedDevices();
            while (input <= 0 || input > cachedDevices.size()) {
                System.out.println(String.format("Please, introduce a valid number between %d and %d", 1,
                        cachedDevices.size()));
            }
            input -= 1;
            da.searchServices(new int[]{0x0100}, uuids, cachedDevices.get(input), deviceDiscoverer);
            synchronized (INQUIRY_COMPLETED_EVENT) {
                INQUIRY_COMPLETED_EVENT.wait();
            }
            String serverURL = deviceDiscoverer.getDevicesURLs().get(0).get("chat");
            if (serverURL == null) {
                System.err.println("=====Chat server not found=====");
                System.exit(-1);
            }
            StreamConnection streamConnection = (StreamConnection) Connector.open(serverURL);

            Scanner sc = new Scanner(System.in);
            br = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));
            BufferedOutputStream bo = new BufferedOutputStream(streamConnection.openOutputStream());

            Thread t = new Thread(() -> {
                System.out.print(" > ");
                while (!ptrBool[0] && sc.hasNext()) {
                    try {
                        String linea = sc.nextLine();
                        bo.write(linea.getBytes());
                        bo.write('\n');
                        bo.flush();
                        System.out.print(" > ");
                        ptrBool[0] = linea.equals("FIN");
                    } catch (IOException e) {
                        ptrBool[0] = false;
                        e.printStackTrace();
                    }
                }
            }, "Terminal reader");
            t.start();

            String linea = br.readLine();
            while (!ptrBool[0] && linea != null) {
                System.out.println(linea);
                linea = br.readLine();
                if ("exit".equals(linea)) {
                    ptrBool[0] = true;
                    bo.write("exit\n".getBytes());
                    bo.flush();
                }
            }

            br.close();
            bo.close();
            streamConnection.close();
            sc.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

        }
    }
}
