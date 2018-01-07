import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

// 最初のチャネルを削除して、残りのチャネルをコピーして新しいhyperstackを表示する。
// チャネル数はひとつ減少。
// if(removeCh(imp,0,imp_hyp)==false)の第２引数で削除するチャネルを任意に指定できる。

public class My_RemoveCh1 implements PlugInFilter {
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
		
		ImagePlus imp_hyp;
		imp_hyp=IJ.createHyperStack("Output_ChannelRemoved", w, h, nCh-1, nSl, nFr, 8);
		if(removeCh(imp,0,imp_hyp)==false)
			{IJ.error("The channel specified can not be removed!"); return;}     //  impの第２引数で指定するchannelのstackを削除して他をimp_hypにコピーする。
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
	
	//imp_h1の指定したchannelのstackを削除してそのほかをimp_h2に書き込んで返す。 by Hiroki Oda   2013.4.5
	public boolean removeCh(ImagePlus imp_h1, int ch, ImagePlus imp_h2){
		ImageStack[] stack_h1 = getEachStack(imp_h1);
		ImageStack[] stack_h2 = getEachStack(imp_h2);
		ImageProcessor ip_h1, ip_h2;
		
		int ch1,ch2,value;
		ch1=imp_h1.getNChannels();
		
		if(ch1-1<ch || ch<0)return false;
		if(ch1-1!=imp_h2.getNChannels())return false;
		
		for(int ns=1; ns<=imp_h1.getNSlices(); ns++){
			ch2=0;
			for(int c=0; c<ch1; c++){
				if(c!=ch){
					ip_h1=stack_h1[c].getProcessor(ns);
					ip_h2=stack_h2[ch2].getProcessor(ns);
					for(int j=0; j<imp_h1.getHeight(); j++){
						for(int i=0; i<imp_h1.getWidth();i++){
							value=ip_h1.getPixel(i, j);
							ip_h2.putPixel(i, j, value);
						}
					}
					ch2++;
				}
			}
		}
		return true;
	}
	
	// Multichannel hyperstackとsingle channel stackを統合して新たなhyperstackを作る。 by Hiroki Oda   2013.4.5
	public void getCombine(ImagePlus imp_h1, ImagePlus imp_0, ImagePlus imp_h2){
		ImageStack[] stack_h1=getEachStack(imp_h1);
		ImageStack stack_0=imp_0.getImageStack();
		ImageStack[] stack_h2=getEachStack(imp_h2);
		ImageProcessor ip_h1, ip_0, ip_h2;
		
		int ch1, value;
		ch1=imp_h1.getNChannels();
		
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