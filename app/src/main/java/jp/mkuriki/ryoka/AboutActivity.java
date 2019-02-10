/*
 *
 * ryokaApp_android
 * Licenced under MIT License. Copyright 2019, Kuriki Murahashi (@mkuriki_)
 *
 */
package jp.mkuriki.ryoka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class AboutActivity extends Activity {
	
	// テキストビュー用の定数
	public static final float TEXT_SIZE = 18.0f;
	
	// リストの選択肢用の定数
	private static final int ABOUT_KEITEKI = 0;
	private static final int ABOUT_RYOKA = 1;
	private static final int ABOUT_AD = 2;
	private static final int THANKS = 3;
	private static final int CONTACT = 4;
	private static final int NEWS = 5;
	private static final int VERSION = 6;
	private static final int PASSWORD = 7;
	
	// 広告費表示用のパスワード設定
	private static final String adPassword = "perasperaadastra";

	private static final String[] aboutList = {
		"北海道大学恵迪寮とは",
		"寮歌について",
		"広告について",
		"謝辞",
		"問い合わせ",
		"更新履歴",
		"バージョン情報",
		"広告消去設定",
	};

	private static final String[] aboutFileList = {
		"about/about_keiteki.txt",
		"about/about_ryoka.txt",
		"about/ad.txt",
		"about/thanks.txt",
		"about/contact.txt",
		"about/news.txt",
		"about/version.txt",
		"about/password.txt",
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);
        
        // リストを作成
        ListView list = (ListView)this.findViewById(R.id.listAbout);
        ArrayAdapter<String> arrayAdapter 
        	= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aboutList);
        list.setAdapter(arrayAdapter);
        
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				// パスワード設定用のメニューかどうかで判断
				if(pos != PASSWORD){
					openDialog(pos);
				}else{
					adPassword();
				}
			}

		});
    }
	
	private void openDialog(int pos)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		TextView tv = new TextView(this);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		// 寮についてと問い合わせはリンクが必要
		if(pos == ABOUT_KEITEKI || pos == CONTACT || pos == THANKS || pos == PASSWORD){
			tv.setAutoLinkMask(Linkify.ALL);
		}
		tv.setText(getMessage(pos));
		tv.setTextSize(TEXT_SIZE);
		tv.setTextColor(Color.WHITE);

		// ダイアログの設定
		dialog.setTitle(aboutList[pos]);
		dialog.setView(tv);
		
		
		// 閉じる(OK) ボタンの設定
		dialog.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		dialog.create();
		dialog.show();
	}
	
	// ダイアログに表示するメッセージを取得
	private String getMessage(int pos)
	{
		String message = null;
		message = readText(aboutFileList[pos]);
		return message;
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

    // 広告消去パスワード入力用ダイアログ
    private void adPassword(){

		// SharedPreferences の設定
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = sp.edit();
		
		// パスワード入力用のダイアログ準備
		final EditText editPassword = new EditText(this);
		editPassword.setMaxLines(1);
		editPassword.setInputType(InputType.TYPE_CLASS_TEXT);
		AlertDialog.Builder adDialog = new AlertDialog.Builder(this);

		adDialog.setTitle("広告消去用パスワード");
		adDialog.setView(editPassword);
		adDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String inputPassword = editPassword.getText().toString();
				if(editPassword.getText().toString().equals(adPassword)){
					Toast.makeText(AboutActivity.this, "正しいパスワードが入力されました\nアプリを再起動してください", Toast.LENGTH_LONG).show();
					// パスワードが正しければ SharedPreferences に 1 を設定
					editor.putInt("password", 1);
					editor.commit();
				}else{
					Toast.makeText(AboutActivity.this, "パスワードが間違っています\n広告が表示される設定になっています", Toast.LENGTH_LONG).show();
					// 設定をリセットするため SharedPreferences に 0 を設定
					editor.putInt("password", 0);
					editor.commit();
				}
			}
		});
		adDialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(sp.getInt("password", 0) == 1){
					Toast.makeText(AboutActivity.this, "広告が表示されない設定になっています", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(AboutActivity.this, "広告が表示される設定になっています", Toast.LENGTH_LONG).show();
				}
			}
		});

		/*
		// テキストボックスで確定キーを押したときの処理
        editPassword.setOnEditorActionListener(new OnEditorActionListener(){
        	@Override
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
				if(editPassword.getText().toString().equals(adPassword)){
					Toast.makeText(AboutActivity.this, "正しいパスワードが入力されました\nアプリを再起動してください", Toast.LENGTH_LONG).show();
					// パスワードが正しければ SharedPreferences に 1 を設定
					editor.putInt("password", 1);
					editor.commit();
				}else{
					Toast.makeText(AboutActivity.this, "パスワードが間違っています\n広告が表示される設定になっています", Toast.LENGTH_LONG).show();
					// 設定をリセットするため SharedPreferences に 0 を設定
					editor.putInt("password", 0);
					editor.commit();
				}
        		return true;
        	}
        });
        */
    	
		adDialog.create();
		adDialog.show();

		return;
    }
}
