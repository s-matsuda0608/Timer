package jp.matsuda.Timer;

import java.util.ArrayList;

import com.google.gson.Gson;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TimerActivity extends AppCompatActivity {

	//Viewのフィールド
	private EditText hourText;
	private EditText minText;
	private EditText secText;
	private Button startAndStopButton;
	private Button resetButton;

	//ステート
	private int status;
	private final int ST_DISABLE = 0;
	private final int ST_ENABLE = 1;
	private final int ST_PAUSE =2;

	//アプリの設定関連
	SharedPreferences sharedPref;
	private int alarmId;
	private int dataSize;
	private boolean noticeAlarmEnable;
	private ArrayList<NoticeData> noticeList = new ArrayList<NoticeData>();

	private MediaPlayer sound = new MediaPlayer();
	private MediaPlayer noticeSound = new MediaPlayer();

	private boolean ringingNoticeSound = false;

	private MyCountDownTimer cdt;
	private final int interval = 250;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		getPref();
		findView();

		setListener();
		status = ST_DISABLE;

		setSound();
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
					String hour = hourText.getText().toString();
					String min =  minText.getText().toString();
					String sec = secText.getText().toString();
					int ms = Util.timeToMilliSec(hour, min, sec);

					if(Util.hasTimeError(hour, min, sec)){
						Toast.makeText(getApplicationContext(), Const.TIME_ERROR, Toast.LENGTH_SHORT).show();
						timerInitialize();

					}else{
						startAndStopButton.setText(R.string.stopButton);
						GradientDrawable gd = (GradientDrawable) startAndStopButton.getBackground();
						gd.setColor(0xffff0000);
						status = ST_ENABLE;
						setEnabled(false);
						cdt = new MyCountDownTimer(ms,interval);

						cdt.start();
					}

				}else if(status==ST_ENABLE){
					cdt.cancel();
					startAndStopButton.setText(R.string.startButton);
					GradientDrawable gd = (GradientDrawable) startAndStopButton.getBackground();
					gd.setColor(0xff228b22);
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
	}

	private void setOutLineOnTimerView(int colorCode) {
		((LinearLayout)hourText.getParent()).setBackgroundColor(colorCode);
		((LinearLayout)minText.getParent()).setBackgroundColor(colorCode);
		((LinearLayout)secText.getParent()).setBackgroundColor(colorCode);
	}

	private void timerInitialize() {

		startAndStopButton.setText(R.string.startButton);
		GradientDrawable gd = (GradientDrawable) startAndStopButton.getBackground();
		gd.setColor(0xff228b22);

		setEnabled(true);
		setOutLineOnTimerView(Color.BLACK);
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
	}

	private void findView() {
		hourText = (EditText) findViewById(R.id.hourEdit);
		minText = (EditText) findViewById(R.id.minEdit);
		secText = (EditText) findViewById(R.id.secEdit);
		startAndStopButton = (Button)findViewById(R.id.startAndStop);
		resetButton = (Button)findViewById(R.id.resetButton);
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
		//オプションメニューを開くたびに呼ばれる
		menu.findItem(R.id.musicSetting).setEnabled(status == ST_DISABLE);
		menu.findItem(R.id.noticeSetting).setEnabled(status == ST_DISABLE);

		return super.onPrepareOptionsMenu(menu);
	}

	public void ringAlarm(){
		// TODO 予告アラームのメソッド(新しく作る？)

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

	public void ringNoticeAlarm(int i){

		ringingNoticeSound = true;

		noticeSound = MediaPlayer.create(this, Const.SOUNDS_PATH[noticeList.get(i).getNoticeAlarmId()]);
		noticeSound.setLooping(true);
		noticeSound.seekTo(0);

		noticeSound.start();

		final int num = i;

		AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
		alertDlg.setTitle("予告");
		alertDlg.setMessage(Const.END_MSG);
		alertDlg.setPositiveButton("OK", null);

		alertDlg.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				noticeSound.stop();
				ringingNoticeSound = false;
				LinearLayout noticeView = (LinearLayout) findViewById(R.id.noticeView);
				TableRow tr = (TableRow)((TableLayout) noticeView.getChildAt(num)).getChildAt(0);
				((TextView)tr.getChildAt(0)).setTextColor(Color.GRAY);
				((TextView)tr.getChildAt(1)).setTextColor(Color.GRAY);
			}
		});

		// 表示
		alertDlg.create().show();
	}

	public void setSound(){
		sound = MediaPlayer.create(this, Const.SOUNDS_PATH[alarmId]);// 音楽ファイルを読み込み
		sound.setLooping(true); // ループ設定
		sound.seekTo(0); // 再生位置を0ミリ秒に指定
	}

	public void onUserLeaveHint(){
		sound.stop();
		noticeSound.stop();
		ringingNoticeSound = false;
	}

	public class MyCountDownTimer extends CountDownTimer{

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			// カウントダウン完了後に呼ばれる
			ringAlarm();
			timerInitialize();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// インターバル(countDownInterval)毎に呼ばれる

			String hour = Util.timeFormat((int)millisUntilFinished/1000/60/60);
			String min = Util.timeFormat((int)millisUntilFinished/1000/60);
			String sec = Util.timeFormat((int)millisUntilFinished/1000%60);

			hourText.setText(hour);
			minText.setText(min);
			secText.setText(sec);
			System.out.println("count : "+hour+":"+min+":"+sec);

			if( ! ringingNoticeSound){
				for(int i=0; i<noticeList.size(); i++){
					NoticeData nd = noticeList.get(i);
					System.out.println("notice : "+nd.getHour()+":"+nd.getMin()+":"+nd.getSec());
					if(hour.equals(nd.getHour()) && min.equals(nd.getMin()) && sec.equals(nd.getSec())){
						ringNoticeAlarm(i);
						break;
					}
				}
			}

		}
	}

}
