package jp.matsuda.Timer;

public class Util {

	public static String timeFormat(String data){

		int d = 0;

		try {
			d = Integer.parseInt(data);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return timeFormat(d);
	}

	public static String timeFormat(int data){
		return String.format("%02d",data);
	}

	public static String makeTimeView(int hour, int min, int sec){
		return timeFormat(hour)+":"+timeFormat(min)+":"+timeFormat(sec);
	}

	public static String makeTimeView(String hour, String min, String sec){
		return timeFormat(hour)+":"+timeFormat(min)+":"+timeFormat(sec);
	}

	public static void sleep(int i) {
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			System.out.println("sleep失敗");
		}
	}

	public static int timeToMilliSec(int hour, int min, int sec){
		return hour*60*60*1000 + min*60*1000 + sec*1000;
	}

	public static int timeToMilliSec(String hour, String min, String sec){
		try {
			return timeToMilliSec(Integer.parseInt(hour), Integer.parseInt(min), Integer.parseInt(sec));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
