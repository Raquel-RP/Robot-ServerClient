import ProtocoloRobot.MensajesProtocolo;
import SHA256_Base64.HashingEncoding;
import static SHA256_Base64.HashingEncoding.base64;
import static SHA256_Base64.HashingEncoding.sha256;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
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
            Socket socketConexion;
            socketConexion = new Socket(servidor, puerto);

            // Obtengamos los canales para recibir y enviar:
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketConexion.getInputStream()));
            // Creamos un bufferedReader para leer líneas del teclado:
            BufferedReader inTeclado = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter salida = new PrintWriter(socketConexion.getOutputStream());
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
                mensaje.interpretarMensajeSolicitud(orden);
                
                if (mensaje.getTipo() == MensajesProtocolo.mLogin) {
                    campos = orden.split(DEL);
                    String password = campos[3];
                    password = base64(sha256(password));
                    String mensajeLogin = campos[0] + DEL + campos[1] + DEL + campos[2] + DEL + password;
                    
                    mostrar("Enviando solicitud de login... ", true);
                    salida.println(mensajeLogin);
                    salida.flush();
            
                    mostrar("Comprobando mensaje recibido... ", false);
                    linea = entrada.readLine();
                    mensaje2.interpretarMensajeRespuesta(linea);
                    System.out.println(mensaje2.getTipo());
                }              
                
                else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                    mostrar("Enviando solicitud de apagado... ", true);
                    salida.println(orden);
                    salida.flush();
                    mostrar("Comprobando mensaje recibido... ", false);
                    linea = entrada.readLine();
                    mensaje2.interpretarMensajeRespuesta(linea);
                    System.out.println(mensaje2.getTipo());
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
                    salida.println(orden);
                    salida.flush();
                    mostrar("Enviando mensaje...", false);
                    mostrar(" ¡Enviado!", true);
                    linea = entrada.readLine();
                    mensaje2.interpretarMensajeRespuesta(linea);
                }
            }
            mostrar("Cerrando socket... ", false);
            socketConexion.close();
            mostrar("¡Cerrado! ", true);
            //////////////////////////////////////////////////////////////////////////////////////
            

        } catch (UnknownHostException ex) {
            System.err.println("Error al abrir el socket: no se puede resolver el nombre del servidor.");

        } catch (IllegalArgumentException ex) {
            System.err.println("Error al abrir el socket: parámetro incorrecto.");
        } catch (IOException ex) {
            System.err.println("Error de entrada salida...");
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
