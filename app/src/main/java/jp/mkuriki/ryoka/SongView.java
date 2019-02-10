package jp.mkuriki.ryoka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import net.nend.android.NendAdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SongView extends Activity{

	// Intent 用定数
	public static final int GO_HOME = 9;
	public static final int GO_NEXT = 8;
	public static final int GO_PREV = 7;
	public static final int FROM_SONG_SHOW = 10;
	
	// 寮歌リストの midi ファイルがあるかどうかのフラグ
	public static final int SONG_WITH_MIDI = 7;
	// 寮歌リストの mp3  ファイルがあるかどうかのフラグ
	public static final int SONG_WITH_MP3 = 8;
	// 寮歌リストの mp3  ファイルがあるかどうかのフラグ
	public static final int SONG_WITH_HTML = 9;
	// 曲のファイル名
	private String midiFileName;
	// mp3 のファイル名
	private String mp3FileName;
	// html のファイル名
	private String htmlFileName;
	// MediaPlayer
	private MediaPlayer mPlayer = null;
	private MediaPlayer mp3Player = null;
	private MediaPlayer mp3StreamPlayer = null;
	// private MediaPlayer hiresPlayer = null;
	
	// ネットワークの接続状況を取得する変数
	private ConnectivityManager cm;
	
	// 寮歌リストの読み込み
	private ArrayList<String[]> ryokaList;
	
	ArrayList<Integer> resultList;
	
	private int fileNum = 0;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        // intent を取得
        final Intent intent = getIntent();
        // ネットワーク接続の状況を取得
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        super.onCreate(savedInstanceState);

		// 寮歌リストを取得
		ryokaList = csvReader.readCSV(this, "ryoka_list.csv");
		final int ryokaCount = ryokaList.size();

		if(intent != null){
			resultList = intent.getIntegerArrayListExtra("searchResult");
			// 選択された寮歌のファイル名
			fileNum = intent.getIntExtra("songFileNum", 0);
		}

		// mp3 データがある寮歌ならばボタンを生成
		htmlFileName = ryokaList.get(fileNum)[SONG_WITH_HTML];
		if(htmlFileName.equals("0")) {
		    // HTML ファイルがなければテキストようのビューをセット
			setContentView(R.layout.songview);
			// 寮歌読み込み
			String lyrics = readSongText(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME]);

			// R.id.label に StringBuilder をテキストに変換して配置
			// TextView songView = (TextView)this.findViewById(R.id.songView);
			TextView songView = (TextView)findViewById(R.id.songView);
			// songView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
			songView.setText(lyrics);
		}else{
			// HTML ファイル用のビューをセット
			setContentView(R.layout.rubyview);
			WebView rubyview = (WebView)findViewById(R.id.rubyView);
			rubyview.setBackgroundColor(0);
			String htmlFile = String.format("file:///android_asset/html/%s.html", htmlFileName);
			rubyview.loadUrl(htmlFile);
		}

        // final LinearLayout linearLayout = (LinearLayout)findViewById(R.id.songViewLayout);
        
		// SharedPreferences の準備
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// 広告表示がオフかどうかチェック
		if((sp.getInt("password", 0) == 1) || (sp.getLong("adTime", 0) > System.currentTimeMillis())){
			// 広告を消す処理
			LinearLayout adLayout = (LinearLayout)findViewById(R.id.songViewLayout);
			NendAdView adView2 = (NendAdView)findViewById(R.id.nend2);
			adLayout.removeView((View)adView2);
		}
        
        // 特定の寮歌の際に画像を表示
        setImage();

        // ボタンの取得
        Button nextBtn = (Button)findViewById(R.id.nextButton);
        Button prevBtn = (Button)findViewById(R.id.prevButton);
        Button topBtn = (Button)findViewById(R.id.topButton);

        // 最初へボタンを押したときの処理
        topBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		setResult(GO_HOME, intent);
        		// MediaPlayer の解放
        		if(mPlayer != null){
        			mPlayer.release();
        			mPlayer = null;
        		}
        		finish();
        	}
        });
        // 次へボタンを押したときの処理
        nextBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// 次の寮歌を指定しておき, 一度 SongActivity を終了して
        		// 改めて SongListActivity から SongActivity を開いてもらう
        		if(resultList != null){
        			// 検索結果のリストの場合
        			// リスト外にいかないように条件分岐
        			int index = resultList.indexOf(fileNum);
        			if((index + 1) >= resultList.size()){
        				Toast.makeText(SongView.this, "これ以上ありません", Toast.LENGTH_SHORT).show();
        				return;
        			}else{
        				fileNum = resultList.get(index + 1);
        			}
        		}else{
        			// 全寮歌リストの場合
        			if((fileNum + 1) >= ryokaCount){
        				Toast.makeText(SongView.this, "これ以上ありません", Toast.LENGTH_SHORT).show();
        				return;
        			}else{
        				fileNum += 1;
        			}
        		}
        		setResult(GO_NEXT, intent);
        		intent.putExtra("songNextNum", fileNum);
        		// MediaPlayer の解放
        		if(mPlayer != null){
        			mPlayer.release();
        			mPlayer = null;
        		}
        		finish();
        	}
        });

        // 前へボタンを押したときの処理
        prevBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// 次の寮歌を指定しておき, 一度 SongActivity を終了して
        		// 改めて SongListActivity から SongActivity を開いてもらう
        		if(resultList != null){
        			// リスト外にいかないように条件分岐
        			int index = resultList.indexOf(fileNum);
        			if((index - 1) < 0){
        				Toast.makeText(SongView.this, "これ以上ありません", Toast.LENGTH_LONG).show();
        				return;
        			}else{
        				fileNum = resultList.get(index - 1);
        			}
        		}else{
        			// 全寮歌リストの場合
        			if((fileNum - 1) < 0){
        				Toast.makeText(SongView.this, "これ以上ありません", Toast.LENGTH_LONG).show();
        				return;
        			}else{
        				fileNum -= 1;
        			}
        		}
        		setResult(GO_PREV, intent);
        		intent.putExtra("songPrevNum", fileNum);
        		// MediaPlayer の解放
        		if(mPlayer != null){
        			mPlayer.release();
        			mPlayer = null;
        		}
        		finish();
        	}
        });

        // mp3 データがある寮歌ならばボタンを生成
        mp3FileName = ryokaList.get(fileNum)[SONG_WITH_MP3];
        if(!mp3FileName.equals("0")){
        	// mp3 再生ボタンの追加
        	LinearLayout songLayout = (LinearLayout)findViewById(R.id.songViewLayout);
        	// Button playBtn = new Button(this);
        	// playBtn.setText("歌を再生/停止");
        	// songLayout.addView(playBtn);

        	// mp3 系ボタンのレイアウトを追加
        	LayoutInflater inflater = LayoutInflater.from(this);
        	LinearLayout mp3Layout = (LinearLayout)inflater.inflate(R.layout.mp3view, null);
        	songLayout.addView(mp3Layout);

        	// 再生用のボタン取得
        	Button playBtn = (Button)findViewById(R.id.playMp3);
        	
        	// 再生機能の実装
        	if(mp3Player == null){
        		mp3Player = MediaPlayer.create(getApplicationContext(), getResources().getIdentifier(mp3FileName, "raw", "jp.mkuriki.ryoka"));
        	}
        	playBtn.setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v){
        			if(!mp3Player.isPlaying()){
        				try{
        					mp3Player.prepare();
        				}catch(IllegalStateException e){
        					e.printStackTrace();
        					// Toast.makeText(SongView.this, "エラーが発生しました. mp3Player.prepare IllegalStateException", Toast.LENGTH_SHORT).show();
        				}catch(IOException e){
        					e.printStackTrace();
        					// Toast.makeText(SongView.this, "エラーが発生しました. mp3Player.prepare IOException", Toast.LENGTH_SHORT).show();
        				}
        				mp3Player.seekTo(0);
        				mp3Player.start();

        				// 都ぞ弥生のときだけダイアログで楡陵謳春賦を表示
        				if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/m45.txt")){
        					openDialog();
        				}
        			}else{
        				mp3Player.pause();
        			}
        		}
        	});

        	// mp3 ダウンロード用のボタン取得
        	Button downloadBtn = (Button)findViewById(R.id.downloadMp3);

        	// mp3 ファイルへのリンクの作成
        	mp3FileName = ryokaList.get(fileNum)[SONG_WITH_MP3];
        	// mp3FileName = "http://www.ep.sci.hokudai.ac.jp/~mkuriki/phone/ryoka/mp3/" + mp3FileName + ".mp3";
			mp3FileName = "http://133.50.160.51/~mkuriki/phone/ryoka/mp3/" + mp3FileName + ".mp3";

        	// mp3 ダウンロードボタンの処理
        	downloadBtn.setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v){
        			NetworkInfo nInfo = cm.getActiveNetworkInfo(); 
        			if(nInfo == null){
        				Toast.makeText(SongView.this, "ネットワークに接続されていないので開けません", Toast.LENGTH_SHORT).show();
        				return;
        			}
        			// 多重読み込みを防ぐため, player が null のときだけ読み込み処理をする. 
        			if(mp3StreamPlayer == null){
        				// ストリーミング用の Player 作成
        				mp3StreamPlayer = new MediaPlayer();
        				// ストリーミング用 MP3 の読み込み
        				try{
        					mp3StreamPlayer.setDataSource(mp3FileName);
        				}catch (IllegalArgumentException e){
        					// TODO 自動生成された catch ブロック
        					e.printStackTrace();
        					Toast.makeText(SongView.this, "エラーが発生しました. mp3StreamPlayer.setDataSource IllegalArgumentException", Toast.LENGTH_SHORT).show();
        				}catch (IllegalStateException e){
        					// TODO 自動生成された catch ブロック
        					e.printStackTrace();
        					Toast.makeText(SongView.this, "エラーが発生しました. mp3StreamPlayer.setDataSource IllegalStateException", Toast.LENGTH_SHORT).show();
        				}catch (IOException e){
        					// TODO 自動生成された catch ブロック
        					e.printStackTrace();
        					Toast.makeText(SongView.this, "エラーが発生しました. mp3StreamPlayer.setDataSource IOException", Toast.LENGTH_SHORT).show();
        				}
        			}
        			if(!mp3StreamPlayer.isPlaying()){
        				try{
        					mp3StreamPlayer.prepare();
        				}catch(IllegalStateException e){
        					e.printStackTrace();
        					Toast.makeText(SongView.this, "エラーが発生しました. mp3StreamPlayer.prepare IllegalStateException", Toast.LENGTH_SHORT).show();
        				}catch(IOException e){
        					e.printStackTrace();
        					Toast.makeText(SongView.this, "エラーが発生しました. mp3StreamPlayer.prepare IOException", Toast.LENGTH_SHORT).show();
        				}
        				mp3StreamPlayer.seekTo(0);
        				mp3StreamPlayer.start();
        				Toast.makeText(SongView.this, "高音質データはウェブからダウンロードしています. モバイルのデータ通信量には注意してください. ", Toast.LENGTH_SHORT).show();

        				// 都ぞ弥生のときだけダイアログで楡陵謳春賦を表示
        				if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/m45.txt")){
        					openDialog();
        				}
        			}else{
        				mp3StreamPlayer.pause();
        			}
        		}
        	});
        	
        }
        
        // MIDI データがある寮歌ならばボタンを生成
        // さらに PDF へのウェブリンクも追加
        midiFileName = ryokaList.get(fileNum)[SONG_WITH_MIDI];
        if(!midiFileName.equals("0")){
        	// midi 系ボタンのレイアウトを追加
        	LinearLayout songLayout = (LinearLayout)findViewById(R.id.songViewLayout);
        	LayoutInflater inflater = LayoutInflater.from(this);
        	LinearLayout midiLayout = (LinearLayout)inflater.inflate(R.layout.midiview, null);
        	songLayout.addView(midiLayout);

        	// 再生用のボタン取得
        	Button playBtn = (Button)findViewById(R.id.playMidi);
        	
        	// 再生機能の実装
        	mPlayer = new MediaPlayer();
        	try {
        		// midi ファイルの読み込み
                AssetFileDescriptor afd = getResources().getAssets().openFd("midi/" + midiFileName + ".midi");
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        	playBtn.setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v){
        			if(!mPlayer.isPlaying()){
        				try{
        					mPlayer.prepare();
        				}catch(IllegalStateException e){
        					e.printStackTrace();
        				}catch(IOException e){
        					e.printStackTrace();
        				}
        				mPlayer.seekTo(0);
        				mPlayer.start();
        			}else{
        				mPlayer.pause();
        			}
        		}
        	});
        	
        	// 楽譜ダウンロード用のボタン取得
        	Button pdfBtn = (Button)findViewById(R.id.downloadPdf);

        	// PDF ファイルへのリンクの作成
        	midiFileName = ryokaList.get(fileNum)[SONG_WITH_MIDI];
        	// midiFileName = "http://www.ep.sci.hokudai.ac.jp/~mkuriki/phone/ryoka/midi/" + midiFileName + ".pdf";
			midiFileName = "http://133.50.160.51/~mkuriki/phone/ryoka/midi/" + midiFileName + ".pdf";

        	// PDF ダウンロードボタンの処理
        	pdfBtn.setOnClickListener(new OnClickListener(){
        		@Override
        		public void onClick(View v){
        			NetworkInfo nInfo = cm.getActiveNetworkInfo(); 
        			if(nInfo == null){
        				Toast.makeText(SongView.this, "ネットワークに接続されていないので開けません", Toast.LENGTH_SHORT).show();
        				return;
        			}
        			// インテントのインスタンス生成
        			Intent intent = new Intent();
        			// インテントにアクション及びURLをセット
        			intent.setAction(Intent.ACTION_VIEW);
        			intent.setData(Uri.parse(midiFileName));
        			// ブラウザ起動
        			startActivity(intent);
        		}
        	});
        }
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(mPlayer != null){
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		if(mp3Player != null){
			mp3Player.stop();
			mp3Player.release();
			mp3Player = null;
		}
		if(mp3StreamPlayer != null){
			mp3StreamPlayer.stop();
			mp3StreamPlayer.release();
			mp3StreamPlayer = null;
		}
	}
	
	// txt ファイル読み込み処理:ファイル名から
    private String readSongText(String fileName) {
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
    		return new String("読み込み失敗");
    	}
    	return sb.toString();
    }
    
    // 挿絵を表示するための関数
    private void setImage(){
    	ImageView image = (ImageView)findViewById(R.id.songImage);
        // 明治45年寮歌「都ぞ弥生」には旧寮の前と都ぞ弥生
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/m45.txt"))
        	image.setImageResource(R.drawable.m45);
    	// 昭和42年第60回記念祭歌「芳香漂う」にはファイヤーストーム
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/s42b.txt"))
        	image.setImageResource(R.drawable.s42);
        // 昭和46年「朔北に」にはポプラ
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/s46.txt"))
        	image.setImageResource(R.drawable.s46);
        // 昭和53年第70回記念祭歌「草は萌え出で」には士幌小屋
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/s53b.txt"))
        	image.setImageResource(R.drawable.s53);
        // 昭和54年寮歌「うす紅の」には夕暮れ
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/s54.txt"))
        	image.setImageResource(R.drawable.s54);
        // 昭和59年度「雪の白さに」には雪の恵迪寮
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/s59.txt"))
        	image.setImageResource(R.drawable.s59);
        // 昭和62年度「北斗遙かに」には上士幌高原の紺碧ににじむ大空
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/s62.txt"))
        	image.setImageResource(R.drawable.s62);
        // 平成2年度「我楡陵に」には夕暮れの手稲山
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/h02.txt"))
        	image.setImageResource(R.drawable.h02);
        // 平成10年度寮歌「生命萌え出で」には追いコンの写真
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/h10b.txt"))
        	image.setImageResource(R.drawable.h10);
        // 平成15年度寮歌「ああグッと」には295期のビール300本祭りあと
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/h15.txt"))
        	image.setImageResource(R.drawable.h15);
        // 平成20年第100回記念祭歌「雲海貫く」には100回記念壁画
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("ryoka/h20b.txt"))
        	image.setImageResource(R.drawable.h20);
        // 大正9年桜星会歌「瓔珞磨く」には雪の羊蹄山
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("append/a005.txt"))
        	image.setImageResource(R.drawable.t09);
        // ストームの歌にはストーム 
        if(ryokaList.get(fileNum)[SongListActivity.SONG_FILENAME].equals("append/a010.txt"))
        	image.setImageResource(R.drawable.storm);
        
        return;
    }
    
    private void openDialog(){
		// ダイアログの生成および内容の設定
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("楡陵謳春賦");
		// ダイアログに読み込むテキストファイル
		dialog.setMessage(readSongText("about/yuryo.txt"));
		// 閉じる(OK) ボタンの設定
		dialog.setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		dialog.setNegativeButton("歌を止める", null);
		
		// ダイアログの表示
		dialog.create();
		final AlertDialog ad = dialog.show();
		
		Button btn = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mp3Player.isPlaying()){
					mp3Player.pause();
				}
				if(mp3StreamPlayer.isPlaying()){
					mp3StreamPlayer.pause();
				}
			}
		});
    }
}
