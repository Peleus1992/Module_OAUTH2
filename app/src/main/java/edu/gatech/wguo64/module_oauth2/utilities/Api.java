package edu.gatech.wguo64.module_oauth2.utilities;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

import edu.gatech.wguo64.module_oauth2.backend.myApi.MyApi;

/**
 * Created by guoweidong on 12/29/15.
 */
public class Api {
    private static MyApi api = null;
    public static void initialize(GoogleAccountCredential credential) {
        if(api == null) {
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), credential)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("https://" + Constants.GOOGLE_PROJECT_ID + ".appspot.com/_ah/api/")
                    .setApplicationName("Module OAUTH2")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            api = builder.build();
        }
    }
    public static MyApi getApi() {
        return api;
    }
}
