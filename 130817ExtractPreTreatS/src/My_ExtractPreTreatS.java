import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// 最初のチャネルがdapiであることを想定して、そのチャネルをコピーし、スムージングして、先頭に加える。チャネル数はひとつ増える。
// 表面の細胞層のシグナルを抽出するためのプラグイン（ExtractSurface)の前処理として行う。
// Macro"Treat_All_StacksIDS_with_ExtractPreTreat"と組み合わせると、フォルダーのすべてのidsファイルを処理できる。
public class My_ExtractPreTreatS implements PlugInFilter {
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
		{IJ.error("Image stack size is too large!!\nMaximum is 1024x1024x50"); return;} 
		
		
		// -------------------------------------------------------------------------------------------------------- 8/17/13
		double smRadius=20.0;         // smoothingの半径
		int dapiCh=0;
		
		try {					// フォルダがなかったり、ファイルからの読み込みができない場合に例外処理を行う
			File file = new File("c:/nsworkspace/imageJ/_ESetting.txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String rstr="";
			String cut=",";
			while((rstr=br.readLine()) != null){
				if(rstr.startsWith("#")==false){ // #で始まる行はコメント行として無視。それ以外の行からデータを取得。
					if(rstr.startsWith("//")==true || rstr.startsWith("\n")==true || rstr.startsWith(cut))break;  // 130324加えた。
					if(rstr.startsWith("smRadius")==true){  // smRadiusの初期設定  8/17/13
						String[] suji = rstr.split(cut, 0);
						smRadius=Double.parseDouble(suji[1]);
					}
					if(rstr.startsWith("dapiCh")==true){  // dapiChの初期設定  8/17/13
						String[] suji = rstr.split(cut, 0);
						dapiCh=Integer.parseInt(suji[1]);
					}
				}
			}
			br.close();
		} catch (IOException e) {	// 例外処理
			System.out.println(e);
		}
		// --------------------------------------------------------------------------------------------------------- 8/17/13
		
		
		ImagePlus imp_f;
		ImageStack stack_f;
		
		RankFilters rf=new RankFilters();
		imp_f=NewImage.createByteImage("check", w, h, nSl, NewImage.FILL_BLACK);
		stack_f=getDuplicate(imp, dapiCh, imp_f);                       // 0は一番目のSliceにdapiがあることを意味する。dapiCh 8/17/13
		copyCalibration(imp, imp_f);
		
		for(int i=0; i<nSl;i++){
			rf.rank(stack_f.getProcessor(i+1), smRadius, RankFilters.MEAN);
		}
		
//		imp_f.show();
		
		ImagePlus imp_hyp;
		imp_hyp=IJ.createHyperStack("Output", w, h, nCh+1, nSl, nFr, 8);
		if(getCombine(imp,imp_f,imp_hyp)==false)
		{IJ.error("The hyperstack created is not appropriate!"); return;}
		copyCalibration(imp, imp_hyp);

		imp_hyp.show();
		
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
	
	//指定したchannelのstackをduplicateしてimp2に書き込んでimageStackとして返す。 by Hiroki Oda   2013.4.5
	ImageStack getDuplicate(ImagePlus imp1, int ch, ImagePlus imp2){
		ImageStack stackCh = getMultiChannel(imp1, ch);
		ImageStack stackDuplicate = imp2.getImageStack();
		ImageProcessor ip1, ip2;
		
		int value;
		for (int ns = 1; ns <= imp1.getNSlices(); ns++) {  // z slices
			ip1=stackCh.getProcessor(ns);
			ip2=stackDuplicate.getProcessor(ns);
			for(int j=0; j<imp1.getHeight(); j++){
				for(int i=0; i<imp1.getWidth();i++){
					value=ip1.getPixel(i,j);
					ip2.putPixel(i, j, value);
				}
			}
		}
		return stackDuplicate;
	}
	
	// Multichannel hyperstackとsingle channel stackを統合して新たなhyperstackを作る。 by Hiroki Oda   2013.4.5
	public boolean getCombine(ImagePlus imp_h1, ImagePlus imp_0, ImagePlus imp_h2){
		ImageStack[] stack_h1=getEachStack(imp_h1);
		ImageStack stack_0=imp_0.getImageStack();
		ImageStack[] stack_h2=getEachStack(imp_h2);
		ImageProcessor ip_h1, ip_0, ip_h2;
		
		int ch1, value;
		ch1=imp_h1.getNChannels();
		
		if(ch1+1!=imp_h2.getNChannels())return false;
		
		for(int ns=1; ns<=imp_h1.getNSlices(); ns++){
			ip_0=stack_0.getProcessor(ns);
			ip_h2=stack_h2[0].getProcessor(ns);
			for(int j=0; j<imp_h1.getHeight(); j++){
				for(int i=0; i<imp_h1.getWidth();i++){
					value=ip_0.getPixel(i, j);
					ip_h2.putPixel(i, j, value);
				}
			}
		}
		for(int ns=1; ns<=imp_h1.getNSlices(); ns++){
			for(int ch=1; ch<=ch1; ch++){
				ip_h1=stack_h1[ch-1].getProcessor(ns);
				ip_h2=stack_h2[ch].getProcessor(ns);
				for(int j=0; j<imp_h1.getHeight(); j++){
					for(int i=0; i<imp_h1.getWidth();i++){
						value=ip_h1.getPixel(i, j);
						ip_h2.putPixel(i, j, value);
					}
				}
			}
		}
		return true;
	}
	
	// キャリブレーションのコピー imp1からimp2へ                 by Hiroki Oda   2013.4.6
	public void copyCalibration(ImagePlus imp1, ImagePlus imp2){
		Calibration cal1=imp1.getCalibration();
		Calibration cal2=imp2.getCalibration();
		cal2.pixelWidth=cal1.pixelWidth;
		cal2.pixelHeight=cal1.pixelHeight;
		cal2.pixelDepth=cal1.pixelDepth;
		cal2.frameInterval=cal1.frameInterval;
		cal2.xOrigin=cal1.xOrigin;
		cal2.yOrigin=cal1.yOrigin;
		cal2.zOrigin=cal2.zOrigin;
		cal2.fps=cal2.fps;
		String unit=cal1.getUnit();
		cal2.setUnit(unit);
		unit=cal1.getXUnit();
		cal2.setXUnit(unit);
		unit=cal1.getYUnit();
		cal2.setYUnit(unit);
		unit=cal1.getZUnit();
		cal2.setZUnit(unit);
		unit=cal1.getTimeUnit();
		cal2.setTimeUnit(unit);
	}
}