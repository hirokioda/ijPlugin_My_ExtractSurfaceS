ImageJ plugin "My_ExtractSurface"

This plugin was developed by Hiroki Oda and used to extract signals from around the surface cell layer of fluorescently stained spider embryos (Hemmi et al., submitted).
This plugin, combined with My_ExtractPreTreatS, My_RemoveCh1, and My_ColorConvertRGBtoBGR, is used in an attached imageJ Macro "Treat All StacksIDS with ExtractSurface.ijm."
A workspace directory "C:/nsworkspace/imageJ/", including "_ESetting.txt", must be prepared in advance.

Procedure (in case of Windows 7 or 10)
0) Install Fiji.app (ImageJ), which is placed in "home/Program Files/" in my case.
0) Place My_My_ExtractSurface.jar, My_ExtractPreTreatS.jar, My_RemoveCh1.jar, and My_ColorConvertRGBtoBGR.jar in the ImageJ plugin directory (e.g., home/Program Files/Fiji.app/plugins/MyPlugins/).
1) Prepare image stacks (.ids/.ics) in a directory.
2) Prepare _ESetting.txt in "C:/nsworkspace/imageJ/".
3) Prepare an output directroy.
4) Run the macro "Treat All StacksIDS with ExtractSurface.ijm" on the ImageJ.
5) Select the directory containing the .ids files.
6) Select the directory for output.
7) Check the output directory for results.

The format of the "_ESetting.txt" file.
----------------------
#ESetting
#
smRadius, xxxx
dapiCh, x
sdtMaxPercent,30
reBase,0
exThick,8
cutOff,10
----------------------
dapiCh is the number of the DAPI channel in the image stack, which is treated with RankFilters (MEAN).
smRadius is the radius size used for the RankFilters treatment.
The base z-slice, baseZ[x,y], is determined at a z-slice where the smoothed dapi intensity value reaches X% (sdtMaxPercent) of the max along the z-axis of each pixel from the bottom.
 //  z-slices ->
 //  0000<apical*******baseZ[x,y]**basal>0000000
 //  (baseZ[x,y]-exThick)     (baseZ[x,y]+reBase)
Outside the range, all signal intensities are changed to zero.
Independently, "cutOff" intensity value can be set.

