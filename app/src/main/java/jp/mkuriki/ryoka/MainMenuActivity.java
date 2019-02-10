package jp.mkuriki.ryoka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import net.nend.android.NendAdInterstitial;
import net.nend.android.NendAdInterstitial.NendAdInterstitialStatusCode;
import net.nend.android.NendAdInterstitial.OnCompletionListener;
import net.nend.android.NendAdView;
import net.nend.android.NendAdInterstitial.NendAdInterstitialClickType;


public class MainMenuActivity extends Activity {

	// 初めての起動かどうかを調べる値
	public static final int INIT = 1;
	public static final int ALREADY = 0;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d("E", "startttttttttttttttt");
        // インターステイシャル広告ロード
        NendAdInterstitial.loadAd(getApplicationContext(), "f59a565475df2c76e25b1e0e6d85f258bce4a295", 447232);
        setContentView(R.layout.mainmenu);
        
		// SharedPreferences の準備
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// 広告表示がオフかどうかチェック
		if((sp.getInt("password", 0) == 1) || (sp.getLong("adTime", 0) > System.currentTimeMillis())){
			// 広告を消す処理
			LinearLayout adLayout = (LinearLayout)this.findViewById(R.id.mainMenuAdLayout);
			NendAdView adView = (NendAdView)this.findViewById(R.id.nend2);
			adLayout.removeView((View)adView);
		}

		// twitter フォローボタンの処理
       ImageButton btnFollow = (ImageButton)this.findViewById(R.id.followButton);
        btnFollow.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// インテントのインスタンス生成
        		Intent intent = new Intent();
        		// インテントにアクション及びURLをセット
        		intent.setAction(Intent.ACTION_VIEW);
        		intent.setData(Uri.parse("https://twitter.com/intent/follow?screen_name=ryoka_bot&tw_p=followbutton"));
        		// ブラウザ起動
        		startActivity(intent);
        	}
        });
        
        // ボタンが押された時の挙動を設定
        // 一覧から選ぶボタン
        ImageButton btnChoseList = (ImageButton)this.findViewById(R.id.choseListButton);
        btnChoseList.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openSongListActivity();
        	}
        });

        // 曲を探すボタン
        ImageButton btnSearch = (ImageButton)this.findViewById(R.id.searchButton);
        btnSearch.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openSearchActivity();
        	}
        });

        // midi 付きリストボタン
        ImageButton btnMidi = (ImageButton)this.findViewById(R.id.midiListButton);
        btnMidi.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openMidiList();
        	}
        });
        
        // このアプリについてボタン
        Button btnAbout = (Button)this.findViewById(R.id.aboutButton);
        btnAbout.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openAboutActivity();
        	}
        });
        
        // カード引きボタン
        Button btnCard = (Button)this.findViewById(R.id.cardButton);
        btnCard.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openCardActivity();
        	}
        });

        // 広告消去ボタン
        Button btnAd = (Button)this.findViewById(R.id.interstitalBtn);
        btnAd.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		adDelete();
        	}
        });
	}
	
	// 初回起動時のみの動作
	// 更新履歴ダイアログボックスを表示する
	@Override
	public void onResume(){
		super.onResume();
		
		// SharedPreferences の設定
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sp.edit();
		// 端末に保存されている VersionCode/Name を取得
		int vCode = sp.getInt("VersionCode", 1);
		String vName = sp.getString("VersionName", "1.0");
		
		// バージョン情報の取得
		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
		}catch(NameNotFoundException e){
			e.printStackTrace();
		}
		// 最新バージョン情報を SharedPreferences に保存
		editor.putInt("VersionCode", pi.versionCode);
		editor.putString("VersionName", pi.versionName);
		editor.commit();
		
		// ダイアログの生成および内容の設定
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("更新履歴");
		// ダイアログに読み込むテキストファイル
		dialog.setMessage(readText("about/news.txt"));
		// 閉じる(OK) ボタンの設定
		dialog.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		// 更新履歴ダイアログの表示
		if(pi != null){
			// バージョンコードが SharedPreferences に保存されているコードより大きい (新しい) 場合
			if(pi.versionCode > vCode){
				// 謝辞の表示
				thanksText();
				dialog.create();
				dialog.show();
			}
		}
	}
    
	// 曲一覧アクティビティを開く
    public void openSongListActivity(){
    	Intent intent = new Intent(this, SongListActivity.class);
    	startActivityForResult(intent, 0);
    }

	// 検索アクティビティを開く
    public void openSearchActivity(){
    	Intent intent = new Intent(this, SearchActivity.class);
    	startActivityForResult(intent, 0);
    }
    
    // midi 付きリストを表示する
    public void openMidiList(){
    	Intent intent = new Intent(this, SongListActivity.class);

    	// midi 付きリストを得るための変数
    	ArrayList<Integer> midiList = new ArrayList<Integer>();
    	// 寮歌リストを取得して, midi 付きのフラグを調べる
    	ArrayList<String[]> ryokaList = csvReader.readCSV(this, "ryoka_list.csv");
    	String[] ryoka;
    	for(int i = 0; i < ryokaList.size(); i++){
    		ryoka = ryokaList.get(i);
    		if(!ryoka[SongView.SONG_WITH_MP3].equals("0")){
    			midiList.add(i);
    		}
    	}

    	intent.putIntegerArrayListExtra("jp.mkuriki.ryoka.SongListActivity.result", midiList);
    	startActivityForResult(intent, 0);
    }
    
    // このアプリについてボタンを押したときの動作
    public void openAboutActivity(){
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivityForResult(intent, 0);
    }
    
    // おまけボタンを押したときの動作
    // カード引きゲーム!
    public void openCardActivity(){
    	Intent intent = new Intent(this, DrawCard.class);
    	startActivityForResult(intent, 0);
    }
    
    // 広告消去ボタンを押したときの動作
    public void adDelete(){

		// ダイアログの生成および内容の設定
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("支援のお願い");
		// ダイアログに読み込むテキストファイル
		dialog.setMessage(readText("about/adDelete.txt"));
		// ボタンの設定
		dialog.setPositiveButton("支援する", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				NendAdInterstitial.setListener(new OnCompletionListener() {
				    @Override
				    public void onCompletion(NendAdInterstitialStatusCode status) {
				        switch (status) {
				        case SUCCESS:
				            break;
				        case INVALID_RESPONSE_TYPE:
				            // 不明な広告タイプ
							Toast.makeText(MainMenuActivity.this, "広告のロードに失敗しました. しばらく後でやりなおしてください. " + "INVALID_RESPONSE_TYPE", Toast.LENGTH_LONG).show();
				            break;
				        case FAILED_AD_REQUEST:
				            // 広告取得失敗
							Toast.makeText(MainMenuActivity.this, "広告のロードに失敗しました. しばらく後でやりなおしてください. " + "FAILED_AD_REQUEST", Toast.LENGTH_LONG).show();
				            break;
				        case FAILED_AD_INCOMPLETE:
				            // 広告取得未完了
							Toast.makeText(MainMenuActivity.this, "広告のロードに失敗しました. しばらく後でやりなおしてください. " + "FAILED_AD_INCOMPLETE", Toast.LENGTH_LONG).show();
				            break;
				        case FAILED_AD_DOWNLOAD:
				            // 広告画像取得失敗
							Toast.makeText(MainMenuActivity.this, "広告のロードに失敗しました. しばらく後でやりなおしてください. " + "FAILED_AD_DOWNLOAD", Toast.LENGTH_LONG).show();
				            break;
				        default:
				            break;
				        }
				    }
				});

				// インターステイシャル広告の表示
				NendAdInterstitial.showAd(MainMenuActivity.this, new NendAdInterstitial.OnClickListener() {
					@Override
					public void onClick(NendAdInterstitialClickType clickType) {
						switch(clickType){
						case CLOSE:
							// Toast.makeText(MainMenuActivity.this, "広告についての設定は変更されていません. ", Toast.LENGTH_LONG).show();
							return;
						case DOWNLOAD:
							recordAdTime();
							return;
						default:
							break;
						}
					}
				});
			}
		});
		dialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		// ダイアログ表示
		dialog.create();
		dialog.show();
    }
    
    // 広告消去時間を記録する関数
    private void recordAdTime(){

		// SharedPreferences の準備
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sp.edit();

		// 現在時刻をミリ秒で取得し, 7日後を計算して記録
		Long cTime = System.currentTimeMillis();
		cTime += 604800000;
    	// 現在時刻をミリ秒で取得し, 保存 
    	editor.putLong("adTime", cTime);
    	editor.commit();
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Toast.makeText(MainMenuActivity.this, "支援ありがとうございます. \n広告が一週間表示されなくなりました. \n" + df.format(cTime).toString() + "まで", Toast.LENGTH_LONG).show();
    }
    
    // SharedPreferences 設定を保存する関数
    private void setState(int state){
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	sp.edit().putInt("InitState", state).commit();
    }
    
    // SharedPreferences 設定を読み出す関数
    private int getState(){
    	int state;
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	state = sp.getInt("InitState", INIT);
    	
    	return state;
    }

    // txt ファイル読み込み処理:ファイル名から
    private String readText(String fileName) {
    	AssetManager as = getResources().getAssets();
    	
    	InputStream is = null;
    	BufferedReader br = null;
    	
    	// StringBuilder に複数行の txt ファイルを読み込む
    	StringBuilder sb = new StringBuilder();
    	try{
    		try{
    			// list で指定された txt ファイルを開く
    			is = as.open(fileName);
    			// BufferReader に　InputStream で読み込んだ txt ファイルを入れる
    			br = new BufferedReader(new InputStreamReader(is));
    			
    			String str;
    			// StringBuilder へ str に一行ずつ改行をつけたものを読み込んでいく 
    			while((str = br.readLine()) != null){
    				sb.append(str + "\n");
    			}
    		}
    		finally{
    			if (br != null){
    				br.close();
    			}
    		}
    	}
    	// テキスト読み込み失敗時の例外処理
    	catch (IOException e){
    		Toast.makeText(this, "テキストファイルの読み込みに失敗. "
    				+ "作者に問い合わせてください", Toast.LENGTH_SHORT).show();
    	}
    	
    	return sb.toString();
    }
    
    // Campfire 支援お願い用ダイアログの表示メソッド
    private void thanksText(){

//		// SharedPreferences の設定
//		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//		SharedPreferences.Editor editor = sp.edit();
//		// campfire が 2 ならもう表示しない
//		if(sp.getInt("campfire", 0) == 2){
//			return;
//		}
		/* 募集終了につき, 一回だけダイアログを表示する
		 * 
		// 端末に保存されている campfire カウンタを取得
		int campCount = sp.getInt("campfire", 0);
		// 起動が 5 回おきにダイアログを表示したい
		// カウンタが 0 でなければカウンタを増やして終了
		if(campCount != 0){
			// カウンタが 4 なら 0 にリセットする
			if(campCount == 4){
				editor.putInt("campfire", 0);
			}else{
				editor.putInt("campfire", campCount + 1);
			}
			editor.commit();
			return;
		}else{
			// カウンタが 0 のときは起動カウンタを増やして続行
			editor.putInt("campfire", campCount + 1);
			editor.commit();
		}
		*/
		
		// Campfire 宣伝用のダイアログ準備
		AlertDialog.Builder dialogCampfire = new AlertDialog.Builder(this);
		dialogCampfire.setTitle("謝辞");
		TextView tv = new TextView(this);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		tv.setAutoLinkMask(Linkify.ALL);
		tv.setText(readText("about/thanks.txt"));
		tv.setTextSize(18.0f);
		tv.setTextColor(Color.BLACK);
		dialogCampfire.setView(tv);
		dialogCampfire.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		// Campfire 宣伝用のダイアログ準備 終わり
    	
		dialogCampfire.create();
		dialogCampfire.show();

//		// 一回しか表示させないため, 2 に設定しておく
//		editor.putInt("campfire", 2);
//		editor.commit();

		return;
    }
}
