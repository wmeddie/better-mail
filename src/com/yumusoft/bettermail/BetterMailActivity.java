/* Copyright (C) 2011 Eduardo Gonzalez See LICENSE for details. */

package com.yumusoft.bettermail;

import static com.yumusoft.bettermail.Globals.DEBUG;

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.googlelogin.GoogleLoginServiceHelper;
import com.yumusoft.bettermail.Eula.OnEulaAgreedTo;

public class BetterMailActivity extends Activity implements OnEulaAgreedTo {
	
	private static final int LOGIN_CODE = 1000;

	private static final String GMAIL_URL = "https://mail.google.com/";
	private static final int EXTERNAL_LINK_DIALOG_ID = 1;
	private static final int ERROR_DIALOG_ID = 2;
	private static final int DOWNLOAD_DIALOG_ID = 3;
	private static final int ACCOUNT_ERROR_DIALOG_ID = 5;
	private static final int FIRST_RUN_DIALOG_ID = 6;
	
	private WebView _webView;
	private String _url;
	private boolean _isLoading;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 
        		Window.PROGRESS_VISIBILITY_ON);
        
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
        _webView = new WebView(this);
        
        WebSettings settings = _webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        settings.setSupportZoom(false);
        //settings.setSupportMultipleWindows(false); // Cookie problem?
        settings.setCacheMode(WebSettings.LOAD_NORMAL);
        
        _webView.setVerticalScrollbarOverlay(true);
        _webView.setVerticalScrollBarEnabled(false);

        _webView.setWebViewClient(new NonRedirectingWebViewClient());
        _webView.setWebChromeClient(new ProgressObervingChromeClient());
        
        _webView.loadUrl(GMAIL_URL);
        
        setContentView(_webView);
		
		if (Eula.show(this)) {
			// EULA already agreed to.
			saveAccountInPreferences();
		}
    }
    
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	try {
    		if (requestCode == LOGIN_CODE) {
    			String[] accounts = data.getExtras().getStringArray("accounts");
    			if (accounts[0] != null) {
    				if (DEBUG) 
    					Log.d("BetterMail", "Got Username: " + accounts[0]);
    				
    				SharedPreferences pref = 
    					PreferenceManager.getDefaultSharedPreferences(this);
    				
    				SharedPreferences.Editor editor = pref.edit();
    				
    				editor.putString(getString(R.string.pref_email), 
    						accounts[0]);
    				editor.commit();
    				
    			}
    		} 
    	} catch (Exception e) {
    		showDialog(ACCOUNT_ERROR_DIALOG_ID);
    		Log.e("BetterMail", "Unable to access Gmail account username.");
    	}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	
    	CookieSyncManager.getInstance().startSync();
    }

	@Override
    public void onPause() {
    	super.onPause();
    	CookieSyncManager.getInstance().stopSync();
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	AlertDialog.Builder builder;
    	
    	if (DEBUG)
    		Log.d("BetterMail", "Creating Dialog:" + id);
    	
    	switch (id) {
    	case EXTERNAL_LINK_DIALOG_ID:
    		
    		builder = new AlertDialog.Builder(this);
    		builder.setMessage(R.string.dialog_external_link_message)
    		       .setCancelable(false)
    		       .setPositiveButton(R.string.dialog_button_yes, 
    		    		   new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   Intent browserIntent = 
    		        		   new Intent("android.intent.action.VIEW", 
    		        				   Uri.parse(BetterMailActivity.this.getUrl()));
    		        	   startActivity(browserIntent);
    		           }
    		       })
    		       .setNegativeButton(R.string.dialog_button_cancel, 
    		    		   new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
    		dialog = builder.create();
    		break;
    	case ERROR_DIALOG_ID:
    		builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.dialog_network_error_title)
    			   .setMessage(R.string.dialog_network_error_message)
    			   .setPositiveButton(R.string.dialog_button_ok, 
    					   new DialogInterface.OnClickListener() {
    				   public void onClick(DialogInterface dialog, int id) {
    					   dialog.dismiss();
    				   }
    			   });
    		dialog = builder.create();
    		break;
    	case DOWNLOAD_DIALOG_ID:
    		builder = new AlertDialog.Builder(this);
    		builder.setMessage(R.string.dialog_attachment_message)
    			   .setPositiveButton(R.string.dialog_button_ok,
    					   new DialogInterface.OnClickListener() {
    				   public void onClick(DialogInterface dialog, int id) {
    					   Intent i = new Intent(Intent.ACTION_VIEW, 
    							   Uri.parse(BetterMailActivity.this.getUrl()));
    					   startActivity(i);
    				   }
    			   })
    			   .setNegativeButton(R.string.dialog_button_cancel, 
    					   new DialogInterface.OnClickListener() {
    				   public void onClick(DialogInterface dialog, int id) {
    					   dialog.dismiss();
    				   }
				   });
    		dialog = builder.create();
    		break;
    	case ACCOUNT_ERROR_DIALOG_ID:
    		builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.dialog_no_account_title)
    			   .setMessage(R.string.dialog_no_account_message)
    			   .setNeutralButton(R.string.dialog_button_ok,
    					   new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
    		dialog = builder.create();
    		break;
    	case FIRST_RUN_DIALOG_ID:
    		builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.dialog_first_run_title)
    			.setMessage(R.string.dialog_first_run_message)
    			.setNeutralButton(R.string.dialog_button_ok, 
    					new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					dialog.dismiss();
    				}
    			});
    		dialog = builder.create();
    		break;
    	default:
    		if (DEBUG)
    			Log.w("BetterMail", "Invalid Dialog.");
    		dialog = null;
    	}
    	
		return dialog;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (_webView.canGoBack()) {
    			_webView.goBack();
    			return true;
    		}
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
   		MenuItem stop = menu.getItem(0);
   		MenuItem refresh = menu.getItem(1);

    	if (_isLoading) {
    		stop.setVisible(true);
    		refresh.setVisible(false);
    	} else {
    		stop.setVisible(false);
    		refresh.setVisible(true);
    	}
    	
    	return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem item;

    	/*
    	item = menu.add(r.string.menu_back);
    	item.setIcon(R.drawable.ic_menu_back);
    	if (!_webView.canGoBack()) {
    		item.setEnabled(false);
    	}
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				if (_webView.canGoBack()) {
					_webView.goBack();
				}
				return false;
			}
    	});
    	 */
    	    	
    	item = menu.add(R.string.menu_stop);
    	item.setIcon(R.drawable.ic_menu_stop);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem item) {
    			_webView.stopLoading();
    			return false;
    		}
    	});	

    	item = menu.add(R.string.menu_refresh);
    	item.setIcon(R.drawable.ic_menu_refresh);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem item) {
    			_webView.reload();
    			return false;
    		}
    	});
    	
    	item = menu.add(R.string.menu_forward);
    	item.setIcon(R.drawable.ic_menu_forward);
    	if (!_webView.canGoForward()) {
    		item.setEnabled(false);
    	}
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				if (_webView.canGoForward()) {
					_webView.goForward();
				}
				return false;
			}
    	});
    	
    	item = menu.add(R.string.menu_inbox);
    	item.setIcon(R.drawable.ic_menu_home);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				_webView.loadUrl(GMAIL_URL);
				return false;
			}
		});
    	
    	item = menu.add(R.string.menu_help);
    	item.setIcon(R.drawable.ic_menu_help);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				Intent myIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://www.yumusoft.com/" +
								"bettermail/help.html"));
				startActivity(myIntent);
				
				return false;
			}
		});
    	
    	item = menu.add(R.string.menu_settings);
    	item.setIcon(R.drawable.ic_menu_preferences);
    	item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				Intent prefsIntent = new Intent();
				prefsIntent.setClass(BetterMailActivity.this, 
						BetterMailPreferences.class);
				startActivity(prefsIntent);
				return false;
			}
		});
    	
    	return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
    }
        
    public String getUrl() {
    	return _url;
    }
    
    public void setUrl(String value) {
    	_url = value;
    }
    
    public void setIsLoading(boolean value) {
    	_isLoading = value;
	}
    
    public boolean getIsLoading() {
    	return _isLoading;
    }
    
    public void onEulaAgreedTo() {
    	// If EULA is agreed to then this is the first run.
    	showDialog(FIRST_RUN_DIALOG_ID);
		saveAccountInPreferences();
	}

	private void saveAccountInPreferences() {
		String mainAccount = getAccountUsingAccountManager();
		
		if (mainAccount == null) {
			GoogleLoginServiceHelper.getAccount(this, LOGIN_CODE, false);
		} else {
			SharedPreferences pref = 
				PreferenceManager.getDefaultSharedPreferences(this);
					
			SharedPreferences.Editor editor = pref.edit();
					
			editor.putString(getString(R.string.pref_email), 
					mainAccount);
			editor.commit();
		}
	}
    
    private String getAccountUsingAccountManager() {
    	String res = null;
    	try {
    		Class<?> accountManagerClass = 
    			Class.forName("android.accounts.AccountManager");
    		Class<?> accountClass = Class.forName("android.accounts.Account");
    		Method getMethod = accountManagerClass.getMethod("get",
    				new Class[] {
    					Context.class
    				}
    		);
    		
    		Method getAccountsByType = 
    			accountManagerClass.getMethod("getAccountsByType", new Class[] {
    				String.class
    			}
    		);
    		
    		Object manager = getMethod.invoke(null, this);
    		Object[] accounts = (Object[])getAccountsByType.invoke(manager, 
    				new Object[] {
    					"com.google"
    				}
    		);
    		
    		if (accounts.length >= 1) {
    			res = (String)accountClass.getField("name").get(accounts[0]);
    			if (DEBUG) Log.d("BetterMail", "Found Acount using Account " +
    					"Manager: " + res);
    		}
    	} catch (Exception ex) {
    		if (DEBUG) Log.d("BetterMail", "Unable to find account using " +
    				"Account Manager");
    	}
    	
    	return res;
	}
    
    private class NonRedirectingWebViewClient extends WebViewClient {
    	/*
    	 * Called Before loading a URL.  Returning true means the current 
    	 * WebView should handle.  A false return value means it should 
    	 * use the normal Browser or other broadcast receivers.
    	 */
    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		
    		// BUGFIX: There was at least one error where url.substring failed
    		//         with an IndexOutOfRangeException.  I'm guessing it 
    		//         must've been a link to void or something.  Probably want
    		//         to return true.
    		if (url == null || url.length() < 10) {
    			return true;
    		}
    		
    		int domainIndex = url.indexOf('/', 9); // https://_ => 9th char
    		String domain = url.substring(0, domainIndex);

    		if (domain.contains("mail.google.com") || 
    				url.startsWith("https://www.google.com/accounts")) {
    			setIsLoading(true);
    			
    			if (DEBUG)
    				Log.d("BetterMail", url);
    			
    			// Download attachments.
    			if (url.contains("?view=att")) {
    				setUrl(url);
    				showDialog(DOWNLOAD_DIALOG_ID);
    				return true;
    			}
    			
    			view.loadUrl(url);
    			return true;
    		} else {
    			// Open external links in Browser activity.
    			if (DEBUG)
    				Log.d("BeterMail", url);
    			
    			setUrl(url);
    			showDialog(EXTERNAL_LINK_DIALOG_ID);
    			
    			return true;
    		}
    	}
    	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		setIsLoading(false);
    		CookieSyncManager.getInstance().sync();
    	}
    	
    	@Override
    	public void onReceivedError (WebView view, int errorCode, 
    			String description, String failingUrl) {
    		view.post(new Runnable() {
				public void run() {
					showDialog(ERROR_DIALOG_ID);
				}
    		});
    	}
    }
    
    private class ProgressObervingChromeClient extends WebChromeClient {
    	@Override
    	public void onProgressChanged(WebView view, int progress) {
    		setTitle(R.string.loading);
    		setProgress(progress * 100); // 0~10000
    		
    		if (progress == 100) {
    			setTitle(R.string.app_name);
    		}
    	}
    }
}