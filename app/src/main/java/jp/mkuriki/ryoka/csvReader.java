package jp.mkuriki.ryoka;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.content.Context;
import android.content.res.AssetManager;

public class csvReader 
{
    /**********************************************
    * csv ファイルからデータを読み込み, List で返す
    * 先頭行はタイトルとして無視する. 
    *
    * @param context        コンテキスト
    * @param csvFileName   読み込む csv ファイル名
    * @return csv をString[] で返す ArrayList
    **********************************************/
    static public ArrayList<String[]> readCSV(Context context, String csvFileName)
    {
        InputStream is = null;
        BufferedReader br = null;
        ArrayList<String[]> csvList = new ArrayList<String[]>();
        String[] str = null;
 
        try
        {
            AssetManager as = context.getResources().getAssets();
            is = as.open(csvFileName);
            br = new BufferedReader(new InputStreamReader(is));
 
            // 一行目を無視するため, 一回だけ読む
            br.readLine();
 
            String line = "";
            // 最後まで読む
        	while((line = br.readLine()) != null)
            {
                str = line.split(",", -1);
                csvList.add(str);
            }
            br.close();
 
        } catch (FileNotFoundException e) {
            // File が見つからなかったときの例外. 
            e.printStackTrace();
        } catch (IOException e) {
            // BufferedReader が失敗したときの例外. 
            e.printStackTrace();
        }
 
        return csvList;
    }
}