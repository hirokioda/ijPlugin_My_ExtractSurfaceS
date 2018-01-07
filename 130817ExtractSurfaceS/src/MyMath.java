

public class MyMath {
	
	// 最大値の取得
	public static double[] getMax(double[] value, int numb){  // double[] v　は0以上
		double[] result= new double[2];  // result[0]=max値を示す配列index; result[1]= max値;
		result[0]=0.0; result[1]=value[0];
		for(int i=1; i<numb; i++){
			if(value[i]>result[1]) {result[0]=i; result[1]=value[i];}
		}
		return result;
	}
	
	// 最初のthreshold以上の値を取得
	public static double[] getFirstL_fromTop(double[] value, int numb, double threshold){  // thresholdよりも大きな値に遭遇したらその値と配列インデックスをリターン
		double[] result=new double[2];
		for(int i=0; i<numb; i++){
			if(value[i]>threshold) {result[0]=i; result[1]=value[i]; return result;}
		}
		result[0]=-1; result[1]=-1;   // 期待する値がなければ-1をリターン
		return result;
	}
	
	public static double[] getFirstL_fromBottom(double[] value, int numb, double threshold){  // thresholdよりも大きな値に遭遇したらその値と配列インデックスをリターン
		double[] result=new double[2];
		for(int i=numb-1; i>=0; i--){
			if(value[i]>threshold) {result[0]=i; result[1]=value[i]; return result;}
		}
		result[0]=-1; result[1]=-1;   // 期待する値がなければ-1をリターン
		return result;
	}
	
	public static double[] getFirstPercentMax(double[] value, int numb, double percent, boolean from_top){  // thresholdよりも大きな値に遭遇したらその値と配列インデックスをリターン
		double[] result=new double[2];
		double[] max=new double[2];
		max=getMax(value, numb);
		if(from_top==true)result=getFirstL_fromTop(value, numb, 0.01*percent*max[1]);
		else result=getFirstL_fromBottom(value, numb, 0.01*percent*max[1]);
		return result;
	}
	
	public static double[] getZdiff(double[] value, int numb){   // 微分を取得。値の変化があったかどうかを見て、z profileに核のシグナルがあるかを判定するため。
		double[] diff=new double[numb-1];
		for(int i=0; i<numb-1; i++){
			diff[i]=value[i+1]-value[i];
		}
		return diff;
	}
	
	public static boolean judgeMaxDiff(double[] value, int numb, double threshold){  //  微分のmaxで判定
		double[] diff=new double[numb-1];
		double[] max=new double[2];
		diff=getZdiff(value, numb);
		max=getMax(diff, numb-1);
		if(max[1]<threshold) return true;
		return false;
	}
	
	// スムージング
	public static double[] smoothenArray(double[] s, int windowS){  // 配列sの数値をwindowをシフトしながら平均をとっていく; windowSはウインドウサイズ
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
