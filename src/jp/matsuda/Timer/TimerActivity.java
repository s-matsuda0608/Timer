package jp.matsuda.Timer;

import java.util.ArrayList;

import com.google.gson.Gson;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import jp.matsuda.Timer.R;

public class TimerActivity extends AppCompatActivity {

	//Viewのフィールド
	private EditText hourText;
	private EditText minText;
	private EditText secText;
	private Button startAndStopButton;
	private Button resetButton;
	private Button noticeSettingButton;

	//ステート
	private int status;
	private final int ST_DISABLE = 0;
	private final int ST_ENABLE = 1;
	private final int ST_PAUSE =2;

	//アプリの設定関連
	SharedPreferences sharedPref;
	private int alarmId;
	private String alarmName;
	private int dataSize;
	private boolean noticeAlarmEnable;
	private ArrayList<NoticeData> noticeList = new ArrayList<NoticeData>();

	private MediaPlayer sound = new MediaPlayer();

	private MyCountDownTimer cdt;
	private final int interval = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		getPref();
		findView();

		setListener();
		status = ST_DISABLE;

		setNoticeAlarmView();


	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//リロード処理
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();

		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	//アプリの設定を読み込む
	private void getPref() {

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		//noticeList読み込み
		Gson gson = new Gson();
		dataSize = sharedPref.getInt(Const.DATA_SIZE,0);
		int ds = dataSize;
		for(int i=0;i<ds;i++){
			noticeList.add(gson.fromJson(sharedPref.getString(Const.NOTICE_DATA+String.valueOf(i),""),NoticeData.class));
		}

		//そのほか読み込み
		alarmId = sharedPref.getInt(Const.ALARM_ID, 0);
		alarmName = sharedPref.getString(Const.ALARM_NAME, "");
		noticeAlarmEnable = sharedPref.getBoolean(Const.NOTICE_ALARM_ENABLE, false);
	}

	private void setNoticeAlarmView() {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout noticeViewLinear = (LinearLayout) findViewById(R.id.noticeView);

		if (noticeAlarmEnable && noticeList.size()!=0) {

			for (int i = 0; i < noticeList.size(); i++) {

				View tableLayout = inflater.inflate(R.xml.notice_view_container, null);

				TextView timeView = (TextView) tableLayout.findViewById(R.id.time);
				TextView soundView = (TextView) tableLayout.findViewById(R.id.sound);

				NoticeData nd = noticeList.get(i);

				//ビュー要素書き換え
				timeView.setText(Util.makeTimeView(nd.getHour(),nd.getMin(),nd.getSec()));
				soundView.setText(nd.getNoticeAlarmName());

				//配置
				noticeViewLinear.addView(tableLayout, noticeViewLinear.getChildCount());
			}
		}else {
			TextView view = new TextView(this);
			view.setText("なし");
			noticeViewLinear.addView(view, noticeViewLinear.getChildCount());
		}
	}

	private void setListener() {

		startAndStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){

				if(status==ST_DISABLE || status==ST_PAUSE){

					//時間取得＆ミリ秒に変換
					int ms = Util.timeToMilliSec(hourText.getText().toString(), minText.getText().toString(), secText.getText().toString());

					if(ms == 0){
						Toast.makeText(getApplicationContext(), Const.TIME_ERROR1, Toast.LENGTH_SHORT).show();
						timerInitialize();

					}else{
						startAndStopButton.setText(R.string.stopButton);
						status = ST_ENABLE;
						setEnabled(false);
						cdt = new MyCountDownTimer(ms,interval);

						cdt.start();
					}

				}else if(status==ST_ENABLE){
					cdt.cancel();
					startAndStopButton.setText(R.string.startButton);
					status = ST_PAUSE;
				}
			}

		});

		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				if(cdt != null){
					cdt.cancel();
				}
				timerInitialize();
			}
		});

		noticeSettingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v){
				Intent intent = new Intent();
				intent.setClassName(Const.PACKAGE_NAME, Const.NOTICE_SETTING_ACTIVITY);
				startActivity(intent);
			}
		});
	}



	private void timerInitialize() {
		startAndStopButton.setText(R.string.startButton);
		setEnabled(true);
		hourText.setText(R.string.initTime);
		minText.setText(R.string.initTime);
		secText.setText(R.string.initTime);
		status = ST_DISABLE;
	}

	private void setEnabled(boolean bool) {
		hourText.setFocusable(bool);
		hourText.setFocusableInTouchMode(bool);
		minText.setFocusable(bool);
		minText.setFocusableInTouchMode(bool);
		secText.setFocusable(bool);
		secText.setFocusableInTouchMode(bool);
		noticeSettingButton.setEnabled(bool);
	}

	private void findView() {
		hourText = (EditText) findViewById(R.id.hourEdit);
		minText = (EditText) findViewById(R.id.minEdit);
		secText = (EditText) findViewById(R.id.secEdit);
		startAndStopButton = (Button)findViewById(R.id.startAndStop);
		resetButton = (Button)findViewById(R.id.resetButton);
		noticeSettingButton = (Button)findViewById(R.id.noticeSettingButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (status == ST_DISABLE) {
			if (id == R.id.musicSetting) {

				Intent intent = new Intent();
				intent.setClassName(Const.PACKAGE_NAME, Const.ALARM_SETTING_ACTIVITY);
				startActivity(intent);

				return true;

			} else if (id == R.id.noticeSetting) {

				Intent intent = new Intent();
				intent.setClassName(Const.PACKAGE_NAME, Const.NOTICE_SETTING_ACTIVITY);
				startActivity(intent);

				return true;

			} else {
				return super.onOptionsItemSelected(item);
			}
		}else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.findItem(R.id.musicSetting).setEnabled(status == ST_DISABLE);
		menu.findItem(R.id.noticeSetting).setEnabled(status == ST_DISABLE);

		return super.onPrepareOptionsMenu(menu);
	}

	public void ringAlarm(){

		sound = MediaPlayer.create(this, R.raw.sound1);// 音楽ファイルを読み込み
		sound.setLooping(true); // ループ設定
		sound.seekTo(0); // 再生位置を0ミリ秒に指定

		sound.start();

		//カウントダウン終了を知らせるダイアログ(OKボタン押下でアラーム止まる)
		AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
		alertDlg.setTitle("終了");
		alertDlg.setMessage(Const.END_MSG);
		alertDlg.setPositiveButton("OK", null);

		alertDlg.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				sound.stop();
			}
		});

		// 表示
		alertDlg.create().show();
	}

	public void onUserLeaveHint(){
		sound.stop();
	}

	public class MyCountDownTimer extends CountDownTimer{

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			// カウントダウン完了後に呼ばれる
			ringAlarm();
			Toast.makeText(getApplicationContext(), "終了", Toast.LENGTH_SHORT).show();
			timerInitialize();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// インターバル(countDownInterval)毎に呼ばれる
			// TODO

			int hour = (int)Math.ceil(millisUntilFinished/1000)/60/60;
			int min = (int)Math.ceil(millisUntilFinished/1000)/60;
			int sec = (int)Math.ceil(millisUntilFinished/1000)%60;

			hourText.setText(Util.timeFormat(hour));
			minText.setText(Util.timeFormat(min));
			secText.setText(Util.timeFormat(sec));
		}
	}

}
