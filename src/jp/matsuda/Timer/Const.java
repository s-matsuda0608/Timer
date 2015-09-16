package jp.matsuda.Timer;

public class Const {

	//インスタンス生成できないようにする
	private Const(){}

	//sharedPrefのキー
	final static String ALARM_ID = "alarmId";
	final static String ALARM_NAME = "alarmName";
	final static String NOTICE_DATA = "noticeData";
	final static String DATA_SIZE = "dataSize";
	final static String NOTICE_ALARM_ENABLE = "noticeAlarmEnable";

	//パッケージ名・アプリ名
	final static String PACKAGE_NAME = "jp.matsuda.Timer";
	final static String NOTICE_SETTING_ACTIVITY = "jp.matsuda.Timer.NoticeSettingActivity";
	final static String ALARM_SETTING_ACTIVITY = "jp.matsuda.Timer.AlarmSettingActivity";

	//アラーム音選択用
	final static CharSequence[] SOUNDS_ARRAY = {"sound 0", "sound 1", "sound 2","sound 3","sound 4"};
	final static int[] SOUNDS_PATH = {R.raw.sound0,R.raw.sound1,R.raw.sound2,R.raw.sound3,R.raw.sound4};

	//各種メッセージ
	final static String END_MSG = "時間になりました\n「OK」を押してアラームを止めてください";
	final static String NOTICE_MSG = "予告アラームです。\n「OK」を押してアラームを止めてください";
	final static String TIME_ERROR= "入力された時間が不正です。";

	//ダイアログのボタン
	final static String OK = "OK";
	final static String CANCEL = "キャンセル";
}
