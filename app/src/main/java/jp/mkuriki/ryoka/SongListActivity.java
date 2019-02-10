package jp.mkuriki.ryoka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import net.nend.android.*;

public class SongListActivity extends Activity {

	// intent 用定数
	public static final int SHOW_SONG_VIEW = 1;
	public static final int SHOW_SONG_VIEW_WITH_LIST = 2;
	// 寮歌リスト定数
	public static final int SONG_YEAR = 0;
	public static final int SONG_SONG = 1;
	public static final int SONG_MUSIC = 2;
	public static final int SONG_TITLE = 3;
	public static final int SONG_FILENAME = 4;
	public static final int SONG_FLAG = 5;
	public static final int SONG_HEADER = 6;

	private ArrayList<Integer> searchResult = null;
	private ArrayList<String[]> songList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.songlist);

		// SharedPreferences の準備
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// 広告表示がオフかどうかチェック
		if((sp.getInt("password", 0) == 1) || (sp.getLong("adTime", 0) > System.currentTimeMillis())){
			// 広告を消す処理
			LinearLayout adLayout = (LinearLayout)this.findViewById(R.id.songListLayout);
			NendAdView adView2 = (NendAdView)this.findViewById(R.id.nend2);
			adLayout.removeView((View)adView2);
		}

		
		ArrayList<Map<String, String>> ryokaList = null;
		songList = csvReader.readCSV(this, "ryoka_list.csv");

		// intent を取得
		final Intent intent = getIntent();
		
		// 受け取った intent が null じゃなく, 検索結果が入っているかどうかで場合分け
		if(intent != null){
			searchResult = intent.getIntegerArrayListExtra("jp.mkuriki.ryoka.SongListActivity.result");
			if((searchResult != null) && (!searchResult.isEmpty())){
				ryokaList = makeList(songList, searchResult);
			}
			else{
				ryokaList = makeList(songList);
			}
		}
		else{
			// csv ファイルから寮歌リスト作成
			ryokaList = makeList(songList);
		}
		
		// 曲目追加
		SimpleAdapter sAdapter = new SimpleAdapter(
				this, ryokaList, android.R.layout.simple_list_item_2, 
				new String[] {"title", "creator"}, 
				new int[] {android.R.id.text1, android.R.id.text2}
		);

		// ListView にアダプタを設定
		ListView list = (ListView) findViewById(R.id.songList);
		list.setAdapter(sAdapter);

		// リストビューのアイテムがクリックされたときの挙動を設定
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

				// どの曲を選んだか保存する
				// 検索結果のあるなしで場合分け
				if((searchResult != null) && (!searchResult.isEmpty())){
					// activity_song を起動する
					startSongActivity(searchResult.get(pos), searchResult);
				}
				else{
					// activity_song を起動する
					startSongActivity(pos);
				}
			}
		});

	}

	// 歌詞表示用の activity_song 画面に遷移
	public void startSongActivity(int songNum) {
		Intent intent = new Intent(this, SongView.class);
		intent.putExtra("songFileNum", songNum);
		startActivityForResult(intent, SHOW_SONG_VIEW);
	}
	// 歌詞表示用の activity_song 画面に遷移:検索結果の Integer リストを渡すバージョン
	public void startSongActivity(int songNum, ArrayList<Integer> list) {
		Intent intent = new Intent(this, SongView.class);
		intent.putIntegerArrayListExtra("searchResult", list);
		intent.putExtra("songFileNum", songNum);
		startActivityForResult(intent, SHOW_SONG_VIEW_WITH_LIST);
	}

	// 歌詞表示用の Activity から戻ってきたときの処理
	@Override
	public void onActivityResult(int request, int result, Intent data)
	{
		// 検索結果なしの場合
		if(request == SHOW_SONG_VIEW){
			// GO_HOME ならこの Activity も終了
			if(result == SongView.GO_HOME){
				finish();
			}
			// GO_NEXT なら次の寮歌を表示
			// GO_HOME が適切に動作するように SongList から改めて呼び出すようにする. 
			if(result == SongView.GO_NEXT){
				startSongActivity(data.getIntExtra("songNextNum", 0));
			}
			// GO_PREV なら前の寮歌を表示
			// GO_HOME が適切に動作するように SongList から改めて呼び出すようにする. 
			if(result == SongView.GO_PREV){
				startSongActivity(data.getIntExtra("songPrevNum", 0));
			}
		}
		// 検索結果ありの場合
		if(request == SHOW_SONG_VIEW_WITH_LIST){
			// GO_HOME ならこの Activity も終了
			if(result == SongView.GO_HOME){
				// intent に GO_HOME を渡す
				setResult(SongView.GO_HOME, getIntent());
				finish();
			}
			// GO_NEXT なら次の寮歌を表示
			// GO_HOME が適切に動作するように SongList から改めて呼び出すようにする. 
			if(result == SongView.GO_NEXT){
				startSongActivity(data.getIntExtra("songNextNum", 0), searchResult);
			}
			// GO_PREV なら前の寮歌を表示
			// GO_HOME が適切に動作するように SongList から改めて呼び出すようにする. 
			if(result == SongView.GO_PREV){
				startSongActivity(data.getIntExtra("songPrevNum", 0), searchResult);
			}
		}
	}
	
	// csv ファイルを読み込んだ List からタイトル, 作歌, 作曲者を抜き出す
	// 曲目リストに載せるため, SimpleAdapter に投げられる形の List を返す
	private ArrayList<Map<String, String>> makeList(List<String[]> srcList)
	{
		// 年, 作歌者, 作曲者, 寮歌名を取得するための String 配列
		String[] str;
		ArrayList<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		for(int i = 0; i < srcList.size(); i++)
		{
			// str に読み出す i 行目の要素をすべて入れる
			str = srcList.get(i);
			dataList.add(mapRyoka(str));
		}
		
		return dataList;
	}
	
	// csv ファイルを読み込んだ List からタイトル, 作歌, 作曲者を抜き出す
	// 曲目リストに載せるため, SimpleAdapter に投げられる形の List を返す
	private ArrayList<Map<String, String>> makeList(List<String[]> srcList, ArrayList<Integer> result)
	{
		// 年, 作歌者, 作曲者, 寮歌名を取得するための String 配列
		String[] str;
		ArrayList<Map<String, String>> dataList = new ArrayList<Map<String, String>>();

		for(int i = 0; i < result.size(); i++)
		{
			// result にあるものだけでリストをつくる
			// str に読み出す i 行目の要素をすべて入れる
			str = srcList.get(result.get(i));
			dataList.add(mapRyoka(str));
		}

		return dataList;

	}
	
	// 寮歌のタイトルや作家作曲者を組み合わせた HashMap を返す関数
	private Map<String, String> mapRyoka(String[] src)
	{
		// 返す Map を用意
		Map<String, String> map = new HashMap<String, String>();
		
		// フラグ 0 : 校歌
		if(src[SONG_FLAG].equals("0")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "君作歌 " + src[SONG_MUSIC] + "氏選曲");
		}
		// フラグ 1 : 年度あり, タイトルあり, 作歌, 作曲者あり 君付け表示 普通の歌
		else if(src[SONG_FLAG].equals("1")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			// 作曲者がいるかいないか (同一かどうか) で場合分け
			if(!src[SONG_MUSIC].equals("")){
				map.put("creator", src[SONG_SONG] + "君作歌 " + src[SONG_MUSIC] + "君作曲");
			}
			else{
				map.put("creator", src[SONG_SONG] + "君作歌・作曲");
			}
		}
		// フラグ 2 : 年度なし, タイトルあり, 作歌, 作曲者あり 君付け表示 各部歌など
		else if(src[SONG_FLAG].equals("2")){
			map.put("title", src[SONG_TITLE]); // 体裁のため改行を入れる
			// 作曲者がいるかいないかで場合分け
			if(!src[SONG_MUSIC].equals("")){
				map.put("creator", src[SONG_SONG] + "君作歌 " + src[SONG_MUSIC] + "君作曲");
			}
			else{
				map.put("creator", src[SONG_SONG] + "君作歌・作曲");
			}
		}
		// フラグ 3 : 年度あり, タイトルあり, 作歌, 作曲者なし 一部桜星歌など
		else if(src[SONG_FLAG].equals("3")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			map.put("creator", "");
		}
		// フラグ 4 : 年度なし, タイトルあり, 作歌, 作曲者なし 水産放浪歌など
		else if(src[SONG_FLAG].equals("4")){
			map.put("title", src[SONG_TITLE]); // 体裁のため改行を入れる
			map.put("creator", "");
		}
		// フラグ 5 : 年度あり, タイトルあり, 作歌, 作曲者あり 作曲は氏表示 清き郷石狩の
		else if(src[SONG_FLAG].equals("5")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "君作歌 " + src[SONG_MUSIC] + "氏作曲");
		}
		// フラグ 6 : 年度なし, タイトルあり, Prof. / 管弦楽部 作歌・作曲 STUDENLIED, 昭和6年応援歌
		else if(src[SONG_FLAG].equals("6")){
			map.put("title", src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "作歌・作曲");
		}
		// フラグ 7 : 年度あり, タイトルあり, Prof. 作歌・作曲 College Hymn など
		else if(src[SONG_FLAG].equals("7")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "作歌・作曲");
		}
		// フラグ 8 : 年度なし, タイトルあり, 先生作歌, 君作曲 昭和7年応援歌
		else if(src[SONG_FLAG].equals("8")){
			map.put("title", src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "先生作歌 " + src[SONG_MUSIC] + "君作曲");
		}
		// フラグ 9 : 年度あり, タイトルあり, 氏作歌, 氏作曲 水産専門部歌
		else if(src[SONG_FLAG].equals("9")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "氏作歌 " + src[SONG_MUSIC] + "氏作曲");
		}
		// フラグ 10 : 年度あり, タイトルあり, 氏作歌, 学校作曲 函館水産高校校歌
		else if(src[SONG_FLAG].equals("10")){
			map.put("title", src[SONG_YEAR] + "\n" + src[SONG_TITLE]);
			map.put("creator", src[SONG_SONG] + "氏作歌 " + src[SONG_MUSIC] + "作曲");
		}
		
		return map;
	}

}
