// "Treat All StacksIDS with ExtractSurface"
// This macro saves all image windows in a specfied directory
// with the title used as the file name.
//
// by Hiroki Oda, 2013.4.6


macro "Treat All StackIDS with ExtractSurface" {
	source_dir = getDirectory("Source Directory");  // select the directory containg .ids files
	target_dir = getDirectory("Target Directory");  // select the directory for saving the output files.
	if (File.exists(target_dir) && File.exists(source_dir)) {
		setBatchMode(false);
		list = getFileList(source_dir);
		for(i=0; i<list.length; i++){
			if(endsWith(list[i], "ids")){
				open(source_dir + "/" + list[i]);
				run("My ExtractPreTreatS");
				run("My ExtractSurfaceS");
				run("My RemoveCh1");
				run("My ColorConvertRGBtoBGR");
				selectWindow("Output_ColorConverted");
				saveAs("tiff", target_dir + "/" + list[i]);
				run("Bio-Formats Exporter");
				close();
				selectWindow("/"+list[i]);
				close();
				selectWindow("Output");
				close();
				selectWindow("Output_ChannelRemoved");
				close();
				showProgress(i, list.length);
			}
		}
	}
}
