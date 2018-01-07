import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class My_ExtractSurfaceS implements PlugInFilter {
	ImagePlus imp;
	public int setup(String arg, ImagePlus imp) {
		if (imp == null) {
			IJ.noImage();
			return DONE;
		}
		this.imp = imp;
		return DOES_ALL;
	}

	public void run (ImageProcessor ip) {   // 一番目のチャネルにdapiのsmoothingしたファイルをセットしておく
		if(imp.getType()!=0)
		{IJ.error("File type should be 8 bit!!"); return;} // 8 bit でなく、複数チャネルに対応

		int w = imp.getWidth();        // 幅
		int h = imp.getHeight();       // 高さ
		int nCh = imp.getNChannels();  // カラーチャネル
		int nSl = imp.getNSlices();    // zスライス
		int nFr = imp.getNFrames();    // タイムポイント

		if(nCh<2 || nSl<2)
		{IJ.error("Multi-channel stack file is required!!"); return;}
		if(w>1024 || h>1024 || nSl>60)
		{IJ.error("Image stack size is too large!!\nMaximum is 1024x1024x60"); return;}

		ImageStack[] stack = getEachStack(imp); //stackをchannelごとに分割（このクラスのメソッド、下を見よ）

		Zprocess zps= new Zprocess(stack, w, h, nCh, nSl, nFr); // Zprocessクラスのコンストラクタ作成。

		// -------------------------------------------------------------------------------------------------------- 8/17/13
		int stdMaxPercent=100;  // DapiのMax値の何パーセントのところを基準値にするか
		int reBase=0;         // basal端の基準値からの相対的位置を指定
		int exThick=1;        // シグナルを抽出する領域の厚み（apical端までの距離）
		int cutOff=0;         // 考慮するdapiシグナルの最小値

		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File("c:/nsworkspace/imageJ/_ESetting.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String rstr="";
			String cut=",";
			while((rstr=br.readLine()) != null){
				if(rstr.startsWith("#")==false){ // #で始まる行はコメント行として無視。それ以外の行からデータを取得。
					if(rstr.startsWith("//")==true || rstr.startsWith("\n")==true || rstr.startsWith(cut))break;  // 130324加えた。
					if(rstr.startsWith("stdMaxPercent")==true){  // stdMaxPercentの初期設定
						String[] suji = rstr.split(cut, 0);
						stdMaxPercent=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("reBase")==true){  // reBaseの初期設定
						String[] suji = rstr.split(cut, 0);
						reBase=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("exThick")==true){  // exThickの初期設定
						String[] suji = rstr.split(cut, 0);
						exThick=Integer.parseInt(suji[1]);
					}
					if(rstr.startsWith("cutOff")==true){  // cutOffの初期設定
						String[] suji = rstr.split(cut, 0);
						cutOff=Integer.parseInt(suji[1]);
					}
				}
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
		// --------------------------------------------------------------------------------------------------------- 8/17/13

		// 描画フレームの設定
		JFrame frame=new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle("Graph1");
		frame.setSize(650,450);

// ImageStackの第一チャネルからイメージデータを取得
		zps.getImageStackData(0);     // スムージングしたDAPIのシグナルがチャネル０に入っていることを想定している。dapi[i][j][k]にデータを収納。
		zps.getSurfaceRange(stdMaxPercent);  // 基準値の取得。max value x stdMaxPercent%以上の値を最初に取る配列インデックス(30%が適当)  8/17/13 baseZ[i][j]に基準値を収納。

	for(int k=0;k<4;k++){
//		--------------------------------------------------------------------
// 新しいウインドウをを作ってグラフを表示

		BufferedImage image=new BufferedImage(300,200,BufferedImage.TYPE_INT_ARGB);  // TYPE_INT_ARGB,背景透明;TYPE_INT_BGR,背景黒
		Graphics2D g2=(Graphics2D)image.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		GraphMaker gt1=new GraphMaker(image.getWidth(), image.getHeight(), g2);

		gt1.clearImage();
		double[] range={0, nSl, 0, 80}; //  表示範囲{xmin, xmax, ymin, ymax}
		gt1.setGraphFrame(40,20,false, range); // 座標変換係数の計算
		gt1.drawAxis("x", 5, "y", 8); // 座標軸の描画
		/*
		gt1.drawStr(11,    //  パラメータの値の表示(フォントサイズ指定)
				"RGB intensity profiles (121122)",
				"UnitLength= "+mp.unitVL+" micro meter",
				"LineThickness= "+mp.thLine*mp.unitVL*2+" micro meter",
				"SmoothenWindowSize= "+smoothenWindowSize*2+" micro meter");
		*/
		double[] prf=new double[zps.nSlices]; double[] rslt=new double[2];
		prf=zps.getZprofile(400+k*10,200);
		rslt=MyMath.getFirstPercentMax(prf, zps.nSlices, stdMaxPercent, false);  // false, from Bottom
		gt1.barData(prf, zps.nSlices, Color.BLUE, 2.0F );
		gt1.plotSpot(rslt[0], rslt[1], Color.BLUE, 2.0F);
		System.out.println(rslt[0]+" "+rslt[1]+" "+" "+nSl);
		gt1.drawStr(11,    //  パラメータの値の表示(フォントサイズ指定)
				"Values",
				"rslt_0bule= "+rslt[0]+" ",
				"rslt_1blue= "+rslt[1]+" ",
				"nSlices= "+nSl+" ");


		prf=zps.getZprofile(400+k*10,400);
		rslt=MyMath.getFirstPercentMax(prf, zps.nSlices, stdMaxPercent, false);
		gt1.barData(prf, zps.nSlices, Color.RED, 1.5F );
		gt1.plotSpot(rslt[0], rslt[1], Color.RED, 2.0F);

		prf=zps.getZprofile(400+k*10,600);
		rslt=MyMath.getFirstPercentMax(prf, zps.nSlices, stdMaxPercent, false);
		gt1.barData(prf, zps.nSlices, Color.GREEN, 1.0F );
		gt1.plotSpot(rslt[0], rslt[1], Color.GREEN, 2.0F);


		// 描画内容
		g2.dispose();
		JLabel label1=new JLabel(new ImageIcon(image));
		frame.add(label1);
	}
//		---------------------------------------------------------------------
		// 描画フレームの表示
//		frame.setVisible(true);                                                                             //  ここをオンにするとグラフを表示する。
//		---------------------------------------------------------------------

	// Surface以外の部分を黒く塗りつぶす処理を行う。
		for(int i=1; i<zps.nCh; i++){
			zps.extractSurfaceRange(i, reBase, exThick, cutOff);    //  一番奥のイメージから手前に動かしてMaxの30%を超えた位置を基準として、相対的な位置でsurface rangeを指定。ファイルからパラメーター設定 cutOff 8/17/13
		}
	}
// -----------------------------------------------------------------------------
	//stackをchannelごとに分割し配列へ by 逸見さん
	ImageStack[] getEachStack(ImagePlus imp) {
		int nCh = imp.getNChannels();
		ImageStack[] stack = new ImageStack[nCh];
		for (int i = 0; i < nCh; i++) {
			stack[i] = getMultiChannel(imp, i + 1);
		}
		return stack;
	}

	//指定したchannelのstackを取得(1 <= c) by 逸見さん
	ImageStack getMultiChannel(ImagePlus imp, int ch) {
		ImageStack stackWhole = imp.getImageStack();
		ImageStack stackEach = new ImageStack(imp.getWidth(), imp.getHeight());
		int index;
		for (int nf = 1; nf <= imp.getNFrames(); nf++) {   // time points
			for (int ns = 1; ns <= imp.getNSlices(); ns++) {  // z slices
				index = imp.getStackIndex(ch, ns, nf);
				stackEach.addSlice(stackWhole.getProcessor(index));
			}
		}
		return stackEach;
	}
}