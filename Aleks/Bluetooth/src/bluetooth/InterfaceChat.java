package bluetooth;

import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import chatUI.ChatWindow;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class InterfaceChat {
    public static void main(String args[]) throws IOException {
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

        StreamConnection service = (StreamConnection) Connector.open(BluetoothSocket.searchDevices(bufferedReader, agent));
        System.out.println("connected to server!");
        Scanner sc = new Scanner(System.in);
        BufferedReader br = new BufferedReader(new InputStreamReader(service.openInputStream()));
        BufferedOutputStream bo = new BufferedOutputStream(service.openOutputStream());
        /* En este punto deberíamos buscar el servicio (en caso de que estemos en el cliente) y conectarnos a la URL
        o activar la escucha de peticiones (en el caso del servidor) */

        /* A continuación, obtenemos el inputStream y outputStream y los usamos desde la ventana */

        /* Invocamos la ventana (que se ejecuta como un thread en segundo plano) y
         * definimos la acción de enviar lo que insertemos por teclado */
        java.awt.EventQueue.invokeLater(() -> {
            final ChatWindow _window;
            _window = new ChatWindow();
            _window.setVisible(true);
            _window.addActionListener(evt -> {
                /* MODIFICAR EL CÓDIGO PARA EL ENVÍO AQUÍ */
                String s = _window.getIn(); // metodo que lee de la entrada
                _window.setOut(s); //Método que escribe en la salida de la ventana
            });
            /*
             * Líneas obligatorias: hay que registrar un listener para los eventos de ventana y
             * sobre el de window closing, realizar el cierre de conexiones.
             */
            _window.addWindowListener(new java.awt.event.WindowListener() {
                public void windowClosing(WindowEvent e) {
                    System.out.println("Window closing event .... close connections");
                }

                public void windowClosed(WindowEvent e) {
                    System.out.println("Window closed event ");
                }

                public void windowDeactivated(WindowEvent e) {
                    System.out.println("Window deactivated event ");
                }

                public void windowOpened(WindowEvent e) {
                    System.out.println("Window event ");
                }

                public void windowIconified(WindowEvent e) {
                    System.out.println("Window event ");
                }

                public void windowDeiconified(WindowEvent e) {
                    System.out.println("Window event ");
                }

                public void windowActivated(WindowEvent e) {
                    System.out.println("Window event ");
                }

            });

        });
        while (true) {
            /* En este punto, una vez iniciada la ventana, nos ponemos en bucle a recibir la información del otro extremo */
            /* MODIFICAR EL CÓDIGO PARA LA RECEPCIÓN AQUÍ */
        }
    }

}
