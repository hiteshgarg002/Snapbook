package com.hrrock.snapbook.utils;

import android.os.Environment;

/**
 * Created by hp-u on 10/4/2017.
 */

public class FilePaths {
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String DCIM = ROOT_DIR + "/DCIM";
    public String CAMERA = DCIM + "/Camera";
    public String SDCARD = "/storage/4DFD-1CFE/DCIM";
    public String SDCARD_CAMERA = SDCARD + "/Camera";
    public String SDCARD_Retrica = SDCARD + "/Retrica";
    public String INSTAGRAM = PICTURES + "/Instagram";
    public String SCREENSHOTS = PICTURES + "/Screenshots";
    public String RETRICA = DCIM + "/Retrica";
    public String WHATSAPP = ROOT_DIR + "/WhatsApp/Media/WhatsApp Images";
}
