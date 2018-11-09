import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.List;
import java.util.Scanner;

public class BluetoothSocket
{
    private static final Object INQUIRY_COMPLETED_EVENT = new Object();

    public static void main(String[] args) throws IOException
    {
        LocalDevice ld = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = ld.getDiscoveryAgent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String url = deviceSearcher(bufferedReader, agent);
        Boolean[] ptrBool = {false};
        StreamConnection serv = (StreamConnection) Connector.open(url);
        System.out.println("Connected to the server.");

        Scanner sc = new Scanner(System.in);
        BufferedReader br = new BufferedReader(new InputStreamReader(serv.openDataInputStream()));
        BufferedOutputStream bo = new BufferedOutputStream(serv.openDataOutputStream());

        Thread t = new Thread(() -> {
            System.out.print(" > ");
            while(!ptrBool[0] && sc.hasNext()) {
                try {
                    String linea = sc.nextLine();
                    bo.write(linea.getBytes());
                    bo.write('\n');
                    bo.flush();
                    System.out.print(" > ");
                    ptrBool[0] = linea.equals("END");
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
            if("END".equals(linea)) {
                ptrBool[0] = true;
                bo.write("END\n".getBytes());
                bo.flush();
            }
        }
    }

    public static String deviceSearcher(BufferedReader br, DiscoveryAgent agent)
    {
        String serverAddress = null;
        List<RemoteDevice> cachedDevices;

        try
        {
            DiscoveryListener dl = new DevSerDiscoverer(INQUIRY_COMPLETED_EVENT);
            System.out.println("\nSearching all nearby devices...\n");
            agent.startInquiry(DiscoveryAgent.GIAC, dl);

            synchronized (INQUIRY_COMPLETED_EVENT)
            {
                try
                {
                    INQUIRY_COMPLETED_EVENT.wait();
                } catch (Exception e) { }
            }

            ((DevSerDiscoverer) dl).printDevices();

            System.out.println("To which device do you want to connect? (number of device):\n");
            int deviceNum = Integer.parseInt(br.readLine()) - 1;
            cachedDevices = ((DevSerDiscoverer) dl).getCachedDevices();
            RemoteDevice dev = cachedDevices.get(deviceNum);
            agent.searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1002)}, dev, dl);

            synchronized (INQUIRY_COMPLETED_EVENT)
            {
                INQUIRY_COMPLETED_EVENT.wait();
            }

            serverAddress = ((DevSerDiscoverer) dl).getUrls().get(0).get("chat");

            if(serverAddress == null)
            {
                System.err.println("There is no server in that device...");
                System.exit(-1);
            }
        }catch(InterruptedException | IOException e)
        {
            e.printStackTrace();
        }
        return serverAddress;
    }
}