
import ProtocoloRobot.MensajesProtocolo;
import static SHA256_Base64.HashingEncoding.base64;
import static SHA256_Base64.HashingEncoding.sha256;
import java.io.IOException;
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
public class ServidorRobotOO {

    // Puerto donde escuchará el servidor:
    int puerto = 9999;
    boolean evCIFOk = false;
    String infoChoque = "";

    private static int errorMensajeIncorrecto = 1;
    private static int errorMensajeMalFormado = 2;

    static final char DEL = 0x20;

    /////////////////////////////////
    // Estados
    static final int estadoInicioFin = 0;
    static final int estadoLogin = 1;
    static final int estadoFuncionando = 2;
 
    int estado = estadoInicioFin;
    /////////////////////////////////

    public ServidorRobotOO(int puerto) {
        this.puerto = puerto;
    }

    /**
     * Se le puede pasar el puerto para el servidor como primer argumento.
     *
     * @param args
     */
    public static void main(String[] args) {
        int puerto = 9999;

        if (args.length >= 1) {
            puerto = Integer.parseInt(args[0]);
        }

        // Creamos un servidor, y lo ejecutamos.
        ServidorRobotOO servidor = new ServidorRobotOO(puerto);
        servidor.ejecutar();
    }

    @SuppressWarnings("empty-statement")
    public void ejecutar() {
        // Si ocurre algún error, lo anotamos:
        int error = 0;

        String linea = "";

        // El socket es de tipo UDP:
        DatagramSocket buzon;

        try {
            ////// Inicializamos el socket de escucha, para esperar cientes://////////////////////
            // Le indicamos al servidor UDP que escuche en el puerto "9999":
            mostrar("Abriendo puerto...", false);
            
            buzon = new DatagramSocket(puerto);
            byte []bufferDatagramaRecepcion = new byte[1024];
            
            mostrar("¡Abierto!", true);
            
///////////////////////////////////////////////////////////////////////////////

            // Para que sea un servidor iterativo, repetimos lo siguiente
            // por cada nueva conexión:
            boolean salir = false;
            boolean salirServicio = false;
            mostrar("Entrando en bucle de servicio a clientes...", true);
            
            while (!salirServicio) {

                ////////// Aceptamos una nueva conexión: /////////////////////////////////////
                // Esperamos a que se conecte algún cliente. El servidor se quedará
                // bloqueado hasta que llegue la primera conexión.
                
                mostrar(" ", true);
                mostrar("Esperando conexiones entrantes... ", false);
                
                // Comenzamos en el estado inicial:
                estado = estadoInicioFin;
                
                mostrar("¡Conexión aceptada!", true);
                /// Se recibe un mensaje:
                mostrar("Esperando mensaje del cliente...", false);
                
                salir = false;
                do {
                    // Reservamos memoria para el datagrama recibido:  
                    DatagramPacket datagrama=new DatagramPacket(bufferDatagramaRecepcion, 1024);
                    
                    // Leemos del socket ("buzón"), a ver si hay algún datagrama:
                    try {
                        buzon.receive(datagrama);
                    } catch (IOException ex){}
                    
                    // Almacenamos la información del remitente:
                    InetAddress direccionRemitente = datagrama.getAddress();
                    int puertoRemitente = datagrama.getPort();
                    
                    // Leemos el mensaje: número de bytes recibidos, y el contenido:
                    int nBytesRecibidos=datagrama.getLength();
                    
                    linea = new String(datagrama.getData(),0,nBytesRecibidos);
                    mostrar("¡Recibido!", true);
                    /////////////////////////////                    
                    
                    // interpretamos el mensaje recibido:
                    MensajesProtocolo mensaje = new MensajesProtocolo();
                    mensaje.interpretarMensajeSolicitud(linea);
                                        
                    // Si es un mensaje de protocolo válido:
                    if (mensaje.getTipo() != MensajesProtocolo.mNoValido) {
                        String cadena;
                        
                        // Según el estado en el que estamos, se espera un mensaje u otro:
                        switch (estado) {
                            
                            case estadoInicioFin:

                                if (mensaje.getTipo() == MensajesProtocolo.mEncender) {
                                    mostrar("Enviando confirmación de encendido...", false);
                                    // Enviamos la respuesta:
                                    cadena = enviarCodigoOK();
                                    // Enviamos la respuesta: Creamos el datagrama a partir de un chorro de bytes,
                                    // y creamos un datagrama con la dirección de destino y puerto del remitente:
                                    byte []mensajeRespuestaBytes=cadena.getBytes();
                                    datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                    datagrama.setAddress(direccionRemitente);
                                    datagrama.setPort(puertoRemitente);
                                    buzon.send(datagrama);
                                    
                                    mostrar("¡Enviado!", true);
                                    // Realizamos la transición:
                                    estado = estadoLogin;
                                    mostrar("Entrando en la fase de login...", true);
                                } 
                                else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                                    mostrar("Enviando confirmación de apagado...", false);
                                    cadena = enviarMApagarOK();
                                    byte []mensajeRespuestaBytes=cadena.getBytes();
                                    datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                    datagrama.setAddress(direccionRemitente);
                                    datagrama.setPort(puertoRemitente);
                                    buzon.send(datagrama);
                                    mostrar("¡Mensaje recibido incorrecto!", true);
                                    mostrar("¡Enviado!", true);
                                    salir = true;
                                    salirServicio = true;
                                }
                                else {
                                    cadena = enviarCodigoNOK();
                                    byte []mensajeRespuestaBytes=cadena.getBytes();
                                    datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                    datagrama.setAddress(direccionRemitente);
                                    datagrama.setPort(puertoRemitente);
                                    buzon.send(datagrama);
                                    mostrar("¡Mensaje recibido incorrecto!", true);
                                }
                                

                                break;

                            case estadoLogin:
                                // Si es el mensaje de solicitud de login:
                                if (mensaje.getTipo() == MensajesProtocolo.mLogin) {
                                    String credenciales = mensaje.getParametro();
                                    String password = "password1234";
                                    password = base64(sha256(password));
                                    String credencialesBD = "robot12 " + password;

                                    mostrar("Recibida petición de login: " + credenciales, true);
                                    mostrar("Verificando credenciales...", true);
                                    
                                    // Comprobamos credenciales:
                                    if(credenciales.equals(credencialesBD)){
                                        mostrar("Enviando respuesta...", false);
                                        cadena = enviarCodigoOK();
                                        byte []mensajeRespuestaBytes=cadena.getBytes();
                                        datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                        datagrama.setAddress(direccionRemitente);
                                        datagrama.setPort(puertoRemitente);
                                        buzon.send(datagrama);
                                        mostrar("¡Enviada!", true);
                                        // Realizamos la transición:
                                        estado = estadoFuncionando;
                                        mostrar("Entrando en estado de funcionamiento...", true);
                                    }else {
                                        mostrar("Enviando mensaje de error...", false);
                                        cadena = enviarCodigoNOK();
                                        byte []mensajeRespuestaBytes=cadena.getBytes();
                                        datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                        datagrama.setAddress(direccionRemitente);
                                        datagrama.setPort(puertoRemitente);
                                        buzon.send(datagrama);
                                        mostrar(" Error enviado", true);
                                    }                                                                    
                        
                                    // Si es el mensaje de solicitud de finalizar sesión:
                                }else  if (mensaje.getTipo() == MensajesProtocolo.mCerrarSesion) {
                                    mostrar("Recibida petición de cerrar sesión", true);

                                    mostrar("Enviando respuesta...", false);
                                    cadena = enviarMApagarOK();
                                    byte []mensajeRespuestaBytes=cadena.getBytes();
                                    datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                    datagrama.setAddress(direccionRemitente);
                                    datagrama.setPort(puertoRemitente);
                                    buzon.send(datagrama);
                                    mostrar("¡Enviada!", true);
                                    estado = estadoInicioFin;
                                } else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                                    mostrar("Enviando confirmación de apagado...", false);
                                    cadena = enviarMApagarOK();
                                    byte []mensajeRespuestaBytes=cadena.getBytes();
                                    datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                    datagrama.setAddress(direccionRemitente);
                                    datagrama.setPort(puertoRemitente);
                                    buzon.send(datagrama);
                                    mostrar("¡Enviado!", true);
                                    salir = true;
                                    salirServicio = true;
                                } else {
                                    // Si hay algún error, reintenta login.
                                    cadena = enviarCodigoNOK();
                                    byte []mensajeRespuestaBytes=cadena.getBytes();
                                    datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                    datagrama.setAddress(direccionRemitente);
                                    datagrama.setPort(puertoRemitente);
                                    buzon.send(datagrama);
                                    mostrar("¡Mensaje erróneo!", true);
                                }

                                break;

                            case estadoFuncionando:
                            if (mensaje.getTipo() == MensajesProtocolo.mReqLidar) {
                                mostrar("Enviando confirmación de uso de lidar...", false);
                                // Enviamos la respuesta:
                                cadena = enviarMLidarOK("43 92"); //mensaje.getParametro() si se implementase con un Lidar real
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
  
                            } else if (mensaje.getTipo() == MensajesProtocolo.mReqEstado) {
                                mostrar("Enviando confirmación de uso de sensores de choque...", true);
                                // Enviamos la respuesta:
                                cadena = enviarMResEstadoOK("NO"); //mensaje.getParametro() si se implementase con un sensor real
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mAvanzar) {
                                mostrar("Enviando confirmación de desplazamiento...", true);
                                // Enviamos la respuesta:
                                cadena = enviarCodigoOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mParar) {
                                mostrar("Enviando confirmación de parada...", false);
                                // Enviamos la respuesta:
                                cadena = enviarCodigoOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mRotar) {
                                mostrar("Enviando confirmación de rotacion...", false);
                                // Enviamos la respuesta:
                                cadena = enviarCodigoOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mReqPosicion) {
                                mostrar("Enviando confirmación de uso de sensores de choque...", false);
                                // Enviamos la respuesta:
                                cadena = enviarMResPosicionOK("102 321 56"); //mensaje.getParametro() si se implementase real
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mActivarModoLotes) {
                                mostrar("Enviando confirmación de modo lotes...", false);
                                // Enviamos la respuesta:
                                cadena = enviarCodigoOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mCerrarSesion) {
                                mostrar("Enviando confirmación de cierre de sesión...", false);
                                // Enviamos la respuesta:
                                cadena = enviarCodigoOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                                estado = estadoLogin;
                            } else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                                mostrar("Enviando confirmación de apagado...", false);
                                cadena = enviarMApagarOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Enviado!", true);
                                salir = true;
                                salirServicio = true;
                            }            
                            else {
                                // Si hay algún error sigue escuchando
                                cadena = enviarCodigoNOK();
                                byte []mensajeRespuestaBytes=cadena.getBytes();
                                datagrama=new DatagramPacket(mensajeRespuestaBytes,mensajeRespuestaBytes.length);
                                datagrama.setAddress(direccionRemitente);
                                datagrama.setPort(puertoRemitente);
                                buzon.send(datagrama);
                                mostrar("¡Mensaje recibido incorrecto!", true);
                            }
                        }

                    }
                } while (!salir);

                mostrar("Cerrando conexión... ", false);
                buzon.close();
                mostrar("¡Cerrado! ", true);
            }

///////////////////////////////////////////////////////////////
        } catch (IOException ex) {
            Logger.getLogger(ServidorRobotOO.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error de entrada/buzon.");
            System.err.println("Error de entrada/buzon.");
        }

    }

    /**
     * Método auxiliar para mostrar mensajes por pantalla:
     *
     * @param mensaje
     */
    private static void mostrar(String mensaje, boolean simple) {
        System.out.print(((simple) ? "" : "Servidor: ") + mensaje + ((simple) ? "\n" : ""));
    }

    private String enviarCodigoOK() {
        
        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearCodigoOK();

        String cadena = mensaje.serializar();
        
        return cadena;
    }

    private String enviarCodigoNOK() {

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearCodigoNOK();

        String cadena = mensaje.serializar();

        return cadena;
    }

    private String enviarMApagarOK() {

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMApagarOK();

        String cadena = mensaje.serializar();

        return cadena;
    }

    private String enviarMResEstadoOK(String infoChoque) {

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMResEstadoOK(infoChoque);

        String cadena = mensaje.serializar();

        return cadena;
    }

    private String enviarMLidarOK(String posicion) {
        
        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMLidarOK(posicion);

        String cadena = mensaje.serializar();

        return cadena;
    }

    private String enviarMResPosicionOK(String posicionActual) {
        
        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMResPosicion(posicionActual);

        String cadena = mensaje.serializar();

        return cadena;
    }
}
