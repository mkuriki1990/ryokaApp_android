package jp.mkuriki.ryoka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.nend.android.NendAdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class DrawCard extends Activity
{
	// レイアウト
	private Button resetButton;
	private Button openButton;
	private Button howToBtn;
	private Button whatCardBtn;
	
	// カードの総数
	private static final int CARD_COUNT = 12;
	private ArrayList<Integer> drawed = new ArrayList<Integer>();
	
	private ImageView[][] cardList = new ImageView[CARD_COUNT][2];
	private RelativeLayout[] layoutList = new RelativeLayout[CARD_COUNT];
	// カードを引いたかどうかのフラグ
	private boolean[] drawable = new boolean[CARD_COUNT];
    // カードの色情報
    private int[] cardColor = new int[CARD_COUNT];
	// カードを引いた数のカウンタ
	private int drawCount = 0;
	// 色一覧
    private int[] colorList =
			{
                0x88e60012, // 赤
                0x880068ff, // 青
				0x88fff100, // 黄
				0x88009944, // 緑
				0x88e4007f, // 桃
				0x88f39800, // 橙
				0x88000000, // 白
				0x888fc31f, // 黄緑
				0x8800a0e9, // 水
             	0x883d20bb, // 紫
            	0x887e5727, // 茶
                0x88888888, // 肌
            };
	

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawcard);
        
		// SharedPreferences の準備
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// 広告表示がオフかどうかチェック
		if(sp.getInt("password", 0) == 1){
			// 広告を消す処理
			LinearLayout adLayout = (LinearLayout)this.findViewById(R.id.drawCardLayout);
			NendAdView adView = (NendAdView)this.findViewById(R.id.nend);
			NendAdView adView2 = (NendAdView)this.findViewById(R.id.nend2);
			adLayout.removeView((View)adView);
			adLayout.removeView((View)adView2);
		}else if(sp.getLong("adTime", 0) > System.currentTimeMillis()){
			LinearLayout adLayout = (LinearLayout)this.findViewById(R.id.drawCardLayout);
			NendAdView adView = (NendAdView)this.findViewById(R.id.nend);
			adLayout.removeView((View)adView);
		}

		// リセットボタンの読み込み
        resetButton = (Button)findViewById(R.id.resetButton);
		// 全オープンボタンの読み込み
        openButton = (Button)findViewById(R.id.openButton);

        // カードの View ID 読み込みなど
        for(int i = 0; i < CARD_COUNT; i++){
        	String cardFrontID = "cardView" + String.valueOf(i);
        	ImageView front = (ImageView)findViewById(getResources().getIdentifier(cardFrontID, "id", "jp.mkuriki.ryoka"));
        	String cardBackID = "cardBack" + String.valueOf(i);
        	ImageView back = (ImageView)findViewById(getResources().getIdentifier(cardBackID, "id", "jp.mkuriki.ryoka"));
        	String layoutID = "cardBase" + String.valueOf(i);
        	RelativeLayout layout = (RelativeLayout)findViewById(getResources().getIdentifier(layoutID, "id", "jp.mkuriki.ryoka"));

        	// カードほ数字は 0, 裏は 1 
        	cardList[i][0] = front;
        	cardList[i][1] = back;
        	cardList[i][1].setClickable(true);
        	
        	layoutList[i] = layout;
        	drawable[i] = true;
        }
        
        // カード引きの遊び方, そもそも何かボタン
        howToBtn = (Button)findViewById(R.id.howToButton);
        whatCardBtn = (Button)findViewById(R.id.whatCardButton);
        
        // カード自体がタップされたときの処理
        for(int i = 0; i < CARD_COUNT; i++){
        	cardList[i][1].setOnClickListener(new OnClickListener(){
        		// ローカル変数 i を取得するための処理
        		private int i;
        		public OnClickListener setInt(int i){
        			this.i = i;
        			return this;
        		}
        		@Override
        		public void onClick(View v){
					// カードがまだ引かれていなければ引く
        			if(drawable[i]){
        				// カードを引く処理
        				// drawCard(i);
        				// カードを引いたカウントをインクリメント
        				drawCount += 1;
        				// カードを選択済み画像に変更
        				String cardFile = "ta";
        				if(drawCount < 10){
							cardFile += "0";
        				}
        				cardFile += String.valueOf(drawCount);
        				// int resource = getResources().getIdentifier("ta01", "drawable", "jp.mkuriki.ryoka");
        				int resource = getResources().getIdentifier(cardFile, "drawable", "jp.mkuriki.ryoka");
						cardList[i][1].setImageResource(resource);
						// cardList[i][1].setColorFilter(0xcc0000ff, PorterDuff.Mode.SRC_IN);
                        // カードの色変更と記録
						cardList[i][1].setColorFilter(colorList[drawCount-1]);
                        cardColor[i] = colorList[drawCount - 1];
        				// タップしたら Vibration!!
        				// 振動させるよ！
        				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        				vibrator.vibrate(30);
        				drawable[i] = false;
        			}
        		}
        	}.setInt(i));
        }
        
        // リセットボタンを押したときの処理
        resetButton.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// カードを引いてない状態に戻す
        		for(int i = 0; i < CARD_COUNT; i++){
					int resource = getResources().getIdentifier("tz01", "drawable", "jp.mkuriki.ryoka");
					cardList[i][1].setImageResource(resource);
        			cardList[i][1].setVisibility(View.VISIBLE);
        			cardList[i][0].setVisibility(View.GONE);
        			drawable[i] = true;
                    cardList[i][1].setColorFilter(0x000000);
        			// 引いたカードの種類リストもリセット
        			drawed = new ArrayList<Integer>();
        			// 引いたカードの数をリセット
        			drawCount = 0;
        			// 振動するぜ！
        			Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        			vibrator.vibrate(30);
        		}
        	}
        });
        
        // オープンボタン
        openButton.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// 選ばれているカードを全てオープンする
        		for(int i = 0; i < CARD_COUNT; i++){
        			// 選ばれているカードを引く
        			if(!drawable[i]){
        				drawCard(i);
        			}
        			// カードの選択状態をリセット
        			drawable[i] = true;
        			// 振動するぜ！
        			Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        			vibrator.vibrate(30);
        		}
        	}
        });

        howToBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openDialog("遊び方", "about/howtoplay.txt");
        	}
        });

        whatCardBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		openDialog("カード引きとは", "about/whatcard.txt");
        	}
        });
	}
	
	private void drawCard(int num)
	{
		// 同じカードを引いたかどうかのフラグ
		boolean sameCard = true;
		int markNum = 0;
		int cardNum = 1;
		int cardId = 0;
		String cardFile = "tz01";
		
		// 同じカードにならなくなるまでループを回す. 
		while(sameCard){
			// 柄の決定 : スペード, ハート, ダイヤ, クラブ (1-4)
			markNum = (int)(Math.random()*4 + 1);
			// 数の決定 : 1 - 13
			cardNum = (int)(Math.random()*13 + 1);
			cardId = cardNum * markNum;
			
			// 同じカードかのフラグもしくはこれまで引いたカードがまだない場合
			if(!drawed.isEmpty()){
				for(int i = 0; i < drawed.size(); i++){
					if(drawed.contains(Integer.valueOf(cardId))){
						break;
					}
					else{
						// 同じカードではなかったことをフラグに入れる
						sameCard = false;
						// リストにカード情報を追加
						drawed.add(Integer.valueOf(cardId));
					}
				}
			}
			else{
				// 同じカードではなかったことをフラグに入れる
				sameCard = false;
				// リストにカード情報を追加
				drawed.add(Integer.valueOf(cardId));
			}
		}

		// カードのファイル名
		cardFile = String.valueOf(cardNum);

		// 数字が 9 以下のときは 0 埋めしたい
		if(cardNum <= 9){
			cardFile = "0" + cardFile;
		}
		
		switch(markNum){
		case 1: // スペード
			cardFile = "ts" + cardFile;
			break;
		case 2: // ハート
			cardFile = "th" + cardFile;
			break;
		case 3: // ダイヤ
			cardFile = "td" + cardFile;
			break;
		case 4: // クラブ
			cardFile = "tc" + cardFile;
			break;
		}

		// 53 分の 1 の確率で JOKER が出てくる
		if((int)(Math.random()*53 + 1) == 53){
			cardFile = "tx01";
		}
		
		// カードが決定したので該当する R.drawable.xxx の リソースID を取得
		int resource = getResources().getIdentifier(cardFile, "drawable", "jp.mkuriki.ryoka");
		cardList[num][0].setImageResource(resource);
        // カードの色変更と記録
        cardList[num][0].setColorFilter(cardColor[num]);
        // cardColor[i] = colorList[drawCount - 1];

		//カードのアニメーションを規定
		RotateAnimator anim = new RotateAnimator(cardList[num][1], cardList[num][0], layoutList[num].getWidth()/2, layoutList[num].getHeight()/2);
		layoutList[num].startAnimation(anim);
	}
	
	private void openDialog(String title, String fileName)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		TextView tv = new TextView(this);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		tv.setText(readText(fileName));
		tv.setTextSize(AboutActivity.TEXT_SIZE);
		tv.setTextColor(Color.WHITE);

		// ダイアログの設定
		dialog.setTitle(title);
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
}
