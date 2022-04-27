package ProtocoloRobot;

/**
 *
 * @author Raquel Romero Pedraza
 */

public class MensajesProtocolo {
    
    // Tipos de mensaje y sus códigos de referencia
    public static final int mEncender = 10;
    public static final int mLogin = 11;
    public static final int mReqLidar = 12;
    public static final int mReqEstado = 13;
    public static final int mAvanzar = 14;
    public static final int mParar = 15;
    public static final int mRotar = 16;
    public static final int mReqPosicion = 17;
    public static final int mActivarModoLotes = 18;
    public static final int mCerrarSesion = 19;
    public static final int mApagar = 20;

    public static final int mLidarOK = 21;
    public static final int mResEstadoOK = 22;
    public static final int mResPosicionOK = 23;
    public static final int mApagarOK = 24;

    public static final int codigoOK = 1;
    public static final int codigoNOK = 2;
    public static final int mNoValido = -1;

    static final String DEL = " ";
    static final String DELFIN = ";";
    
    // Tipo de mensaje:
    int tipoMensaje = mNoValido;

    String parametro = "";
    private int errorFormatoMensajeIncorrecto = -1;

    /**
     * Interpreta una línea de texto
     *
     * @param linea
     */
    public int interpretarMensajeSolicitud(String linea) {
        int error = 0;
        int tipo = obtenerTipoMensajeSolicitud(linea);
        String[] campos = linea.split(DEL);
        int nCampos = campos.length;

        if (nCampos >= 1) {

            switch (tipo) {
                case mEncender:
                case mReqLidar:
                case mReqEstado:
                case mParar:
                case mReqPosicion:
                case mCerrarSesion:
                case mApagar:
                    if (nCampos != 2) {
                        error = errorFormatoMensajeIncorrecto;  
                    }
                    break;

                case mLogin:
                    if (nCampos == 4) {
                        String username = campos[2];
                        String password = campos[3];
                        parametro = username + DEL + password;
                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;

                case mAvanzar:
                    if (nCampos == 3) {
                        String tiempo = campos[2];

                        if (!esEntero(tiempo)) {
                            error = errorFormatoMensajeIncorrecto;

                        } else {
                            parametro = tiempo;
                        }

                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;

                case mRotar:
                    if (nCampos == 4) {
                        String direccion = campos[2];
                        String grados = campos[3];

                        if (!esEntero(direccion)|| !esEntero(grados)) {
                            error = errorFormatoMensajeIncorrecto;

                        } else {
                            parametro = direccion + DEL + grados;
                        }

                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;
                
                case mActivarModoLotes:
                    if (nCampos >= 4) {
                        String lote[];
                        lote = new String[5];
                        int j = 0;
                        for (int i = 2 ; i < campos.length ; i++) {
                            if (campos[i] != null) {
                                lote[j] = campos[i];
                                j++;
                            }
                        }
 
                        for (int i = 0 ; i < lote.length ; i++) {
                            if (esEntero(lote[i])|| esEntero(lote[i])) {
                                error = errorFormatoMensajeIncorrecto;
    
                            } else {
                                for (int k = 0 ; k < lote.length ; k++) {
                                    parametro = parametro + DEL + lote[k];
                                }
                            }
                        }
                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;
            }
        }

        tipoMensaje = tipo;

        return error;
    }

    /**
     * Interpreta una línea de texto
     *
     * @param linea
     */
    public int interpretarMensajeRespuesta(String linea) {
        int error = 0;
        int tipo = obtenerTipoMensajeRespuesta(linea);
        String[] campos = linea.split(DEL);
        int nCampos = campos.length;

        if (nCampos >= 1) {

            switch (tipo) {
                case codigoOK:
                case codigoNOK:
                case mApagarOK:
                    if (nCampos > 3) {
                        error = errorFormatoMensajeIncorrecto;  
                        }
                    break;
                
                case mLidarOK:
                    if (nCampos == 4) {
                        String lidar = campos[2] + DEL + campos[3];  

                        if (!esEntero(lidar)) {
                            error = errorFormatoMensajeIncorrecto;

                        } else {
                            parametro = lidar;
                        }

                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;

                case mResEstadoOK:
                    if (nCampos == 3) {
                        String estado = campos[2];

                        if (esEntero(estado)) {
                            error = errorFormatoMensajeIncorrecto;

                        } else {
                            parametro = estado;
                        }

                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;
                                              
                case mResPosicionOK:
                    if (nCampos == 5) {
                        String posicion = campos[2] + DEL + campos[3] + DEL + campos[4];

                        if (!esEntero(posicion)) {
                            error = errorFormatoMensajeIncorrecto;

                        } else {
                            parametro = posicion;
                        }

                    } else {
                        error = errorFormatoMensajeIncorrecto;
                    }
                    break;
                   
                case mNoValido:

                    break;
            }
        }

        tipoMensaje = tipo;

        return error;
    }

    public int obtenerTipoMensajeSolicitud(String linea) {
        int tipo = mNoValido;

        if (linea.startsWith("100 ")) {
            tipo = mEncender;

        } else if (linea.startsWith("101 ")) {
            tipo = mLogin;

        } else if (linea.startsWith("102 ")) {
            tipo = mReqLidar;

        } else if (linea.startsWith("103 ")) {
            tipo = mReqEstado;

        }else if (linea.startsWith("104 ")) {
            tipo = mAvanzar;

        } else if (linea.startsWith("105 ")) {
            tipo = mParar;

        } else if (linea.startsWith("106 ")) {
            tipo = mRotar;

        } else if (linea.startsWith("107 ")) {
            tipo = mReqPosicion;

        } else if (linea.startsWith("108 ")) {
            tipo = mActivarModoLotes;

        } else if (linea.startsWith("109 ")) {
            tipo = mCerrarSesion;

        } else if (linea.startsWith("110 ")) {
            tipo = mApagar;
        }
        
        return tipo;
    }

    public int obtenerTipoMensajeRespuesta(String linea) {
        int tipo = mNoValido;

        if (linea.startsWith("201 ")) {
            tipo = mLidarOK;

        } else if (linea.startsWith("202 ")) {
            tipo = mResEstadoOK;

        } else if (linea.startsWith("203 ")) {
            tipo = mResPosicionOK;

        } else if (linea.startsWith("000 ")) {
            tipo = mApagarOK;

        } else if (linea.startsWith("200 ")) {
            tipo = codigoOK;

        } else if (linea.startsWith("400 ")) {
            tipo = codigoNOK;

        }
        return tipo;
    }

    boolean esEntero(String numero) {
        boolean es = false;

        try {
            Integer.parseInt(numero);
            es = true;
        } catch (NumberFormatException ex) {
            es = false;
        }
        return es;
    }

    public int getTipo() {
        return tipoMensaje;
    }

    public String getParametro() {
        return parametro;
    }

    /**
     * Devuelve la representación de este mensaje en formato de cadena de caracteres
     * @return 
     */
    public String serializar() {
        String serializado = "";

        switch (tipoMensaje) {
            case mEncender:
                serializado = "100" + DEL + "ROBOT" + DEL + "HELLO" + DELFIN;
                break;
            case mLogin:
                serializado = "101" + DEL + "LOGIN" + DEL + parametro + DELFIN;
                break;
            case mReqLidar:
                serializado = "102" + DEL + "LIDAR" + DELFIN;
                break;
            case mReqEstado:
                serializado = "103" + DEL + "ESTADO" + DELFIN;
                break;
            case mAvanzar:
                serializado = "104" + DEL + "AVANZA" + DEL + parametro + DELFIN;
                break;
            case mParar:
                serializado = "105" + DEL + "PARAR" + DELFIN;
                break;
            case mRotar:
                serializado = "106" + DEL + "ROTA" + DEL + parametro + DELFIN;
                break;
            case mReqPosicion:
                serializado = "107" + DEL + "POS" + DELFIN;
                break;
            case mActivarModoLotes:
                serializado = "108" + DEL + "LOTE:" + DEL + parametro + DELFIN;
                break;
            case mCerrarSesion:
                serializado = "109" + DEL + "LOGOUT" + DELFIN;
                break;
            case mApagar:
                serializado = "110" + DEL + "SHUTDOWN" + DELFIN;
                break;        
            case mLidarOK:
                serializado = "201" + DEL + "LIDAR" + DEL + parametro + DELFIN;
                break;
            case mResEstadoOK:
                serializado = "202" + DEL + "CHOQUE" + DEL + parametro + DELFIN;
                break;
            case mResPosicionOK:
                serializado = "203" + DEL + "COOR" + DEL + parametro + DELFIN;
                break;
            case codigoOK:
                serializado = "200" + DEL + "OK" + DELFIN;
                break;
            case codigoNOK:
                serializado = "400" + DEL + "BAD REQUEST" + DELFIN;
                break;
            case mApagarOK:
                serializado = "000" + DEL + "BYE" + DELFIN;
                break;

        }

        return serializado;
    }

    
    public int crearMLidarOK(String posicion) {
        int error = 0;

        tipoMensaje = mLidarOK;
        parametro = posicion;

        return error;
    }

    public int crearMResEstadoOK(String infoChoque) {
        int error = 0;

        tipoMensaje = mResEstadoOK;
        parametro = infoChoque;

        return error;
    }

    public int crearMResPosicion(String posicionActual) {
        int error = 0;

        tipoMensaje = mResPosicionOK;
        parametro = posicionActual;

        return error;
    }

    public int crearMApagarOK() {
        int error = 0;

        tipoMensaje = mApagarOK;

        return error;
    }

    public int crearCodigoOK() {
        int error = 0;

        tipoMensaje = codigoOK;

        return error;
    }

    public int crearCodigoNOK() {
        int error = 0;

        tipoMensaje = codigoNOK;

        return error;
    }

}
