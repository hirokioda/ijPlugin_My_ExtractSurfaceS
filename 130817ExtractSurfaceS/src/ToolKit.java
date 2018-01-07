import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ToolKit {
	static int[] getRGB(int c){    // Color pixel vluesからRGBの要素を取得
		int[] iArray= new int[3];
		iArray[0]  =  (c  &  0xff0000)  >>  16; // red 0-255
		iArray[1]  =  (c  &  0x00ff00)  >>  8;  // green 0-255
		iArray[2]  =  (c  &  0x0000ff);         // blue 0-255
		return iArray;
	}
// ------------------------------------------------WriteDataToFile------------------------------------------------
	static void WriteToFile(String comment, double[] u, double[] v, double[] w, String outfile) {  // ３つの変数の書き出す（コメントあり）
		try {					// フォルダがなかったり、ファイルへの書き込みができない場合には例外処理をする必要がある
			File file = new File(outfile);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(comment + "\n");
			for (int i = 0; i < u.length; i++) {
				bw.write(i+"\t"+u[i] + "\t" + v[i] + "\t" + w[i] + "\n");
			}
			bw.write("\n");
			bw.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}

	static void WriteToFile(double[]u, double[] v, double[] w, String outfile) {     // ３つの変数の書き出す（コメントなし）
		try {					// フォルダがなかったり、ファイルへの書き込みができない場合には例外処理をする必要がある
			File file = new File(outfile);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < u.length; i++) {
				bw.write(i+"\t"+u[i] + "\t" + v[i] + "\t" + w[i] + "\n");
			}
			bw.write("\n");
			bw.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}
	
	static void WriteToFile(String comment, double[] dist, double[] x, double[] y, double[] z, double[] u, double[] v, double[] w, String outfile) {  // ３つの変数の書き出す（コメントあり）
		try {					// フォルダがなかったり、ファイルへの書き込みができない場合には例外処理をする必要がある
			File file = new File(outfile);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(comment + "\n");
			for (int i = 0; i < u.length; i++) {
				bw.write(i+"\t"+dist[i]+"\t"+x[i] + "\t" + y[i] + "\t" + z[i] + "\t" +u[i] + "\t" + v[i] + "\t" + w[i] + "\n");
			}
			bw.write("\n");
			bw.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}
	
	static void WriteToFile(String comment, double u, double v, double w, double x,double y, String outfile) {  // ５つの変数の書き出す（コメントあり）
		try {					// フォルダがなかったり、ファイルへの書き込みができない場合には例外処理をする必要がある
			File file = new File(outfile);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(comment + "\n");
			bw.write(u + "\t" + v + "\t" + w + "\t" + x + "\t" + y + "\n");
			bw.write("\n");
			bw.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}
	
// ------------------------------------------------WriteDataToFile------------------------------------------------

// ------------------------------------------------ReadDataFromFile-----------------------------------------------
	static double[][] ReadFromFile_Doulbe(String cut, int n, String infile) { // cutは区切り文字をセット、nは１行当たりのデータ数
		double[][] iArray = new double[n][10000];  // ファイルから読み込む要素数が分からないので10000を最大としている。
		int numb=0;
		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File(infile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			System.out.println(infile);
			String rstr="";

			while((rstr=br.readLine()) != null){
				System.out.println(rstr);
				if(rstr.startsWith("#")==false){ // #で始まる行はコメント行として無視
					String[] suji = rstr.split(cut, 0);
					System.out.println(suji[0]+" "+suji[1]+" "+suji[2]);
					for (int i=0; i<n; i++){
						iArray[i][numb]=Double.parseDouble(suji[i]);
						System.out.println(iArray[i][numb]);
					}
					numb++;
				}
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
		// 値を戻すための配列を作り、それに入れ直し（要素数を一致させるため）
		double[][] rArray = new double[n][numb];
		for(int j=0; j<numb; j++){
			for(int i=0; i<n; i++){
				rArray[i][j]=iArray[i][j];
			}
		}
		System.out.println(rArray[0].length);
		return rArray;
	}

// ------------------------------------------------ReadDataFromFile-----------------------------------------------

	static void ReadFromFile_PixInfo(String infile){
		int elnumb=3;
//		double[] pix=new double[elnumb];
		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File(infile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String rstr="";
			while((rstr=br.readLine()) != null){
				if(rstr.startsWith("#pixinfo")==true){ // #pixinfoで始まる行に1pixelあたりの長さをx,y,zの順に入れておく
					String[] suji = rstr.split("\t", 0);
					System.out.println(suji[0]+" "+suji[1]+" "+suji[2]+" "+suji[3]);
					for (int i=0; i<elnumb; i++){
						puL[i]=Double.parseDouble(suji[i+1]);
						System.out.println(puL[i]);
					}
				}
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
	}


// ------------------------------------------------getPixPos-----------------------------------------------
	// real position -> pixelへの変換（z方向は１を足す必要あり)
	static double[] puL={1.0,1.0,1.0};  // pixunitlength (puL) 1ピクセル当たりの長さのデフォルトを設定--ReadFromFile_PixInfoで変更
	
	static double[] getPixPos(double x, double y, double z){
		int elnumb=3;
		double[] pixpos=new double[elnumb];
		pixpos[0]=x/puL[0];
		pixpos[1]=y/puL[1];
		pixpos[2]=z/puL[2]; // 取得すべきスライス番号とずれているので注意！
		return pixpos;
	}

	static double[][] getPixPos(double[] x, double[] y, double[] z){
		int elnumb=3;
		double[][] pixpos=new double[elnumb][x.length];
		for(int i=0; i<x.length; i++){
			pixpos[0][i]=x[i]/puL[0];
			pixpos[1][i]=y[i]/puL[1];
			pixpos[2][i]=z[i]/puL[2]; // 取得すべきスライス番号とずれているので注意！
		}
		return pixpos;
	}
	
	// スムージング
	static double[] smoothenArray(double[] s, int windowS){  // 配列sの数値をwindowをシフトしながら平均をとっていく; windowSはウインドウサイズ
		double[] av=new double[s.length];
		for(int i=0; i<s.length; i++){
			double sum=0;
			for(int j=i-windowS; j<=i+windowS;j++){
				int k;
				if(j<0){
					k=0;
				}
				else if(j>=s.length){
					k=s.length-1;
				}
				else {
					k=j;
				}
				sum=sum+s[k];
			}
			av[i]=sum/(windowS*2+1);
		}
		return av;
	}
	
	// 配列の足し算
	public static double[] addArray(double[] s1, double[] s2){
		double [] sum=new double [s1.length];
		for(int i=0; i<s1.length; i++){
			sum[i]=s1[i]+s2[i];
		}
		return sum;
	}
	// 配列の各要素の割り算
	public static double[] divArray(double[] s, double d){
		double [] div=new double [s.length];
		for(int i=0; i<s.length; i++){
			div[i]=s[i]/d;
		}
		return div;
	}
}
