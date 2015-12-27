package edu.gatech.wguo64.module_oauth2;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.service.carrier.CarrierMessagingService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.gatech.wguo64.module_oauth2.utilities.Constants;
import edu.gatech.wguo64.module_oauth2.utilities.RoundImage;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
        , ResultCallback
        , View.OnClickListener {
    private GoogleApiClient googleApiClient;

    private Button signOutBtn;
    public ImageView photoImg;
    public TextView emailTxt;

    public final static String TAG = "MainActivity";
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signOutBtn = (Button)findViewById(R.id.sign_out_btn);
        signOutBtn.setOnClickListener(this);

        photoImg = (ImageView)findViewById(R.id.photo_img);
        emailTxt = (TextView)findViewById(R.id.email_txt);

        preferences = getSharedPreferences(Constants.PREFERENCE, MODE_PRIVATE);

        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out_btn:
                if(googleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(this);
                }
                break;
        }
    }

    @Override
    public void onResult(Result result) {
        if(result.getStatus().isSuccess()) {
            finish();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, connectionResult.getErrorMessage());
    }

    private void updateUI() {
        // Update account email address
        emailTxt.setText(preferences.getString(Constants.PREFERENCE_ACCOUNT_EMAIL, "NULL"));
        // Update account photo.
        // We use AsyncTask because we only have photo url and we need download
        // which should not be in UI thread.
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.connect();
                    if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BitmapDrawable drawable = (BitmapDrawable)BitmapDrawable.createFromStream(connection.getInputStream(), params[0]);
                        return drawable.getBitmap();
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if(bitmap != null) {
                    RoundImage roundImage = new RoundImage(bitmap);
                    photoImg.setImageDrawable(roundImage);
                }
            }
        }.execute(preferences.getString(Constants.PREFERENCE_ACCOUNT_PHOTO_URL, "NULL"));
    }
}
