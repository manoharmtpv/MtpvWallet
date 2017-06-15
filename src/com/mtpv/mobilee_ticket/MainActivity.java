package com.mtpv.mobilee_ticket;

import com.crashlytics.android.Crashlytics;


import io.fabric.sdk.android.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.fabric.sdk.android.BuildConfig;
import mother.com.test.PidSecEncrypt;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mtpv.mobilee_ticket_services.DBHelper;
import com.mtpv.mobilee_ticket_services.ServiceHelper;

@SuppressLint({ "WorldReadableFiles", "NewApi", "SimpleDateFormat" })
public class MainActivity extends Activity implements OnClickListener, LocationListener {

	final int SPLASH_DIALOG = 0;
	EditText et_pid;
	EditText et_pid_pwd;
	Button btn_cancel;
	Button btn_submit;
	TextView tv_ip_settings;

	public static String Pid_code = "", Pid_pswrd = "", Pid_Name = "", Ps_code = "", Ps_Name = "", CADRE_CODE = "",
			CADRE_NAME = "", UNIT_CODE = "", UNIT_NAME = "", SECURITY_CD = "";

	DBHelper db;
	Cursor c, cursor_officerdata, c_dup_res, c_whlr_details;

	final int PROGRESS_DIALOG = 1;

	public static String[] arr_logindetails;

	LocationManager m_locationlistner;
	android.location.Location location;

	public static double latitude = 0.0;
	public static double longitude = 0.0;
	String provider = "";
	public static String IMEI = "";
	public static String user_id = "";
	public static String user_pwd = "";

	public static String e_user_id = null, sim_No = null, mobile_no = null;
	public static String e_user_tmp = "", e_imei = null, e_latitude = null, e_longitude = "";
	/* GPS VALUES */
	// flag for GPS status
	boolean isGPSEnabled = false;
	// flag for network statussetText

	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	SharedPreferences preference;
	SharedPreferences.Editor editor;
	String service_type = "";
	String services_url = "";
	String ftps_url = "";
	public static String URL = "";

	TextView textView2;

	/* STATIC URL WHICH IS TO BE FIXED */
	// http://192.168.11.4:8080/eTicketMobileHyd----- LIVE
	// http://192.168.11.10:8080/eTicketMobileHyd---- TEST
	private String url_to_fix = "/services/MobileEticketServiceImpl?wsdl";
	@SuppressWarnings("unused")
	private String test_service_url = "http://192.168.11.55:8080/eTicketMobileHyd";
	@SuppressWarnings("unused")
	private String live_service_url = "http://192.168.11.4/eTicketMobileHyd";

	/* STATIC URL WHICH IS TO BE FIXED */
	public static String FTP_HOST = "";

	ArrayList<String> ar_test = new ArrayList<String>();
	ArrayList<String> arr_for_logindetails;

	public static String appVersion = null;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(BuildConfig.DEBUG) {
			Fabric.with(this, new Crashlytics());
		}


		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.login);
		
		// splash start
		showDialog(SPLASH_DIALOG);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				removeDialog(SPLASH_DIALOG);
			}
		}, 2500);
		// splash end

		LoadUIcomponents();
		textView2 = (TextView) findViewById(R.id.textView2);
		appVersion = textView2.getText().toString().trim();

		if (android.os.Build.VERSION.SDK_INT > 11) {
			StrictMode.ThreadPolicy polocy = new StrictMode.ThreadPolicy.Builder().build();
			StrictMode.setThreadPolicy(polocy);
		}

		preference = getSharedPreferences("preferences", MODE_WORLD_READABLE);
		service_type = preference.getString("servicetype", "live");
		services_url = preference.getString("serviceurl", "url1");
		ftps_url = preference.getString("ftpurl", "url2");

		/************** Adding Shortcut of Application **************/
		SharedPreferences prefs = getSharedPreferences("ShortCutPrefs", MODE_PRIVATE);
		if (!prefs.getBoolean("isFirstTime", false)) {
			addShortcut();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("isFirstTime", true);
			editor.commit();
		}
		/************** Adding Shortcut of Application **************/

		if ((!services_url.equals("url1") && (service_type.equals("live")))) {
			URL = "" + services_url + "" + url_to_fix;
		} else if ((!services_url.equals("url1") && (service_type.equals("test")))) {
			URL = "" + services_url + "" + url_to_fix;
		}
		getLocation();
	}

	/************** Adding Shortcut of Application **************/
	private void addShortcut() {
		Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
		shortcutIntent.setAction(Intent.ACTION_MAIN);
		shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
		shortcutIntent.addFlags(flags);

		Intent addIntent = new Intent();
		addIntent.putExtra("duplicate", false);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.logo));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		getApplicationContext().sendBroadcast(addIntent);
	}
	/************** Adding Shortcut of Application **************/

	/*** START METHOD TO LOAD UI COMPONENTS ****/
	private void LoadUIcomponents() {
		// TODO Auto-generated method stub

		et_pid = (EditText) findViewById(R.id.edtpidcode_login_xml);
		et_pid_pwd = (EditText) findViewById(R.id.edtpidpwd_login_xml);
		btn_cancel = (Button) findViewById(R.id.btncancel_login_xml);
		btn_submit = (Button) findViewById(R.id.btnsubmit_login_xml);
		tv_ip_settings = (TextView) findViewById(R.id.tv_ipsettings);

	//	et_pid.setText("23001004");
		//et_pid_pwd.setText("WdSt24Pr");

		btn_cancel.setOnClickListener(this);
		btn_submit.setOnClickListener(this);
		tv_ip_settings.setOnClickListener(this);

		db = new DBHelper(getApplicationContext());
	}

	/*** END METHOD TO LOAD UI COMPONENTS ****/

	@SuppressWarnings("unused")
	public void getLocation() {

		try {
			m_locationlistner = (LocationManager) this.getSystemService(LOCATION_SERVICE);
			// getting GPS status
			isGPSEnabled = m_locationlistner.isProviderEnabled(LocationManager.GPS_PROVIDER);
			// getting network status
			isNetworkEnabled = m_locationlistner.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
				latitude = 0.0;
				longitude = 0.0;
			} else {
				this.canGetLocation = true;
				// String current_time;
				String current_time = null;
				String current_date = null;
				// First get location from Network Provider
				if (isNetworkEnabled) {
					m_locationlistner.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");

					if (m_locationlistner != null) {
						location = m_locationlistner.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();

							long time = location.getTime();
							Date date = new Date(time);

							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String gps_Date = sdf.format(date);
							System.out.println(gps_Date);
							if (gps_Date == null) {
								startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
							}
							Log.i("location_Date", "" + gps_Date);
						} else {
							latitude = 0.0;
							longitude = 0.0;
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						m_locationlistner.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Log.d("GPS Enabled", "GPS Enabled");

						if (m_locationlistner != null) {
							location = m_locationlistner.getLastKnownLocation(LocationManager.GPS_PROVIDER);

							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();

								long time = location.getTime();
								Date date = new Date(time);

								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String gps_Date = sdf.format(date);
								System.out.println(gps_Date);
								if (gps_Date == null) {
									startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS),
											0);
								}
								Log.i("gps_Date", "" + gps_Date);
							} else {
								latitude = 0.0;
								longitude = 0.0;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			showToast("Please check the GPS Location !");
		}

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		IMEI = getDeviceID(telephonyManager);
		if (telephonyManager.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
			sim_No = "" + telephonyManager.getSimSerialNumber();
		} else {
			sim_No = "";
		}
	}

	@Override
	public void onLocationChanged(Location location) {

		if (location != null) {
			latitude = (float) location.getLatitude();
			longitude = (float) location.getLongitude();
		} else {
			latitude = 0.0;
			longitude = 0.0;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}

	String getDeviceID(TelephonyManager phonyManager) {

		String id = phonyManager.getDeviceId();
		if (id == null) {
			id = "not available";
		}

		int phoneType = phonyManager.getPhoneType();

		switch (phoneType) {

		case TelephonyManager.PHONE_TYPE_NONE:
			return id;

		case TelephonyManager.PHONE_TYPE_GSM:
			return id;

		case TelephonyManager.PHONE_TYPE_CDMA:
			return id;

		default:
			return "UNKNOWN:ID=" + id;

		}
	}

	@SuppressWarnings("unused")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.btncancel_login_xml:
			clearInputFields();
			break;

		case R.id.btnsubmit_login_xml:
			String pidcode = et_pid.getText().toString();
			String password = et_pid_pwd.getText().toString();

			// initializing null values
			String PS_CODE = null;
			String PS_NAME = null;
			String CADRE_CODE = null;
			String CADRE_NAME = null;
			String UNIT_CODE = null;
			String UNIT_NAME = null;

			DBHelper helper = new DBHelper(getApplicationContext());
			ContentValues values = new ContentValues();
			values.put("PIDCODE", pidcode);
			values.put("PASSWORD", password);
			values.put("PIDNAME", pidcode);
			values.put("PS_CODE", PS_CODE);
			values.put("PS_NAME", PS_NAME);
			values.put("CADRE_CODE", CADRE_CODE);
			values.put("CADRE_NAME", CADRE_NAME);
			values.put("UNIT_CODE", UNIT_CODE);
			values.put("UNIT_NAME", UNIT_NAME);

			SQLiteDatabase db = openOrCreateDatabase(DBHelper.DATABASE_NAME, MODE_PRIVATE, null);
			db.execSQL(DBHelper.CREATE_USER_TABLE);
			db.execSQL("delete from " + DBHelper.USER_TABLE);
			db.insert(DBHelper.USER_TABLE, null, values); // Inserting Row
			System.out.println("*********************OFFICER TABLE Insertion Successfully **********************");

			if (et_pid.getText().toString().trim().equals("")) {
				et_pid.setError(Html.fromHtml("<font color='black'>Enter PID</font>"));

			} else if (et_pid_pwd.getText().toString().trim().equals("")) {
				et_pid_pwd.setError(Html.fromHtml("<font color='black'>Enter password</font>"));

			} else {

				if ((!services_url.equals("url1")) && (!ftps_url.equals("url2"))) {

					if (isOnline()) {
						user_id = "" + et_pid.getText().toString().trim();
						user_pwd = "" + et_pid_pwd.getText().toString().trim();

						getLocation();

						try {
							e_user_id = PidSecEncrypt.encryptmd5(user_id);
							e_user_tmp = PidSecEncrypt.encryptmd5(user_pwd);

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else {
						showToast("Please check your network connection !");
					}

					LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
					if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						new Async_task_login().execute();

					} else {
						// showGPSDisabledAlertToUser();
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
						alertDialogBuilder.setMessage("GPS is Disabled in your Device \nPlease Enable LOCATION ?")
								.setCancelable(false)
								.setPositiveButton("Goto Settings", new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										Intent callGPSSettingIntent = new Intent(
												android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
										startActivity(callGPSSettingIntent);
									}
								});
						alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						AlertDialog alert = alertDialogBuilder.create();
						alert.show();
					}
				} else {
					showToast("Set IP Settings!");
				}
			}
			break;

		case R.id.tv_ipsettings:
			startActivity(new Intent(MainActivity.this, IPSettings.class));
			break;

		default:
			break;
		}
	}

	protected void showGPSDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage("    GPS is Disabled in your Device \n            Please Enable GPS?")
				.setCancelable(false).setPositiveButton("Goto Settings", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent callGPSSettingIntent = new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(callGPSSettingIntent);
					}
				});
		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	/* TO CHECK ITS STATE ONLINE OR OFFLINE */
	public Boolean isOnline() {
		ConnectivityManager conManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nwInfo = conManager.getActiveNetworkInfo();
		// return nwInfo != null;
		if (nwInfo != null && nwInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public Boolean dataConnection() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return mobileInfo != null;
	}

	private void clearInputFields() {
		// TODO Auto-generated method stub
		et_pid.setText("");
		et_pid_pwd.setText("");
	}

	public class Async_task_login extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String[] version_split = appVersion.split("\\-");
			ServiceHelper.login("" + user_id, "" + e_user_tmp, "" + IMEI, "" + sim_No, "" + latitude, "" + longitude,
					"" + version_split[1]);
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showDialog(PROGRESS_DIALOG);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			removeDialog(PROGRESS_DIALOG);

			/*
			 * 1-invalid login id 2-invalid password 3-un authorized device in
			 * else condition you will get correct response
			 */
			MainActivity.arr_logindetails = ServiceHelper.Opdata_Chalana.split(":");

			if (ServiceHelper.Opdata_Chalana != null) {
				if (ServiceHelper.Opdata_Chalana.toString().trim().equals("1")) {
					showToast("Invalid Login ID");
				} else if (ServiceHelper.Opdata_Chalana.toString().trim().equals("2")) {
					showToast("Invalid Password");
				} else if (ServiceHelper.Opdata_Chalana.toString().trim().equals("3")) {
					showToast("Unautherized Device");
					
				} else if (ServiceHelper.Opdata_Chalana.toString().trim().equals("4")) {
					showToast("Error, Please Contact E Challan Team at 040-27852721");
				} else if (ServiceHelper.Opdata_Chalana.toString().trim().equals("5")) {
					showToast(
							"You have Exceeded Number of \n Attempts with Wrong Password,\n Please Contact E Challan Team at 040-27852721 ");
				} else if (ServiceHelper.Opdata_Chalana.toString().trim().equals("0")) {
					showToast("Please Contact E Challan Team at 040-27852721");
				} else {
					for (int i = 0; i < MainActivity.arr_logindetails.length; i++) {
					}
					// officerName|pscode|psname|cadre_cd|cadre
					// TESTING PURPOSE|2300|TRAFFIC CELL|00|DEVP
					SharedPreferences sharedPreferences = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					SharedPreferences.Editor editors = sharedPreferences.edit();
					// 23001004|TESTING PURPOSE|2300|TRAFFIC
					// CELL|00|DEVP|7893816681|Y
					String pidCode = "" + arr_logindetails[0];
					String pidName = "" + arr_logindetails[1];
					String psCd = "" + arr_logindetails[2];
					String psName = "" + arr_logindetails[3];
					String cadre_code = "" + arr_logindetails[4];
					String cadre_name = "" + arr_logindetails[5];
					String pass_word = "" + user_pwd;

					String off_phone_no = "" + arr_logindetails[6];
					String current_version = "" + arr_logindetails[7];

					String rta_data_flg = "" + arr_logindetails[8];
					String dl_data_flg = "" + arr_logindetails[9];
					String aadhaar_data_flg = "" + arr_logindetails[10];
					String otp_no_flg = "" + arr_logindetails[11];
					String cashless_flg = "" + arr_logindetails[12];
					String mobileNo_flg = "" + arr_logindetails[13];

					/*
					 * "PID_CD" "TR_EMP_NAME" "PS_CODE" "PS_NAME" "CADRE_CD"
					 * "CADRE" "CONTACT_NO" "Version" "RTA_DATA_FLAG"
					 * "DL_DATA_FLAG" "AADHAAR_DATA_FLAG" "OTP_NO_FLAG"
					 * "CASHLESS_FLAG" "MOBILE_NO_FLAG"
					 */

					// PUTTING IN SHARED PREFERENCES
					editors.putString("PID_CODE", pidCode);
					editors.putString("PID_NAME", pidName);
					editors.putString("PS_CODE", psCd);
					editors.putString("PS_NAME", psName);
					editors.putString("CADRE_CODE", cadre_code);
					editors.putString("CADRE_NAME", cadre_name);
					editors.putString("PASS_WORD", pass_word);
					editors.putString("OFF_PHONE_NO", off_phone_no);
					editors.putString("CURRENT_VERSION", current_version);

					editors.putString("RTA_DATA_FLAG", rta_data_flg);
					editors.putString("DL_DATA_FLAG", dl_data_flg);
					editors.putString("AADHAAR_DATA_FLAG", aadhaar_data_flg);
					editors.putString("OTP_NO_FLAG", otp_no_flg);
					editors.putString("CASHLESS_FLAG", cashless_flg);
					editors.putString("MOBILE_NO_FLAG", mobileNo_flg);

					Log.i("PID_CODE :::", pidCode);
					Log.i("PID_NAME :::", pidName);
					Log.i("PS_CODE   :::", psCd);
					Log.i("PS_NAME   :::", psName);
					Log.i("CADRE_CODE   :::", cadre_code);
					Log.i("CADRE_NAME   :::", cadre_name);
					Log.i("PASS_WORD   :::", pass_word);
					Log.i("OFF_PHONE_NO   :::", off_phone_no);
					Log.i("CURRENT_VERSION   :::", current_version);
					Log.i("RTA_DATA_FLAG   :::", rta_data_flg);
					Log.i("DL_DATA_FLAG   :::", dl_data_flg);
					Log.i("AADHAAR_DATA_FLAG   :::", aadhaar_data_flg);
					Log.i("OTP_NO_FLAG   :::", otp_no_flg);
					Log.i("CASHLESS_FLAG   :::", cashless_flg);
					Log.i("MOBILE_NO_FLAG   :::", mobileNo_flg);
					editors.commit();

					/*
					 * if(cadre_code!=null && Integer.parseInt(cadre_code)<=13){
					 * startActivity(new Intent(getApplicationContext(),
					 * Dashboard.class)); } else { startActivity(new
					 * Intent(getApplicationContext(), Dashboard_PC.class)); }
					 */
					startActivity(new Intent(getApplicationContext(), Dashboard.class));
					startActivity(new Intent(getApplicationContext(), Dashboard.class));
					finish();
				}
			} else {
				showToast("Login Failed!");
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case SPLASH_DIALOG:
			Dialog dg_splash = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
			dg_splash.setCancelable(false);
			dg_splash.setContentView(R.layout.splash);
			return dg_splash;

		case PROGRESS_DIALOG:
			ProgressDialog pd = ProgressDialog.show(this, "", "", true);
			pd.setContentView(R.layout.custom_progress_dialog);
			pd.setCancelable(false);
			return pd;

		default:
			break;

		}
		return super.onCreateDialog(id);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		preference = getSharedPreferences("preferences", MODE_WORLD_READABLE);

		services_url = preference.getString("serviceurl", "url1");
		ftps_url = preference.getString("ftpurl", "url2");

		if (!services_url.equals("urls1")) {
			URL = "" + services_url + "" + url_to_fix;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		preference = getSharedPreferences("preferences", MODE_WORLD_READABLE);
		services_url = preference.getString("serviceurl", "url1");
		ftps_url = preference.getString("ftpurl", "url2");
	}

	private void showToast(String msg) {
		// TODO Auto-generated method stub
		Toast toast = Toast.makeText(getApplicationContext(), "" + msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		View toastView = toast.getView();

		ViewGroup group = (ViewGroup) toast.getView();
		TextView messageTextView = (TextView) group.getChildAt(0);
		messageTextView.setTextSize(24);

		toastView.setBackgroundResource(R.drawable.toast_background);
		toast.show();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		TextView title = new TextView(this);
		title.setText("Hyderabad E-Ticket");
		title.setBackgroundColor(Color.RED);
		title.setGravity(Gravity.CENTER);
		title.setTextColor(Color.WHITE);
		title.setTextSize(26);
		title.setTypeface(title.getTypeface(), Typeface.BOLD);
		title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dialog_logo, 0, R.drawable.dialog_logo, 0);
		title.setPadding(20, 0, 20, 0);
		title.setHeight(70);

		String otp_message = "\n Are you sure, You want to Leave Application...! \n";

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this,
				AlertDialog.THEME_HOLO_LIGHT);
		alertDialogBuilder.setCustomTitle(title);
		alertDialogBuilder.setIcon(R.drawable.dialog_logo);
		alertDialogBuilder.setMessage(otp_message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
			}
		});

		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

		alertDialog.getWindow().getAttributes();

		TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
		textView.setTextSize(28);
		textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
		textView.setGravity(Gravity.CENTER);

		Button btn1 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btn1.setTextSize(22);
		btn1.setTextColor(Color.WHITE);
		btn1.setTypeface(btn1.getTypeface(), Typeface.BOLD);
		btn1.setBackgroundColor(Color.RED);

		Button btn2 = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		btn2.setTextSize(22);
		btn2.setTextColor(Color.WHITE);
		btn2.setTypeface(btn2.getTypeface(), Typeface.BOLD);
		btn2.setBackgroundColor(Color.RED);
	}
}
