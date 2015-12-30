/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package edu.gatech.wguo64.module_oauth2.backend.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;

import edu.gatech.wguo64.module_oauth2.backend.constants.Constants;
import edu.gatech.wguo64.module_oauth2.backend.entities.MyBean;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.module_oauth2.wguo64.gatech.edu",
                ownerName = "backend.module_oauth2.wguo64.gatech.edu",
                packagePath = ""
        ),
        scopes = {Constants.EMAIL_SCOPE, Constants.PROFILE_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID,
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class MyEndpoint {

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        return response;
    }

    @ApiMethod(name = "sayHello")
    public MyBean sayHello(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hello, " + name);

        return response;
    }



}
