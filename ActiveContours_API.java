import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import levelsets.algorithm.ActiveContours;
import levelsets.algorithm.LevelSetImplementation;
import levelsets.ij.ImageContainer;
import levelsets.ij.ImageProgressContainer;
import levelsets.ij.StateContainer;

public class ActiveContours_API {

    public static ImagePlus getSegImage(ImagePlus originalImage, Roi roi, double convergence, double advection, double curvature, double grey_tol, boolean expandToInside, int max_iteration, int step_iteration, boolean getProgressReport) {
        originalImage.setRoi(roi);

        //creating ImageContainer
        ImageContainer ic = new ImageContainer(originalImage);

        //Creating ImageProgressContainer
        ImageProgressContainer progressImage = null;
        if(getProgressReport)
        {
            progressImage = new ImageProgressContainer();
            progressImage.duplicateImages(ic);
            progressImage.createImagePlus("Segmentation progress of " + originalImage.getTitle());
            progressImage.showProgressStep();
        }

        // Create a initial state map out of the roi
        StateContainer sc_roi = new StateContainer();
        sc_roi.setROI(roi, ic.getWidth(), ic.getHeight(), ic.getImageCount(), originalImage.getCurrentSlice());

        //For which side to evolve. False implies that it will expand to Outside.
        sc_roi.setExpansionToInside(expandToInside);
        LevelSetImplementation ls = new ActiveContours(ic, progressImage, sc_roi, convergence, advection, curvature, grey_tol);

        for (int iter = 0; iter < max_iteration; iter++) {
            if (!ls.step(step_iteration)) {
                break;
            }
        }
        StateContainer sc_final = ls.getStateContainer();

        // Convert sc_final into binary image ImageContainer and display
        if (sc_final == null) {
            IJ.log("Error. Sc_final is null. Yah! I know this message is not helpful.");
        }
        ImageStack stack = new ImageStack(originalImage.getWidth(), originalImage.getHeight());
        for (ImageProcessor bp : sc_final.getIPMask()) {
            stack.addSlice(null, bp);
        }
        ImagePlus seg = originalImage.createImagePlus();
        seg.setStack("Segmentation of " + originalImage.getTitle(), stack);
        seg.setSlice(originalImage.getCurrentSlice());

        return seg;
    }
}
