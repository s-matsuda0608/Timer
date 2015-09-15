package jp.matsuda.Timer;

import java.util.ArrayList;

import com.google.gson.Gson;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NoticeSettingActivity extends AppCompatActivity{

	private CheckBox checkBox;
	private Button addButton;
	private Button doneButton;
	private Button cancelButton;

	private SharedPreferences sharedPref;
	SharedPreferences.Editor editor;

	private int dataSize;
	private ArrayList<NoticeData> noticeList = new ArrayList<NoticeData>();
	private boolean noticeAlarmEnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice_setting);

		findView();
		getPref();
		createView();

		setListener();
	}

	private void createView() {

		checkBox.setChecked(noticeAlarmEnable);

		for(int i=0;i<noticeList.size();i++){
			addRowView(noticeList.get(i));
		}

	}

	//アプリの設定を読み込む
	private void getPref() {

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPref.edit();

		//noticeList読み込み
		Gson gson = new Gson();
		dataSize = sharedPref.getInt(Const.DATA_SIZE, 0);
		int ds = dataSize;
		for(int i=0;i<ds;i++){
			noticeList.add(gson.fromJson(sharedPref.getString(Const.NOTICE_DATA+String.valueOf(i),""),NoticeData.class));
		}

		//そのほか読み込み
		noticeAlarmEnable = sharedPref.getBoolean(Const.NOTICE_ALARM_ENABLE, false);

		//読み込んだ設定は初期化しておく
		editor.clear();

	}


	private void findView() {
		checkBox = (CheckBox)findViewById(R.id.checkBox);
		//noticeSettingTable = (ViewGroup)findViewById(R.id.noticeContentTable);
		addButton = (Button)findViewById(R.id.addButton);
		doneButton = (Button)findViewById(R.id.doneButton);
		cancelButton = (Button)findViewById(R.id.cancelButton);
	}

	private void saveSetting() {

		Gson gson = new Gson();

		//noticeList内のNoticeDataを1つずつJsonに変換してsharedPrefに入れる
		try {
			for(int i=0;i<noticeList.size();i++){
				editor.putString(Const.NOTICE_DATA + String.valueOf(i),gson.toJson(noticeList.get(i)));
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		editor.putInt(Const.DATA_SIZE, noticeList.size());
		editor.putBoolean(Const.NOTICE_ALARM_ENABLE, checkBox.isChecked());

		//変更を保存
		editor.commit();
	}

	private void setListener() {

		checkBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if(checkBox.isChecked()){
					checkBox.setChecked(true);

				}else{
					checkBox.setChecked(false);
				}
			}
		});

		doneButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				saveSetting();
				finish();
			}

		});

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NoticeData nd = new NoticeData();
				nd.setHour("00");
				nd.setMin("05");
				nd.setSec("00");
				nd.setNoticeAlarmId(0);
				nd.setNoticeAlarmName(Const.SOUNDS_ARRAY[0].toString());

				//内部データとしてリストに追加
				noticeList.add(nd);
				addRowView(nd);
			}

		});

	}

	private void addRowView(NoticeData nd) {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.xml.notice_setting_container, null);

		final LinearLayout noticeSettingLinear = (LinearLayout) findViewById(R.id.noticeSettingLinear);
		final TextView timeView = (TextView)  rowView.findViewById(R.id.timeValue);
		final TextView soundView = (TextView) rowView.findViewById(R.id.soundValue);
		final Button delBtn = (Button) rowView.findViewById(R.id.delButton);

		//ビュー要素書き換え
		timeView.setText(nd.getHour()+":"+nd.getMin()+":"+nd.getSec());
		soundView.setText(nd.getNoticeAlarmName());

		//ボタンのIDを(子の数 - 1)に設定する。(このようにすると「0,1,2, ...」と振ることができる)
		delBtn.setId(noticeSettingLinear.getChildCount() - 1);

		//「予告アラーム追加」ボタンの前に追加
		noticeSettingLinear.addView(rowView,noticeSettingLinear.getChildCount() - 1);
	}

	public void deleteRow(View v){
		//何行目の「削除」ボタンが押されたか取得
		int rowNumber = v.getId();
		System.out.println("delete："+rowNumber);
		//「削除」ボタンの親(ボタンが配置されている行)の親(その行のテーブル)の親(テーブルと罫線のまとまり)を取得
		LinearLayout container = (LinearLayout) v.getParent().getParent().getParent();
		//コンテナが配置されているLinearLayout取得
		LinearLayout containersParent = (LinearLayout) container.getParent();
		//ビューから削除
		containersParent.removeView(container);

		// 内部データと整合とる(noticeListが小さくなった分ボタンのIDをずらす)
		if(noticeList.size()!=1){
			for(int i=rowNumber+1;i<noticeList.size();i++){
				Button delBtn = (Button) containersParent.findViewById(i);
				delBtn.setId(i - 1);
			}
		}
		noticeList.remove(rowNumber);
	}

	public void setNoticeSound(View v){

		final TextView soundView = (TextView) v.findViewById(R.id.soundValue);

		//何番目の行なのか取得(その行の削除ボタンのIDを取得している)
		final int number = ((ViewGroup) v.getParent().getParent()).getChildAt(1).getId();

		final AlertDialog.Builder listDlg = new AlertDialog.Builder(this);

		listDlg.setTitle("アラーム音選択");

		listDlg.setItems(

				Const.SOUNDS_ARRAY,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						System.out.println("which : "+which);

						final int noticeAlarmId = which;
						final String noticeAlarmName =Const.SOUNDS_ARRAY[which].toString();

						noticeList.get(number).setNoticeAlarmId(noticeAlarmId);
						noticeList.get(number).setNoticeAlarmName(noticeAlarmName);
						soundView.setText(noticeAlarmName);
					}
				});// listDlg.setItems 閉じ
		listDlg.create().show();
	}

	public void setNoticeTime(View v){//引数はTableRow

		final TextView timeView = (TextView) v.findViewById(R.id.timeValue);

		//何番目の行なのか取得(その行の削除ボタンのIDを取得している)
		final int number = ((ViewGroup) v.getParent().getParent()).getChildAt(1).getId();

		//ビュー設定
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.xml.notice_time_setting, (ViewGroup)findViewById(R.id.layout_root));

		//ダイアログ生成
		AlertDialog .Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("予告アラーム時間設定");
		builder.setView(layout);

		builder.setPositiveButton(Const.OK, new OnClickListener () {
			public void onClick(DialogInterface dialog, int which) {
				//入力された値を取得
				EditText hour = (EditText) layout.findViewById(R.id.hour);
				EditText min = (EditText) layout.findViewById(R.id.minute);
				EditText sec = (EditText) layout.findViewById(R.id.second);

				//Stringに直す
				String hourStr = hour.getText().toString();
				String minStr = min.getText().toString();
				String secStr = sec.getText().toString();

				//intに直す
				int hourInt = 0;
				int minInt = 0;
				int secInt = 0;
				try {
					hourInt = Integer.parseInt(hourStr);
					minInt = Integer.parseInt(minStr);
					secInt = Integer.parseInt(secStr);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (minInt<60 && secInt<60 && (hourInt+minInt+secInt)!=0) {
					//リストに保存
					noticeList.get(number).setHour(hourStr);
					noticeList.get(number).setMin(minStr);
					noticeList.get(number).setSec(secStr);
					//画面表示用
					String timeStr = Util.makeTimeView(hourInt, minInt, secInt);
					//画面に表示
					timeView.setText(timeStr);

				}else {
					Toast.makeText(getApplicationContext(),Const.TIME_ERROR2, Toast.LENGTH_SHORT).show();
				}

			}
		});

		builder.setNegativeButton(Const.CANCEL, new OnClickListener () {
			public void onClick(DialogInterface dialog, int which) {
				//何もしない
			}
		});

		builder.create().show();
	}

}
