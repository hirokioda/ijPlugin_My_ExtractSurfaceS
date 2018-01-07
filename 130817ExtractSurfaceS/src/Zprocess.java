import ij.ImageStack;
import ij.process.ImageProcessor;

public class Zprocess {
	ImageStack[] stack;
	int width;
	int height;
	int nCh=3; // チャネル数
	int nSlices; // zスライスの数
	int nFrames;  // time points
	int mptNumb=0;
	double puLx =1.0;  // 1 pixel当たりの長さ、デフォルトで1.0を設定
	double puLy =1.0;
	double puLz =1.0;

	Zprocess(ImageStack[] stack, int w, int h,int ch, int nsl, int nfr){
		this.stack=stack;
		this.width=w;
		this.height=h;
		this.nCh=ch;
		this.nSlices=nsl;
		this.nFrames=nfr;
	}

	int[][][] dapi=new int[1024][1024][60]; // x, y, z real pos：Measurement pointsの数は最大100まで。

	public void getImageStackData(int n) {  // n番目のチャネルのデータすべてをひとつの配列に入れる。
		for(int k=0; k<nSlices; k++){
			ImageProcessor im= stack[n].getProcessor(k+1);
			for(int j=0; j<height; j++){
				for(int i=0; i<width; i++){
					dapi[i][j][k]=im.getPixel(i, j);
				}
			}
		}
	}

	int[][] baseZ=new int[1024][1024];       // 各(x,y)ポイントにおいて、シグナル

	public double[] getZprofile(int nx, int ny){  // (nx, ny)のzプロファイルを取得
		double[] zp=new double[nSlices];
		for(int i=0; i<nSlices; i++){
			zp[i]=(double)dapi[nx][ny][i];
		}
		return zp;
	}

	public void getSurfaceRange(double percent){
		double[] prf=new double[nSlices];
		for(int j=0; j<height; j++){
			for(int i=0; i<width; i++){
				prf=getZprofile(i,j);
				double[] rslt=MyMath.getFirstPercentMax(prf, nSlices, percent, false); //  true, from top; false, from bottom
				baseZ[i][j]=(int)rslt[0];
			}
		}
	}

	public void extractSurfaceRange(int ch, int relative_base, int thickness, int min_threshold){   //  *****apical*******baseZ[x,y]**basal*********
                                                                                                    //    (baseZ[]-thickness)     (baseZ[]+relative_base)
		int apical, basal;
		for(int k=0; k<nSlices;k++){
			ImageProcessor imp=stack[ch].getProcessor(k+1);
			for(int j=0; j<height; j++){
				for(int i=0; i<width; i++){
					basal=baseZ[i][j]+relative_base;
					apical=basal-thickness;
					if(k<apical){imp.putPixel(i, j, 0);}
					else if(k>basal){imp.putPixel(i, j, 0);}
					else if(dapi[i][j][k]<min_threshold){imp.putPixel(i, j, 0);}   // シグナル強度が低い場合はカット。25では消える核がある。10-15が適当。
				}
			}
		}
	}

	public ImageStack getStack(int ch){
		return stack[ch];
	}

}
