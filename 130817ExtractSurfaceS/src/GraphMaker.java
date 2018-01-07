import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

//  >>> 配列数値データを使ってグラフを描くための汎用クラス <<<
public class GraphMaker{
	private int Width, Height;   // 座標変換用定数
	private float Nxmin, Nymin;
	private float xmin, xmax, ymin, ymax, rx, ry;
	private Graphics2D g;
	private float sx=0.75F, sy=0.75F;  // グラフの周囲に設ける空白割合
	//  コンストラクト（クラスの初期化）
	public GraphMaker(int width, int height, Graphics2D g){
		Width=width; Height=height; this.g=g;
	}
	// 上下配置と数値範囲を指定してグラフ枠をセットするメソッド
	public void setGraphFrame(int Nup, int Nbot, boolean aspect, double[] range){
		xmin=(float)range[0]; xmax=(float)range[1];
		ymin=(float)range[2]; ymax=(float)range[3];
		setFrame(Nup, Nbot, aspect);
	}
	public void clearImage(){  // p.177 Program 9.3参照
		g.setColor(Color.white); g.fillRect(0, 0, Width-1, Height-1);
	}
	/* >>> 上下配置と数値範囲を指定してグラフ枠をセットするメソッド <<<
		Nup, Nbottom = アプレット上部、下部の非描画慮域のピクセル値
		aspect=縦横比を保存する場合はtrue, 非保存の場合はfalse  */
	void setFrame(int Nup, int Nbottom, boolean aspect){
		float ap=(ymax-ymin)/(xmax-xmin); // 縦横比
		float aw=(float)(Height-Nup-Nbottom)*sy/((float)Width*sx);
		rx=(float)Width*sx/(xmax-xmin);  // 図示倍率
		ry=(float)(Height-Nup-Nbottom)*sy/(ymax-ymin);
		Nxmin=(float)(Width*(1F-sx)/2);
		Nymin=(float)Nbottom+((float)(Height-Nup-Nbottom)*(1F-sy))/2;
		if(aspect == true){   // 縦横比を保持して図示する場合
			if(ap>aw){
				Nxmin += ((1F-ry/rx)*(float)Width*sx)/2;
				rx=ry;
			}else{
				Nymin += ((1F-rx/ry)*(float)Height*sy)/2;
				ry=rx;
			}
		}
	}
	// x座標を変換するためのメソッド
	private float xtr(double x){
		return rx*((float)x-xmin)+(float)Nxmin;
	}
	private float xtr(float x){
		return rx*(x-xmin)+(float)Nxmin;
	}
	private float xtr(int x){
		return rx*((float)x-xmin)+(float)Nxmin;
	}
	// y座標を変換するためのメソッド
	private float ytr(double y){
		return (float)(Height-Nymin)-ry*((float)y-ymin);
	}
	private float ytr(float y){
		return (float)(Height-Nymin)-ry*(y-ymin);
	}
	private float ytr(int y){
		return (float)(Height-Nymin)-ry*(y-ymin);
	}
	// Path2Dを使って線プロットを描く基本メソッド（クラス内で使用）
	private void polyline(float x[], float y[], int N, Color col, Color colp, float pensize){
		Path2D p = new Path2D.Float();
		p.moveTo(x[0], y[0]);
		for(int i=1; i<N; i++) p.lineTo(x[i], y[i]);
		BasicStroke s1=new BasicStroke(pensize);
		g.setStroke(s1);
        g.draw(p);
	}
	/* >>> 配列数値データを折れ線表示し、データ点に丸印を描くメソッド <<<
	 * x[],y[]=プロットするデータの座標値；N=データ数
	 * col=折れ線の色；colp=データ点の色、nullの場合は点を描かない  */

	public void plotData(float[] x, float[] y, int N, Color col, Color colp, float pensize){  // x, y float
		float xp[]=new float[N], yp[]=new float[N];
		g.setColor(col);    // 折れ線の色を設定
		for(int i=0; i<N; i++){
			xp[i]=xtr(x[i]); yp[i]=ytr(y[i]);
		}
		polyline(xp,yp,N, col, colp, pensize);  // 折れ線
		// colp=nullの場合はデータ点を●印で表示しない
		if(colp != null){
			int d0=(int)(0.016F*(float)Width); // 丸印直径はWidthの1.6%
			if(d0<2) d0=2; // 丸印の最小は２ピクセル
			int r0=d0/2;  // 丸印の半径
			g.setColor(colp); // データ点につける丸印の色を設定
			for(int i=0; i<N; i++){
				g.fillOval((int)(xp[i]-r0), (int)(yp[i]-r0), d0, d0);
			}
		}
	}
	public void plotData(double[] x, double[] y, int N, Color col, Color colp, float pensize){  // x, y double
		float xp[]=new float[N], yp[]=new float[N];
		g.setColor(col);    // 折れ線の色を設定
		for(int i=0; i<N; i++){
			xp[i]=xtr(x[i]); yp[i]=ytr(y[i]);  // floatにキャスト
		}
		polyline(xp,yp,N, col, colp, pensize);  // 折れ線
		// colp=nullの場合はデータ点を●印で表示しない
		if(colp != null){
			int d0=(int)(0.016F*(float)Width); // 丸印直径はWidthの1.6%
			if(d0<2) d0=2; // 丸印の最小は２ピクセル
			int r0=d0/2;  // 丸印の半径
			g.setColor(colp); // データ点につける丸印の色を設定
			for(int i=0; i<N; i++){
				g.fillOval((int)(xp[i]-r0), (int)(yp[i]-r0), d0, d0);
			}
		}
	}
	public void plotData(double[] y, int N, Color col, Color colp, float pensize){  // xはなし(0-N)、y double
		float xp[]=new float[N], yp[]=new float[N];
		g.setColor(col);    // 折れ線の色を設定
		for(int i=0; i<N; i++){
			xp[i]=xtr(i); yp[i]=ytr(y[i]);  // floatにキャスト
		}
		polyline(xp,yp,N, col, colp, pensize);  // 折れ線
		// colp=nullの場合はデータ点を●印で表示しない
		if(colp != null){
			int d0=(int)(0.016F*(float)Width); // 丸印直径はWidthの1.6%
			if(d0<2) d0=2; // 丸印の最小は２ピクセル
			int r0=d0/2;  // 丸印の半径
			g.setColor(colp); // データ点につける丸印の色を設定
			for(int i=0; i<N; i++){
				g.fillOval((int)(xp[i]-r0), (int)(yp[i]-r0), d0, d0);
			}
		}
	}
	public void plotData(double[][] x, int y, int N, Color col, Color colp, float pensize){  //  double[][y], int y
		float xp[]=new float[N], yp[]=new float[N];
		g.setColor(col);    // 折れ線の色を設定
		for(int i=0; i<N; i++){
			xp[i]=xtr(i); yp[i]=ytr(x[i][y]);  // floatにキャスト
		}
		polyline(xp, yp, N, col, colp, pensize);  // 折れ線
		// colp=nullの場合はデータ点を●印で表示しない
		if(colp != null){
			int d0=(int)(0.016F*(float)Width); // 丸印直径はWidthの1.6%
			if(d0<2) d0=2; // 丸印の最小は２ピクセル
			int r0=d0/2;  // 丸印の半径
			g.setColor(colp); // データ点につける丸印の色を設定
			for(int i=0; i<N; i++){
				g.fillOval((int)(xp[i]-r0), (int)(yp[i]-r0), d0, d0);
			}
		}
	}

	public void plotSpot(double x, double y, Color col, float spotsize){ // 座標(x,y)にspotを表示
		int d0=(int)(0.016F*(float)Width*spotsize); // 丸印直径はWidthの1.6%
		if(d0<2) d0=2; // 丸印の最小は２ピクセル
		int r0=d0/2;  // 丸印の半径
		g.setColor(col); // データ点につける丸印の色を設定
		g.fillOval((int)(xtr(x)-r0), (int)(ytr(y)-r0), d0, d0);
	}

	public void barData(double x, double y, Color col, float pensize){
		float xp[]=new float[2], yp[]=new float[2];
		g.setColor(col);   // 棒の色を設定
		xp[0]=xtr(x);xp[1]=xp[0];
		yp[0]=ytr(0);yp[1]=ytr(y);
		polyline(xp, yp, 2, col, null, pensize);
	}
	public void barData(double[] y, int N, Color col, float pensize){
		float xp[]=new float[2], yp[]=new float[2];
		g.setColor(col);
		for(int i=0; i<N;i++){
			xp[0]=xtr(i);xp[1]=xp[0];
			yp[0]=ytr(0);yp[1]=ytr(y[i]);
			polyline(xp, yp, 2, col, null, pensize);
		}
	}
	/** >>> 座標軸を描くメソッド <<<
	 *	strx=x軸につける変数名、xdiv=x軸の目盛りの数
	 *	stry=y軸につける変数名、ydiv=y軸の目盛りの数   */
	public void drawAxis(String strx, int xdiv, String stry, int ydiv){
		g.setColor(Color.black);
		Font font=new Font("TimesRoman", Font.PLAIN, 12);
		Font fontS=new Font("TimesRoman", Font.ITALIC, 12);
		g.setFont(font);
		FontMetrics fm=g.getFontMetrics(); // フォントの寸法を取得
		int strW, strH =fm.getHeight();
		int xs, ys; String str; float value;
		int m=(int)(0.01F*(float)Width); // 目盛り線長さ
		if(m==0) m=2;
		// 軸線描画時の線幅設定
		BasicStroke s1=new BasicStroke(1f);
		g.setStroke(s1);
		// x軸を描画
		xs=(int)xtr(xmax); ys=(int)ytr(0.0F);
		g.drawLine((int)xtr(xmin), ys, xs, ys);
		g.setFont(fontS);
		g.drawString(strx, xs+5, ys-strH/2); // x軸につける記号
		g.setFont(font);
		for(int i=0; i<=xdiv; i++){
			value=xmin+(float)i*(xmax-xmin)/(float)xdiv; // 目盛り値
			str=roundValue(value);  // 数値を文字列に変換
			strW=fm.stringWidth(str);  // 文字列の全幅を取得
			xs=(int)xtr(value); ys=(int)ytr(0.0F);
			g.drawLine(xs, ys-m, xs, ys+m);  // 目盛り線
			g.drawString(str, xs-strW/2, ys-m+strH+2);  //  数値記入(補正）
//			g.drawString(str, xs-strW/2, ys-m+strH+9);  //  数値記入
		}
		//  y軸を描画
		xs=(int)xtr(0.0F); ys=(int)ytr(ymax);
		g.drawLine(xs, (int)ytr(ymin), xs, ys);
		g.setFont(fontS);
		g.drawString(stry, xs-fm.stringWidth(stry)/2, ys-strH+7);
		g.setFont(font);
		for(int i=0; i<=ydiv; i++){    // y軸の目盛り・数値の描画
			value=ymin+(float)i*(ymax-ymin)/(float)ydiv;
			str=roundValue(value);
			strW=fm.stringWidth(str);
			xs=(int)xtr(0.0F); ys=(int)ytr(value);
			g.drawLine(xs-m, ys, xs+m, ys);
			g.drawString(str, xs-strW-9, ys+strH/4); //  数値記入（補正）
//			g.drawString(str, xs-strW-9, ys+strH/2); // 数値記入
		}
	}
	//  パラメータをグラフ内に記録するためのメソッド
	public void drawStr(int fontsize, String str1){
		g.setColor(Color.black);
		Font font=new Font("TimesRoman", Font.PLAIN, fontsize);
		g.setFont(font);
		g.drawString(str1, Width/6, 50);
	}
	public void drawStr(int fontsize, String str1, String str2){
		g.setColor(Color.black);
		Font font=new Font("TimesRoman", Font.PLAIN, fontsize);
		g.setFont(font);
		FontMetrics fm=g.getFontMetrics(); // フォントの寸法を取得
		int strH =fm.getHeight();
		g.drawString(str1, Width/6, 50);
		g.drawString(str2, Width/6, strH*3/5+50);
	}
	public void drawStr(int fontsize, String str1, String str2, String str3){
		g.setColor(Color.black);
		Font font=new Font("TimesRoman", Font.PLAIN, fontsize);
		g.setFont(font);
		FontMetrics fm=g.getFontMetrics(); // フォントの寸法を取得
		int strH =fm.getHeight();
		g.drawString(str1, Width/6, 50);
		g.drawString(str2, Width/6, strH*3/5+50);
		g.drawString(str3, Width/6, strH*3*2/5+50);
	}
	public void drawStr(int fontsize, String str1, String str2, String str3, String str4){
		g.setColor(Color.black);
		Font font=new Font("TimesRoman", Font.PLAIN, fontsize);
		g.setFont(font);
		FontMetrics fm=g.getFontMetrics(); // フォントの寸法を取得
		int strH =fm.getHeight();
		g.drawString(str1, Width/6, 50);
		g.drawString(str2, Width/6, strH*3/5+50);
		g.drawString(str3, Width/6, strH*3*2/5+50);
		g.drawString(str4, Width/6, strH*3*3/5+50);
	}
	public void drawStr(int fontsize, String str1, String str2, String str3, String str4, String str5, String str6){
		g.setColor(Color.black);
		Font font=new Font("TimesRoman", Font.PLAIN, fontsize);
		g.setFont(font);
		FontMetrics fm=g.getFontMetrics(); // フォントの寸法を取得
		int strH =fm.getHeight();
		g.drawString(str1, Width/6, 50);
		g.drawString(str2, Width/6, strH*3/5+50);
		g.drawString(str3, Width/6, strH*3*2/5+50);
		g.drawString(str4, Width/6, strH*3*3/5+50);
		g.drawString(str5, Width/6, strH*3*4/5+50);
		g.drawString(str6, Width/6, strH*3*5/5+50);
	}
	//  有効数字４桁(float),８桁(double)に丸めるメソッド
	public String roundValue(float x){
		x=Math.round(x*10000F);
		if(x>=0F) x/=10000F; else x*=0.0001F;
		String str= ""+x;
		int desimalP=str.indexOf(".");  // ピリオドの位置を求める
		// 末尾が.0に等しい場合、これを除いた上位の数値を表示
		if(str.endsWith(".0")) return str.substring(0, desimalP);
		return str;
	}
	public String roundValue(double x){
		x=Math.round(x*100000000F);
		if(x>=0F) x/=100000000F; else x*=0.00000001F;
		String str= ""+x;
		int desimalP=str.indexOf(".");  // ピリオドの位置を求める
		// 末尾が.0に等しい場合、これを除いた上位の数値を表示
		if(str.endsWith(".0")) return str.substring(0, desimalP);
		return str;
	}
}