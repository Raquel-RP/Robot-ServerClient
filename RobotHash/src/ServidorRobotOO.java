
import ProtocoloRobot.MensajesProtocolo;
import static SHA256_Base64.HashingEncoding.base64;
import static SHA256_Base64.HashingEncoding.sha256;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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

    public void ejecutar() {
        // Si ocurre algún error, lo anotamos:
        int error = 0;

        String linea = "";
        String[] campos;
        String comando = "";

        try {
            ////// Inicializamos el socket de escucha, para esperar cientes://////////////////////
            // Le indicamos al servidor TCP que escuche en el puerto "9999":
            mostrar("Abriendo puerto...", false);
            
            ServerSocket socketEscuchar; 
            socketEscuchar = new ServerSocket(puerto);
            
            mostrar("¡Abierto!", true);
            
///////////////////////////////////////////////////////////////////////////////

            // Para que sea un servidor iterativo, repetimos lo siguiente
            // por cada nueva conexión:
            boolean salir = false;
            boolean salirServicio = false;
            mostrar("Entrando en bucle de servicio a clientes...", true);
            while (!salirServicio) {

                ////////// Aceptamos una nueva conexión: ///////////////////////////////////////////
                // Esperamos a que se conecte algún cliente. El servidor se quedará
                // bloqueado hasta que llegue la primera conexión. Cuando llegue una conexión,
                // el método "accept()" devuelve un socket para recibir/enviar datos:
                mostrar(" ", true);
                mostrar("Esperando conexiones entrantes... ", false);
                Socket socketConexion = socketEscuchar.accept();
                
                // Comenzamos en el estado inicial:
                estado = estadoInicioFin;
                
                // Canales para recibir y enviar:
                PrintWriter salida = new PrintWriter(socketConexion.getOutputStream());
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socketConexion.getInputStream()));
                mostrar("¡Conexión aceptada!", true);

                salir = false;
                do {
                    /// Se recibe un mensaje:
                    mostrar("Esperando mensaje del cliente...", false);
                    linea = entrada.readLine();
                    mostrar("¡Recibido!", true);
                    /////////////////////////////

                    // interpretamos el mensaje recibido:
                    MensajesProtocolo mensaje = new MensajesProtocolo();
                    mensaje.interpretarMensajeSolicitud(linea);

                    // Si es un protocolo válido:
                    if (mensaje.getTipo() != MensajesProtocolo.mNoValido) {
                        // Según el estado en el que estamos, se espera un mensaje u otro:
                        switch (estado) {
                            case estadoInicioFin:

                                if (mensaje.getTipo() == MensajesProtocolo.mEncender) {
                                    mostrar("Enviando confirmación de encendido...", false);
                                    // Enviamos la respuesta:
                                    enviarCodigoOK(salida);
                                    mostrar("¡Enviado!", true);
                                    // Realizamos la transición:
                                    estado = estadoLogin;
                                    mostrar("Entrando en la fase de login...", true);
                                } 
                                else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                                    mostrar("Enviando confirmación de apagado...", false);
                                    enviarMApagarOK(salida);
                                    mostrar("¡Enviado!", true);
                                    salir = true;
                                }
                                else {
                                    // Si se envía cualquier otro mensaje o formato incorrecto no es válido
                                    enviarCodigoNOK(salida);
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
                                        enviarCodigoOK(salida);
                                        mostrar("¡Enviada!", true);
                                        // Realizamos la transición:
                                        estado = estadoFuncionando;
                                        mostrar("Entrando en estado de funcionamiento...", true);
                                    }else {
                                        mostrar("Enviando mensaje de error...", false);
                                        enviarCodigoNOK(salida);
                                        mostrar(" Error enviado", true);
                                    }                                                                    
                        
                                    // Si es el mensaje de solicitud de finalizar sesión:
                                }else  if (mensaje.getTipo() == MensajesProtocolo.mCerrarSesion) {
                                    mostrar("Recibida petición de cerrar sesión", true);

                                    mostrar("Enviando respuesta...", false);
                                    enviarMApagarOK(salida);
                                    mostrar("¡Enviada!", true);
                                    estado = estadoInicioFin;
                                } else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                                    mostrar("Enviando confirmación de apagado...", false);
                                    enviarMApagarOK(salida);
                                    mostrar("¡Enviado!", true);
                                    salir = true;
                                } else {
                                    // Si hay algún error, reintenta login.
                                    enviarCodigoNOK(salida);
                                    mostrar("¡Mensaje erróneo!", true);
                                }

                                break;

                            case estadoFuncionando:
                            if (mensaje.getTipo() == MensajesProtocolo.mReqLidar) {
                                mostrar("Enviando confirmación de uso de lidar...", false);
                                // Enviamos la respuesta:
                                enviarMLidarOK(salida,"43 92"); //mensaje.getParametro() si se implementase con un Lidar real
                                mostrar("¡Enviado!", true);
  
                            } else if (mensaje.getTipo() == MensajesProtocolo.mReqEstado) {
                                mostrar("Enviando confirmación de uso de sensores de choque...", true);
                                // Enviamos la respuesta:
                                enviarMResEstadoOK(salida, "NO"); //mensaje.getParametro() si se implementase con un sensor real
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mAvanzar) {
                                mostrar("Enviando confirmación de desplazamiento...", true);
                                // Enviamos la respuesta:
                                enviarCodigoOK(salida);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mParar) {
                                mostrar("Enviando confirmación de parada...", false);
                                // Enviamos la respuesta:
                                enviarCodigoOK(salida);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mRotar) {
                                mostrar("Enviando confirmación de rotacion...", false);
                                // Enviamos la respuesta:
                                enviarCodigoOK(salida);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mReqPosicion) {
                                mostrar("Enviando confirmación de uso de sensores de choque...", false);
                                // Enviamos la respuesta:
                                enviarMResPosicionOK(salida, "102 321 56"); //mensaje.getParametro() si se implementase real
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mActivarModoLotes) {
                                mostrar("Enviando confirmación de modo lotes...", false);
                                // Enviamos la respuesta:
                                enviarCodigoOK(salida);
                                mostrar("¡Enviado!", true);
                            } else if (mensaje.getTipo() == MensajesProtocolo.mCerrarSesion) {
                                mostrar("Enviando confirmación de cierre de sesión...", false);
                                // Enviamos la respuesta:
                                enviarCodigoOK(salida);
                                mostrar("¡Enviado!", true);
                                estado = estadoLogin;
                            } else if (mensaje.getTipo() == MensajesProtocolo.mApagar) {
                                mostrar("Enviando confirmación de apagado...", false);
                                enviarMApagarOK(salida);
                                mostrar("¡Enviado!", true);
                                salir = true;
                            }            
                            else {
                                // Si hay algún error sigue escuchando
                                enviarCodigoNOK(salida);
                                mostrar("¡Mensaje recibido incorrecto!", true);
                            }
                        }

                    }
                } while (!salir);

                mostrar("Cerrando conexión... ", false);
                socketConexion.close();
                mostrar("¡Cerrado! ", true);
            }

///////////////////////////////////////////////////////////////
        } catch (IOException ex) {
            System.err.println("Error de entrada/salida.");
            System.err.println("Error de entrada/salida.");
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

    private int enviarCodigoOK(PrintWriter salida) {
        int error = 0;

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearCodigoOK();

        salida.println(mensaje.serializar());
        salida.flush();
        
        return error;
    }

    private int enviarCodigoNOK(PrintWriter salida) {
        int error = 0;

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearCodigoNOK();

        salida.println(mensaje.serializar());
        salida.flush();

        return error;
    }

    private int enviarMApagarOK(PrintWriter salida) {
        int error = 0;

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMApagarOK();

        salida.println(mensaje.serializar());
        salida.flush();

        return error;
    }

    private int enviarMResEstadoOK(PrintWriter salida, String infoChoque) {
        int error = 0;

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMResEstadoOK(infoChoque);

        salida.println(mensaje.serializar());
        salida.flush();

        return error;
    }

    private int enviarMLidarOK(PrintWriter salida, String posicion) {
        int error = 0;

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMLidarOK(posicion);

        salida.println(mensaje.serializar());
        salida.flush();

        return error;
    }

    private int enviarMResPosicionOK(PrintWriter salida, String posicionActual) {
        int error = 0;

        MensajesProtocolo mensaje = new MensajesProtocolo();
        mensaje.crearMResPosicion(posicionActual);

        salida.println(mensaje.serializar());
        salida.flush();

        return error;
    }
}
