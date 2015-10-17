package com.example.dries.myocrtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.widget.ImageView;

/**
 * Created by Dries on 9/10/2015.
 */

public class RsScript {
    public Bitmap inBitmap = null;
    public Bitmap outBitmap = null;
    public Context mContext;
    public MainActivity MainThread;
    public boolean Working = true;

    /*! \brief Constructor
    *
    *
    * @param mActiv handle to the MainActivity necessary for the creation of RenderScript objects.
    * @param imageView The image view from the MainActivity where the resulting image is displayed
    * @param view here execution times are displayed
    */
    public RsScript(MainActivity mActiv, ImageView imageView) {

        mContext = mActiv;    //needed by renderscript
        MainThread = mActiv;    //needed for updating UI components from a subthread
    }

    /*! \brief funtion to set the input bitmap
    *
    * This funcion sets the input bitmap that is used in this class for the image processing.
    * The output bitmap used for storing the result after filter execution, is also created
    * with the same dimensions as the input bitmap.
    * @param in The bitmap data
    *
    */
    public void setInputBitmap(Bitmap in) {
        inBitmap = in;
        outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), Bitmap.Config.ARGB_8888);
    }

    /*! \brief returns the output bitmap
    *
    * @return outBitmap the output bitmap
    */
    public Bitmap getOutputBitmap() {
        return outBitmap;
    }

    /*! \brief executes an inverse Filter on the input image
*
* A Renderscript context object is created to handle the lifetime of all other RenderScript objects.
* The necessary memory is allocated for the computations, and to write back the result.
* Global script variables are set and the script starts execution.
* When complete, all objects are destroyed to free the memory.
*
*/
    public void RenderScriptInverse() {

        if (inBitmap == null)
            return;
        long startTime = System.nanoTime();
        Working = true;
        final RenderScript rs = RenderScript.create(mContext);
        Allocation allocIn;
        allocIn = Allocation.createFromBitmap(rs, inBitmap,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT);
        Allocation allocOut = Allocation.createTyped(rs, allocIn.getType());
/*        ScriptC_inverse script = new ScriptC_inverse(rs);

        script.set_in(allocIn);
        script.set_out(allocOut);
        script.set_script(script);

        script.invoke_filter();*/
        //script.forEach_root(allocIn, allocOut);
        rs.finish();
        allocOut.copyTo(outBitmap);

        allocOut.destroy();
        allocIn.destroy();
        rs.destroy();
        //script.destroy();
        Working = false;
    }
}
