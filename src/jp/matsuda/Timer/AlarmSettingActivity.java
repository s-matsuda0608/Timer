package jp.matsuda.Timer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AlarmSettingActivity extends AppCompatActivity{

	private TextView soundView;
	private Button soundSetButton;
	private Button backButton;
	private SharedPreferences sharedPref;

	private AlertDialog.Builder listDlg;
	private AlertDialog.Builder confirmDialog;

	private int alarmId;
	private String alarmName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_setting);

		findView();

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		soundView.setText(sharedPref.getString(Const.ALARM_NAME, Const.SOUNDS_ARRAY[0].toString()));
		alarmId = sharedPref.getInt(Const.ALARM_ID, 0);

		setListener();
		createListDialog();

	}

	private void createListDialog() {

		listDlg = new AlertDialog.Builder(this);
		confirmDialog = new AlertDialog.Builder(this);

		listDlg.setTitle("アラーム音選択");

		listDlg.setItems(

				Const.SOUNDS_ARRAY,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						alarmId = which;
						alarmName = Const.SOUNDS_ARRAY[which].toString();

						confirmDialog.setTitle("確認");
						confirmDialog.setMessage(alarmName + " に変更してよろしいですか？");

						confirmDialog.setPositiveButton(
								Const.OK,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										SharedPreferences.Editor editor = sharedPref.edit();
										editor.putInt(Const.ALARM_ID, alarmId);
										editor.putString(Const.ALARM_NAME, alarmName);
										System.out.println(alarmId+" "+alarmName);
										editor.commit();

										soundView.setText(alarmName);
									}
								});

						confirmDialog.setNegativeButton(
								Const.CANCEL,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										//何もしない
									}
								});

						confirmDialog.create().show();

					}//Onclick 閉じ
				});// listDlg.setItems 閉じ
	}

	private void setListener() {

		soundSetButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Listダイアログ表示
				listDlg.create().show();
			}
		});

		backButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}


	private void findView() {
		soundView = (TextView)findViewById(R.id.soundView);
		soundSetButton = (Button)findViewById(R.id.soundSetButton);
		backButton = (Button)findViewById(R.id.alarmSettingBackButton);
	}

}
