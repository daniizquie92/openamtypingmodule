/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2015 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

package org.typing.forgerock.auth;

import java.security.Principal;
import java.util.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;

import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.idm.*;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;

import org.json.simple.JSONObject;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
/**
 * SampleAuth authentication module example.
 *
 * If you create your own module based on this example, you must modify all
 * occurrences of "SampleAuth" in addition to changing the name of the class.
 *
 * Please refer to OpenAM documentation for further information.
 *
 * Feel free to look at the code for authentication modules delivered with
 * OpenAM, as they implement this same API.
 */
public class TypingAuth extends AMLoginModule{

    // Name for the debug-log
    private final static String DEBUG_NAME = "TypingAuth";
    private final static Debug debug = Debug.getInstance(DEBUG_NAME);

    // Name of the resource bundle
    private final static String amAuthSampleAuth = "amAuthTypingAuth";

    // User names for authentication logic
    private static String USERNAME = "demo";

    private final static String ERROR_1_USERNAME = "test1";
    private final static String ERROR_2_USERNAME = "test2";

    // Orders defined in the callbacks file
    private final static int STATE_BEGIN = 1;
    private final static int STATE_AUTH = 2;
    private final static int STATE_ERROR = 3;

    // Errors properties

    private Map<String, String> options;
    private ResourceBundle bundle;
    private Map<String, String> sharedState;
    private String api_key;
    private String api_secret;
    private TypingDNAapi api;

    private String phrase;

    //PATRONES ALEATORIOS QUE LE PUEDEN SALIR AL USUARIO LA PRIMERA VEZ QUE SE REGISTRA
    private String patterns[] = {"la abeja zumbaba y queria miel mientras veia la television",
            "quetzal murcielago eucalipto 1960",
            "el otro dia comi chocolate con leche",
            "alfombra regaliz mesa cortina",
            "cama piedra encarancunquintar tortuga"};

    public TypingAuth() {
        super();
    }


    /**
     * This method stores service attributes and localized properties for later
     * use.
     * @param subject
     * @param sharedState
     * @param options
     */
    @Override
    public void init(Subject subject, Map sharedState, Map options) {

        debug.message("TypingAuth::init");

        this.options = options;
        this.sharedState = sharedState;
        this.bundle = amCache.getResBundle(amAuthSampleAuth, getLoginLocale());
        this.api_key = CollectionHelper.getMapAttr(options, "iplanet-am-auth-typingauth-apikey");
        this.api_secret = CollectionHelper.getMapAttr(options, "iplanet-am-auth-typingauth-api_secret");
        this.api = new TypingDNAapi(this.api_key, this.api_secret);
    }

    /**
     * Main method of the authentication module.
     * @param callbacks Calback array specified in the .xml file containing all the Callbacks
     * @param state indicates the loading point we are at, Can be 0-(Begin) 1-(Auth) 2-(Error) -1-LoginSucceed
     *
     * @return A String with the image coded in B64
     */
    @Override
    public int process(Callback[] callbacks, int state) throws LoginException {

        debug.message("SampleAuth::process state: {}", state);

        switch (state) {

            case STATE_BEGIN:
                // CAMBIAMOS LA UI Y COMPROBAMOS SI SE ESTÁN PASANDO CREDECIALES DE UN MÓDULO ANTERIOR
                // SI SE ESTÁN PASANDO, RESCATAMOS LA FRASE YA REGISTRADA DEL USUARIO (EN CASO DE EXISTIR) Y LA ESTABLECEMOS EN LA UI TIPO CAPTCHA
                // SI LOS ENROLLMENTS DEL USUARIO ESTÁN A 0, SE BORRA LA FRASE QUE TENGA DEL REGISTRO
                substituteHeader(STATE_AUTH, bundle.getString("sampleauth-ui-login-header"));

                replaceCallback(STATE_AUTH, 0, new NameCallback(
                        bundle.getString("sampleauth-ui-username-prompt")));
                if (this.sharedState.size() > 0) {
                    try {
                        String username = (String) sharedState.get(getUserKey());
                        replaceCallback(STATE_AUTH,0, new TextOutputCallback(TextOutputCallback.INFORMATION, "Bienvenido " + username));

                        try {
                            AMIdentityRepository idrepo = getAMIdentityRepository(
                                    getRequestOrg());
                            IdType type = IdType.USER;
                            IdSearchControl ctrl = new IdSearchControl();
                            ctrl.setAllReturnAttributes(true);
                            ctrl.setMaxResults(1);
                            IdSearchResults r = idrepo.searchIdentities(type,username,ctrl);
                            AMIdentity identity = (AMIdentity) r.getSearchResults().iterator().next();
                            Set attrss = identity.getAttribute("typingPhrase");
                            if (attrss.iterator().hasNext()) {
                                this.phrase = (String) attrss.iterator().next();
                                replaceCallback(STATE_AUTH, 3, new TextOutputCallback(1, "Escriba esta frase: " + phrase));
                                debug.message("user has a phrase");

                                //GENERAR Y ESTABLECER EL CAPTCHA
                                String img = genCaptcha64(phrase);
                                TextOutputCallback cap = new TextOutputCallback(TextOutputCallback.INFORMATION, img);
                                replaceCallback(STATE_AUTH, 6, cap);
                                //HttpServletRequest request = getHttpServletRequest();

                                //El administrador podrá poner los enrollments a 0 para forzar al usuario a volver a escribir la frase del atributo typingPhrase
                                Set attrEnrol = identity.getAttribute("typingEnrollments");
                                if (attrEnrol.iterator().hasNext()) {
                                    int enroll = Integer.parseInt((String)attrEnrol.iterator().next());
                                    if (enroll == 0){
                                        api.deleteUser(username, phrase);
                                        debug.message("eliminando phrase: " + phrase + " del usuario " + username);
                                    }
                                }
                            }else {
                                genRandomPhrase();
                            }
                        }catch(Exception ex){
                            debug.message("error obtaining user phrase and enrollments in case BEGIN");
                        }

                    } catch (Exception ex) {
                        debug.message("No username loaded");
                    }

                }else {
                    genRandomPhrase();
                }
                return STATE_AUTH;

            case STATE_AUTH:
                // Get data from callbacks. Refer to callbacks XML file
                // Establecemos el usuario en caso de venir de otro módulo
                String username = "";
                if (this.sharedState.size() > 0){
                    try{
                        username = (String) sharedState.get(getUserKey());
                    }catch(Exception ex){
                        debug.message("No username loaded");
                    }
                }else{
                    NameCallback nc = (NameCallback) callbacks[0];
                    username = nc.getName();
                }

                if (username.equals("")){
                    substituteHeader(STATE_ERROR, "No hay usuario");
                    return STATE_ERROR;
                }

                // obtenemos la frase introducida por el usuario y comprobamos su validez
                TextInputCallback fc = (TextInputCallback) callbacks[4];
                String frase = fc.getText();
                boolean valid = getSameCount(this.phrase,frase,3);

                HiddenValueCallback hc = (HiddenValueCallback) callbacks[5];
                String tp = hc.getValue();

                //tp es un callback oculto en el formulario que guarde el patron recolectado por el js
                //if (tp.equals("invalid") || tp.equals("hid") || tp.equals("")){
                if (!valid && !(tp.equals("invalid") || tp.equals("hid") || tp.equals(""))){
                    //substituteHeader(STATE_ERROR, "INSERTE EL TEXTO CORRECTAMENTE");
                    replaceCallback(STATE_AUTH,1, new TextOutputCallback(1, "INSERTE EL TEXTO CORRECTAMENTE"));
                    replaceCallback(STATE_AUTH, 4, new TextInputCallback("Introduzca el texto"));
                    return STATE_AUTH;
                }

                //Se colecta el patron introducido por el usuario y se envía a la api mediante el endpoint auto.
                //Si la frase introducida por el usuario no se ha registrado antes, o no las suficientes veces,
                //el message no será Done
                //tendremos que hacer volver a escribir la frase al usuario hasta que esta salga como Done.
                //Si despues de Done el result es 0, no será un login válido y habrá que volver a introducirlo
                JSONObject respuestaApiAuto = api.getAuto(username, tp);
                if (respuestaApiAuto == null){
                    throw new AuthLoginException("Error en la peticion a la api");
                }
                if ((Long)respuestaApiAuto.get("message_code") != 1 && (Long)respuestaApiAuto.get("message_code") != 10) {
                    substituteHeader(STATE_ERROR, "Error interno, contacte con un administrador");
                    return STATE_ERROR;
                }else{
                    if ((Long)respuestaApiAuto.get("message_code") == 1){
                        if ((Long)respuestaApiAuto.get("result") == 0) {
                            //setErrorText("No coinciden los patrones de escritura");
                            substituteHeader(STATE_ERROR, "No coinciden los patrones de escritura");
                            return STATE_ERROR;
                        }else {
                            debug.message("TypingAuth::process User '{}' " +
                                    "authenticated with success.", username);
                            USERNAME = username;
                            return ISAuthConstants.LOGIN_SUCCEED;
                        }
                    }else{
                        //repetir el enrollment hasta que el mensaje sea done
                        //si hay que repetirlo, se carga un mensaje pidiendo que se repita el número de veces restantes
                        //substituteHeader(STATE_ERROR, "Vuelve a escribir la frase");
                        replaceCallback(STATE_AUTH,1, new TextOutputCallback(1, "REPITA LA FRASE"));
                        replaceCallback(STATE_AUTH, 4, new TextInputCallback("Introduzca el texto"));

                        AMIdentityRepository idrepo = getAMIdentityRepository(
                                getRequestOrg());
                        try {
                            IdSearchControl ctrl = new IdSearchControl();
                            ctrl.setAllReturnAttributes(true);
                            ctrl.setMaxResults(1);
                            IdSearchResults r = idrepo.searchIdentities(IdType.USER,username,ctrl);
                            if (r.getSearchResults().iterator().hasNext()){
                                AMIdentity identity = (AMIdentity) r.getSearchResults().iterator().next();
                                Map<String, Set<String>> attrs = new HashMap<String, Set<String>>();
                                //ALMACENAR LA FRASE PARA QUE LE SALGA SIEMPRE LA MISMA
                                Set<String> setPhrase = new HashSet<String>();
                                TextOutputCallback text = (TextOutputCallback) callbacks[3];
                                setPhrase.add(text.getMessage().substring(20));
                                attrs.put("typingPhrase", setPhrase);

                                //COMPROBAR SI HAY ENROLLMENTS Y AUMENTARLE EL VALOR
                                Set<String> setEnroll = new HashSet<String>();
                                Set attrEnrol = identity.getAttribute("typingEnrollments");
                                int enroll = 0;
                                if (attrEnrol.iterator().hasNext()) {
                                    enroll = Integer.parseInt((String)attrEnrol.iterator().next());
                                    enroll += 1;
                                    String enrollS = Integer.toString(enroll);
                                    setEnroll.add(enrollS);
                                }else{
                                    enroll = 1;
                                    setEnroll.add("1");
                                }
                                attrs.put("typingEnrollments", setEnroll);
                                identity.setAttributes(attrs);
                                identity.store();
                                replaceCallback(STATE_AUTH,1, new TextOutputCallback(1, "REPITA LA FRASE"));
                                replaceCallback(STATE_AUTH,2, new TextOutputCallback(1, "QUEDAN "+ (4-enroll) + " VECES MÁS"));
                            }

                            debug.message("id");
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }

                        return STATE_AUTH;
                    }
                }

            case STATE_ERROR:
                return STATE_ERROR;
            default:
                throw new AuthLoginException("invalid state");
        }
    }

    @Override
    public Principal getPrincipal() {
        return new TypingAuthPrincipal(USERNAME);
    }


    /**
     * generates and
     * places a random phraseI.
     */
    private void genRandomPhrase() throws AuthLoginException {
        Random random = new Random();
        int texto = random.nextInt(patterns.length);
        replaceCallback(STATE_AUTH,3, new TextOutputCallback(1, "Escriba esta frase: " + patterns[texto]));
        String img = genCaptcha64(patterns[texto]);
        replaceCallback(STATE_AUTH, 6, new TextOutputCallback(TextOutputCallback.INFORMATION, img));
        
        this.phrase = patterns[texto];

    }

    /**
     * This method generates a CAPTCHA style image with a phrase set by the user
     * @param frase the String to be written in the image
     *
     * @return A String with the image coded in Base64
     */
    private String genCaptcha64(String frase){
        try {
            CaptchaGenerator gen = new CaptchaGenerator();
            String result = gen.encodeBase64(gen.createCaptcha(frase));
            return result;
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("hola");
        }
        return "";
    }

    /**
     * This method compares the different characters in two Strings
     * @param str1 the String to be compared
     * @param str2 the other String to be compared
     * @param fallos margin of different characters allowed
     * @return True if the Strings differ in less or equal than fallos, else False
     */
    private boolean getSameCount(String str1, String str2, int fallos) {
        int count = 0;
        List<String> obj = new LinkedList<String>(Arrays.asList(str2.split("")));
        for(String str : str1.split("")){
            int idx = obj.indexOf(str);
            if(idx >= 0){
                obj.remove(idx);
            }else{
                count++;
            }
        }
        if (obj.size() + count <= fallos){
            return true;
        }else{
            return false;
        }
    }
}
