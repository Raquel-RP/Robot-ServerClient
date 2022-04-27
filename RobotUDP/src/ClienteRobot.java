import ProtocoloRobot.MensajesProtocolo;
import static SHA256_Base64.HashingEncoding.base64;
import static SHA256_Base64.HashingEncoding.sha256;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Raquel Romero Pedraza
 * 
 */

public class ClienteRobot {

    static final char DEL = 0x20;
    private static int errorMensajeIncorrecto;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String servidor = "127.0.0.1";
        int puerto = 9999;

        String linea = "";
        String[] campos;
        String comando = "";
        String textoPlano = "";
        String orden = "";
        String DEL = " ";
        
        int error = 0;
           
        try {

            /////// Iniciamos la conexión con el servidor: //////////////////////////////////////////
            // Nos conectamos al servidor:
            mostrar("Intentando conexión a " + servidor + ":" + puerto + "...", false);
            DatagramSocket buzon;
            buzon = new DatagramSocket();

            // Creamos un bufferedReader para leer líneas del teclado:
            BufferedReader inTeclado = new BufferedReader(new InputStreamReader(System.in));
    
            mostrar(" ¡Conectado!", true);
            

            ////////////////////////////////////////////////////////////////////////////////////////

            boolean salir = false;
            boolean salirServicio = false;
            
            mostrar("Entrando en bucle de envio de mensajes...", true);
            while (!salirServicio) {
                MensajesProtocolo mensaje = new MensajesProtocolo();
                MensajesProtocolo mensaje2 = new MensajesProtocolo();
                
                mostrar("Introduzca la orden: ", false);
                orden = inTeclado.readLine();
                DatagramPacket datagrama;
                DatagramPacket sobreRecepcion;
                mensaje.interpretarMensajeSolicitud(orden);
                
                if (mensaje.getTipo() == MensajesProtocolo.mLogin) {
                    campos = orden.split(DEL);
                    String password = campos[3];
                    password = base64(sha256(password));
                    String mensajeLogin = campos[0] + DEL + campos[1] + DEL + campos[2] + DEL + password;
                    
                    mostrar("Enviando solicitud de login... ", true);
                    // Envía la pregunta:
                    byte []mensajeBytes=mensajeLogin.getBytes();
                    datagrama=new DatagramPacket(mensajeBytes, mensajeBytes.length);
                    datagrama.setAddress(InetAddress.getByName(servidor));
                    datagrama.setPort(puerto);

                    buzon.send(datagrama);
                    mostrar("Enviado!", false);
                    
                    // Recibe la respuesta:
                    byte []mensajeRespuestaBytes=new byte[1024];
                    sobreRecepcion=new DatagramPacket(mensajeRespuestaBytes, mensajeRespuestaBytes.length);
                    buzon.receive(sobreRecepcion);
            
                }              
                
                else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                    mostrar("Enviando solicitud de apagado... ", true);
                    byte []mensajeBytes=orden.getBytes();
                    datagrama=new DatagramPacket(mensajeBytes, mensajeBytes.length);
                    datagrama.setAddress(InetAddress.getByName(servidor));
                    datagrama.setPort(puerto);

                    mostrar("Enviando mensaje...", false);
                    
                    buzon.send(datagrama);
                    
                    mostrar(" ¡Enviado!", true);
                    
                    byte []mensajeRespuestaBytes=new byte[1024];
                    sobreRecepcion=new DatagramPacket(mensajeRespuestaBytes, mensajeRespuestaBytes.length);
                    buzon.receive(sobreRecepcion);
            
                    mostrar("Comprobando mensaje recibido... ", false);
                                        
                    linea = new String(sobreRecepcion.getData(),0,sobreRecepcion.getLength());
                    
                    mensaje2.interpretarMensajeRespuesta(linea);
                    
                    if (mensaje2.getTipo() == MensajesProtocolo.mApagarOK) {
                        mostrar("¡Confirmación correcta!", true);
                        salirServicio = true;
                    }
                    else {
                        error = errorMensajeIncorrecto;
                        mostrar("¡Mensaje recibido erróneo!", true);
                    } 
                }
                else {
                    byte []mensajeBytes=orden.getBytes();
                    datagrama=new DatagramPacket(mensajeBytes, mensajeBytes.length);
                    datagrama.setAddress(InetAddress.getByName(servidor));
                    datagrama.setPort(puerto);

                    mostrar("Enviando mensaje...", false);
                    
                    buzon.send(datagrama);
                    
                    mostrar(" ¡Enviado!", true);
                    
                    byte []mensajeRespuestaBytes=new byte[1024];
                    sobreRecepcion=new DatagramPacket(mensajeRespuestaBytes, mensajeRespuestaBytes.length);
                    buzon.receive(sobreRecepcion);
                                                    
                    linea = new String(sobreRecepcion.getData(),0,sobreRecepcion.getLength());
                    
                }
            }
            mostrar("Cerrando socket... ", false);
            buzon.close();
            mostrar("¡Cerrado! ", true);
            
        } catch (IOException ex) {
            System.err.println("Error de entrada salida...");
            Logger.getLogger(ClienteRobot.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Método auxiliar para mostrar mensajes por pantalla:
     *
     * @param mensaje
     */
    private static void mostrar(String mensaje, boolean simple) {
        System.out.print(((simple) ? "" : "Cliente: ") + mensaje + ((simple) ? "\n" : ""));
    }
}
