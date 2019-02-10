package jp.mkuriki.ryoka;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.nend.android.NendAdView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

// 寮歌を検索するアクティビティ
public class SearchActivity extends Activity {
	
    // ラジオボタン用定数
	private static int RADIO_AND = R.id.radioAnd;
	private static int RADIO_OR = R.id.radioOr;
	// Intent 用定数
	private static int FROM_SEARCH = 10;
    // テキスト入力領域を取得
    private EditText searchText;
    // 検索ボタンを取得
    private Button searchButton;
    // チェックボックスの一覧
    private CheckBox[] checkBoxes;
    // 寮歌リスト読み込み
    private ArrayList<String[]> ryokaList;
    // 検索結果を入れる変数
    public ArrayList<Integer> result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		// SharedPreferences の準備
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// 広告表示がオフかどうかチェック
		if((sp.getInt("password", 0) == 1) || (sp.getLong("adTime", 0) > System.currentTimeMillis())){
			// 広告を消す処理
			LinearLayout adLayout = (LinearLayout)this.findViewById(R.id.searchLayout);
			NendAdView adView2 = (NendAdView)this.findViewById(R.id.nend2);
			adLayout.removeView((View)adView2);
		}

		// 寮歌リスト読み込み
		ryokaList = csvReader.readCSV(this, "ryoka_list.csv");
        
		// チェックボックスを取得
		checkBoxes = new CheckBox[] {
        		(CheckBox)findViewById(R.id.checkTitle),
                (CheckBox)findViewById(R.id.checkSong),
        };

        // テキスト入力領域を取得
        searchText = (EditText)findViewById(R.id.searchText);
        // 検索ボタンを取得
        searchButton = (Button)findViewById(R.id.search);
        // ラジオグループを取得
        final RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioAndOr);
		
		// テキストを取得し, 検索をする. 
        searchButton.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		// 検索用のワードを取得
        		String search = searchText.getText().toString();
        		searchRyoka(search, radioGroup.getCheckedRadioButtonId());
        	}
        });
        
        // テキストボックスで確定キーを押したときの処理
        searchText.setOnEditorActionListener(new OnEditorActionListener(){
        	@Override
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
        		// 検索用のワードを取得
        		String search = searchText.getText().toString();
        		return searchRyoka(search, radioGroup.getCheckedRadioButtonId());
        	}
        });
    }
	
	// 歌詞表示用の Activity から戻ってきたときの処理
	@Override
	public void onActivityResult(int request, int result, Intent data)
	{
		if(request == FROM_SEARCH){
			// GO_HOME だったらトップに戻るためアクティビティを閉じる
			if(result == SongView.GO_HOME){
				finish();
			}
		}
	}

	// CheckBox 配列のチェックボックスを読み取って boolean 配列に入れる関数
	private boolean[] checkBoxToBoolean(CheckBox[] cb)
	{
		boolean[] flag = new boolean[cb.length];
		
		for(int i = 0; i < cb.length; i++){
			flag[i] = cb[i].isChecked();
		}
		
		return flag;
	}
	
	// 検索ワード分割と結果の表示
	// テキスト確定ボタン用に boolean 型を返す
	private boolean searchRyoka(String search, int radioState){
		// 検索用のワードを取得
		String[] searcher = null;
		ArrayList<String> searchWords = new ArrayList<String>();

		// もし検索欄が空白なら入力を促すトーストを出して終了
		if(search.equals("")){
			Toast.makeText(SearchActivity.this, "検索ワードを入力してください", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(search.matches(".*" + "　" + ".*")){
			// 検索用ワードに全角スペースがあればそれで区切る
			searcher = searchText.getText().toString().split("　");
		}else if(search.matches(".*" + " " + ".*")){
			// 検索用ワードに半角スペースがあればそれで区切る
			searcher = search.split(" ");
		}else{
			// 複数検索でないときの対応
			searcher = new String[1];
			searcher[0] = search;
		}
		// 複数の検索ワードからなるリストを作成
		if(searcher != null){
			for(int i = 0; i < searcher.length; i++){
				searchWords.add(searcher[i]);
			}
		}

		// AND 検索
		if(radioState == RADIO_AND){
			result = searchWordAnd(searchWords, checkBoxToBoolean(checkBoxes));
		}
		// OR 検索
		else if(radioState == RADIO_OR){
			result = searchWordOr(searchWords, checkBoxToBoolean(checkBoxes));
		}

		if(!result.isEmpty()){
			// 検索結果がある場合 result は空リストではない
			Toast.makeText(SearchActivity.this, String.valueOf(result.size()) + "件見つかりました", Toast.LENGTH_LONG).show();
			// 寮歌リストアクティビティ呼び出し
			Intent intent = new Intent(SearchActivity.this, SongListActivity.class);
			intent.putIntegerArrayListExtra("jp.mkuriki.ryoka.SongListActivity.result", result);
			startActivityForResult(intent, FROM_SEARCH);
		}
		else{
			// 検索結果がない場合 result は空リストなのでその旨をトースト
			Toast.makeText(SearchActivity.this, "検索結果が見つかりません", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

	// 寮歌を検索し, 検索結果の通し番号の ArrayList を返す
	private ArrayList<Integer> searchWord(String word, boolean[] flag)
	{
		// 検索結果を返すための変数. 該当する寮歌の番号を返す. 
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		// 寮歌一覧の長さ
		int length = ryokaList.size();
		// 検索対象の文字列を入れる変数
		String[] src;
		String lyrics;
		
		// 検索に引っかかったかを示すフラグ
		boolean matched = false;
		
		// 寮歌リスト全体にわたって検索
		for(int i = 0; i < length; i++){
			// フラグを元に戻す
			matched = false;

			// 年, 作歌者, 作曲者, 曲名を探すループ
			// もしこれらの検索フラグが立っていなければスルーする
			if(flag[0]){
				for(int j = 0; j <= SongListActivity.SONG_TITLE; j++)
				{
					// 検索対象を読み込む
					src = ryokaList.get(i);
					// 部分一致を探す
					if(src[j].matches(".*" + word + ".*"))
					{
						// 結果をリストに入れる
						result.add(i);
						// 重複してカウントするのを避けるため, 一回見つければループを抜ける
						// break 後に歌詞を検索し始めないようにフラグも立てる
						matched = true;
						break;
					}
				}
			}
			
			// 歌詞の中を検索する
			// 上と同じく歌詞の検索フラグが立っていなければスルー
			if(flag[1]){
				if(!matched){
					// 検索対象を読み込む
					// 曲名などの検索フラグに応じて微妙に処理が違う
					// 歌詞のうち最初の4行を飛ばすかどうかに関わる
					src = ryokaList.get(i);
					lyrics = readSongText(src[SongListActivity.SONG_FILENAME], 
											flag[0], 
											Integer.valueOf(src[SongListActivity.SONG_HEADER]));
					// 部分一致を探す
					if(lyrics.matches(".*" + word + ".*"))
					{
						result.add(i);
					}
				}
			}
		}
		
		return result;
	}


	// 寮歌を検索し, 検索結果の通し番号の ArrayList を返す
	// 複数の検索ワードを持ったリストを用いた OR 検索
	private ArrayList<Integer> searchWordOr(ArrayList<String> wordList, boolean[] flag)
	{
		// 検索結果を返すための変数. 該当する寮歌の番号を返す. 
		ArrayList<Integer> result = new ArrayList<Integer>();

		// 寮歌一覧の長さ
		int length = ryokaList.size();
		// 検索語リストの長さ
		int searchLength = wordList.size();
		// 検索対象の文字列を入れる変数
		String[] src;
		String lyrics;

		// 検索に引っかかったかを示すフラグ
		boolean matched = false;

		// 寮歌リスト全体にわたって検索
		for(int i = 0; i < length; i++){
			// フラグを元に戻す
			matched = false;

			// 年, 作歌者, 作曲者, 曲名を探すループ
			// もしこれらの検索フラグが立っていなければスルーする
			if(flag[0]){
				label_finish:
					for(int j = 0; j <= SongListActivity.SONG_TITLE; j++)
					{
						// 検索対象を読み込む
						src = ryokaList.get(i);
						// 部分一致を探す
						// OR 検索
						for(int k = 0; k < searchLength; k++){
							if(src[j].matches(".*" + wordList.get(k).toString() + ".*"))
							{
								// 結果をリストに入れる
								result.add(i);
								// 重複してカウントするのを避けるため, 一回見つければループを抜ける
								// break 後に歌詞を検索し始めないようにフラグも立てる
								matched = true;
								break label_finish;
							}
						}
					}
			}

			// 歌詞の中を検索する
			// 上と同じく歌詞の検索フラグが立っていなければスルー
			if(flag[1]){
				if(!matched){
					// 検索対象を読み込む
					// 曲名などの検索フラグに応じて微妙に処理が違う
					// 歌詞のうち最初の4行を飛ばすかどうかに関わる
					src = ryokaList.get(i);
					lyrics = readSongText(src[SongListActivity.SONG_FILENAME], 
											flag[0], 
											Integer.valueOf(src[SongListActivity.SONG_HEADER]));
					// 部分一致を探す
					for(int j = 0; j < searchLength; j++){
						if(lyrics.matches(".*" + wordList.get(j).toString() + ".*"))
						{
							result.add(i);
							// 次の検索ワードで重複で検索されるのを防ぐため break
							break;
						}
					}
				}
			}
		}

		return result;
	}	

	// 寮歌を検索し, 検索結果の通し番号の ArrayList を返す
	// 複数の検索ワードを持ったリストを用いた AND 検索
	private ArrayList<Integer> searchWordAnd(ArrayList<String> wordList, boolean[] flag)
	{
		// 検索結果を返すための変数. 該当する寮歌の番号を返す. 
		ArrayList<Integer> result = new ArrayList<Integer>();

		// 検索語リストの長さ
		int searchLength = wordList.size();
		// 検索対象の文字列を入れる変数
		String searchWord;

		// 最初の一回だけは普通に 1ワード検索
		searchWord = wordList.get(0);
		result = searchWord(wordList.get(0), flag);
		
		// 検索ワードを1つずつ取得して検索
		for(int i = 1; i < searchLength; i++){
			searchWord = wordList.get(i);
			result = searchWordAnd(searchWord, flag, result);
		}

		return result;
	}	

	// 寮歌を検索し, 検索結果の通し番号の ArrayList を返す
	// AND 検索の為に検索結果からさらに絞り込むバージョン
	private ArrayList<Integer> searchWordAnd(String word, boolean[] flag, ArrayList<Integer> list)
	{
		// 検索結果を返すための変数. 該当する寮歌の番号を返す. 
		ArrayList<Integer> result = new ArrayList<Integer>();

		// 検索対象の文字列を入れる変数
		String[] src;
		String lyrics;

		// 検索に引っかかったかを示すフラグ
		boolean matched = false;

		// 寮歌リスト全体にわたって検索
		for(int i = 0; i < list.size(); i++){
			// フラグを元に戻す
			matched = false;

			// 年, 作歌者, 作曲者, 曲名を探すループ
			// もしこれらの検索フラグが立っていなければスルーする
			if(flag[0]){
				for(int j = 0; j <= SongListActivity.SONG_TITLE; j++)
				{
					// 検索対象を読み込む
					src = ryokaList.get(list.get(i));
					// 部分一致を探す
					if(src[j].matches(".*" + word + ".*"))
					{
						// 結果をリストに入れる
						result.add(list.get(i));
						// 重複してカウントするのを避けるため, 一回見つければループを抜ける
						// break 後に歌詞を検索し始めないようにフラグも立てる
						matched = true;
						break;
					}
				}
			}

			// 歌詞の中を検索する
			// 上と同じく歌詞の検索フラグが立っていなければスルー
			if(flag[1]){
				if(!matched){
					// 検索対象を読み込む
					// 曲名などの検索フラグに応じて微妙に処理が違う
					// 歌詞のうち最初の4行を飛ばすかどうかに関わる
					src = ryokaList.get(list.get(i));
					lyrics = readSongText(src[SongListActivity.SONG_FILENAME], 
											flag[0], 
											Integer.valueOf(src[SongListActivity.SONG_HEADER]));
					// 部分一致を探す
					if(lyrics.matches(".*" + word + ".*"))
					{
						result.add(list.get(i));
					}
				}
			}
		}

		return result;
	}

	// txt ファイル読み込み処理
	// ここでは最初の4行は無視する
	// なぜなら最初の4行に年, 曲名, 作歌, 作曲者が入っているため
	private String readSongText(String fileName, boolean flag, int header)
	{
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
				// もし flag が false なら曲名等を検索しないので最初の4行を無視
				if(!flag){
					for(int i = 0; i < header; i++){
						br.readLine();
					}
				}
				// 残りを読み込み
				while((str = br.readLine()) != null){
					sb.append(str);
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
