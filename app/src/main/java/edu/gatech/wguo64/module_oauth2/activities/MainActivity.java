package edu.gatech.wguo64.module_oauth2.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edu.gatech.wguo64.module_oauth2.R;
import edu.gatech.wguo64.module_oauth2.backend.myApi.model.CollectionResponseProduct;
import edu.gatech.wguo64.module_oauth2.backend.myApi.model.Product;
import edu.gatech.wguo64.module_oauth2.utilities.Api;
import edu.gatech.wguo64.module_oauth2.utilities.Constants;
import edu.gatech.wguo64.module_oauth2.utilities.RoundImage;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener
        , ResultCallback
        , View.OnClickListener {
    private GoogleApiClient googleApiClient;

    private Button signOutBtn, notifyBtn, addBtn, showBtn, moreBtn, clearBtn;
    public ImageView photoImg;
    public TextView emailTxt, messageTxt;
    public EditText productEdit, priceEdit;

    public final static String TAG = "MainActivity";
    private SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(Constants.PREFERENCE, MODE_PRIVATE);

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
        notifyBtn = (Button)findViewById(R.id.notify_btn);
        notifyBtn.setOnClickListener(this);
        addBtn = (Button)findViewById(R.id.add_btn);
        addBtn.setOnClickListener(this);
        showBtn = (Button)findViewById(R.id.show_btn);
        showBtn.setOnClickListener(this);
        moreBtn = (Button)findViewById(R.id.more_btn);
        moreBtn.setOnClickListener(this);
        clearBtn = (Button)findViewById(R.id.clear_btn);
        clearBtn.setOnClickListener(this);

        photoImg = (ImageView)findViewById(R.id.photo_img);
        emailTxt = (TextView)findViewById(R.id.email_txt);
        messageTxt = (TextView)findViewById(R.id.message_txt);
        messageTxt.setMovementMethod(new ScrollingMovementMethod());

        productEdit = (EditText)findViewById(R.id.product_edit);
        priceEdit = (EditText)findViewById(R.id.price_edit);

        GoogleAccountCredential credential = GoogleAccountCredential
                .usingAudience(this,
                        "server:client_id:" + Constants.WEB_CLIENT_ID);
        credential.setSelectedAccountName(preferences.getString(Constants.PREFERENCE_ACCOUNT_EMAIL, null));
        Api.initialize(credential);
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
            case R.id.notify_btn:
                break;
            case R.id.add_btn:
                submitProduct();
                break;
            case R.id.show_btn:
                listProducts(true);
                break;
            case R.id.more_btn:
                listProducts(false);
                break;
            case R.id.clear_btn:
                messageTxt.setText("");
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
    private Product getProduct() {
        if("".equals(productEdit.getText().toString()) ||
                "".equals(priceEdit.getText().toString())) {
            return null;
        }
        Product product = new Product();
        product.setName(productEdit.getText().toString());
        try {
            product.setPrice(Double.parseDouble(priceEdit.getText().toString()));
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            return null;
        }
        return product;
    }

    private void submitProduct() {
        Product product = null;
        if((product = getProduct()) != null) {
            new AsyncTask<Product, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Product... params) {
                    try {
//                        Log.d(TAG, params[0].getName() + "/" + params[0].getPrice());
                        if(Api.getApi().product().insert(params[0]).execute() == null) {
                            Log.d(TAG, "product.insert failed.");
                            return false;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "submitProduct:" + e.getLocalizedMessage());
                        return false;
                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean res) {
                    super.onPostExecute(res);
                    if(res) {
                        productEdit.setText("");
                        priceEdit.setText("");
                        Toast.makeText(MainActivity.this, "Successfully added.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute(product);
        } else {
            Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private static String cursor = null;
    private static int count = 0;
    private void listProducts(boolean fromStart) {
        if(cursor != null && fromStart) {
            cursor = null;
            count = 0;
        }
        new AsyncTask<Void, Void, List<Product>>() {
            @Override
            protected List<Product> doInBackground(Void... params) {
                try {
                    CollectionResponseProduct products = null;
                    Log.d(TAG, "products.list:Begin Cursor:" + cursor);
                    if(cursor == null) {
                        products = Api.getApi()
                                .product().list().execute();
                    } else {
                        products = Api.getApi()
                                .product().list().setCursor(cursor).execute();
                    }
                    cursor = products.getNextPageToken();
                    Log.d(TAG, "products.list:End Cursor:" + cursor);
                    List<Product>  res = products.getItems();
                    return res;
                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Product> products) {
                super.onPostExecute(products);
                if(products != null) {
                    for (Product product : products) {
                        messageTxt.append(++count + ". " + product.getName() + "/"
                                + product.getPrice() + "\n");
                    }
                }
            }
        }.execute();
    }
}
