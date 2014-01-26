package com.saibi.ereader.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.android.AuthActivity;
import com.saibi.ereader.EReaderManager;
import com.saibi.ereader.R;

@SuppressWarnings("unused")
public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
    
    public static final int LOGIN_REQUEST = 0x01;
    
    EReaderManager mDropboxManager;
	
	/**
	 * Boton para logearse con dropbox
	 */
	private Button mSubmit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Layout para logearse con diferentes plataformas
        setContentView(R.layout.activity_login);

        // Lo voy a dejar ya que no subo api keys reales al repo
        checkAppKeySetup();
        
        mDropboxManager = EReaderManager.getInstance();
        
//        mSubmit = (Button)findViewById(R.id.auth_button);
//
//        mSubmit.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//            	
//                mDropboxManager.doLogin(LoginActivity.this, LOGIN_REQUEST);
//            }
//        });
        
        if(mDropboxManager.isLoggedIn())
        	showEbookList();
        else
        	mDropboxManager.doLogin(LoginActivity.this, LOGIN_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == LOGIN_REQUEST)
		{
			if(resultCode == RESULT_OK)
				showEbookList();
			
			else
				// Si no se logea correctamente cerramos la aplicacion
				finish();
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    /**
     * Mostramos la pantalla del listado de .epubs
     */
    private void showEbookList()
    {
    	Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    	finish();
    	startActivity(intent);
    }

    
    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (EReaderManager.APP_KEY.startsWith("CHANGE") ||
        		EReaderManager.APP_SECRET.startsWith("CHANGE")) {
            showToast(	"You must apply for an app key and secret from developers.dropbox.com, " +
            			"and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + EReaderManager.APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }
    
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
