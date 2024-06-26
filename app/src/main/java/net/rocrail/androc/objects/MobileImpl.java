package net.rocrail.androc.objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.rocrail.androc.RocrailService;
import net.rocrail.androc.interfaces.Mobile;
import net.rocrail.androc.widgets.LocoImage;

import org.xml.sax.Attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MobileImpl implements Mobile {
    public String Roadname = "";
    public String Consist = "";
    public int Steps = 0;
    public long RunTime = 0;
    public int Vmax = 0;
    public int Vmid = 0;
    public int Vmin = 0;
    public String Vmode = "";
    public int Era = 0;
    protected List<Function> Functions = new ArrayList<>();
    protected String ID = "?";
    protected String Description = "";
    protected String PicName = null;
    protected int Addr = 0;
    protected int Speed = 0;
    protected int Vprev = 0;
    protected Bitmap LocoBmp = null;
    protected boolean ImageRequested = false;
    protected LocoImage imageView = null;
    protected Attributes properties = null;
    protected HashMap<String, Bitmap> FunBmp = new HashMap<>();
    protected boolean[] Function = new boolean[32];
    protected boolean Dir = true;
    protected boolean Placing = true;
    protected boolean Show = true;
    protected boolean Lights = false;
    RocrailService rocrailService = null;

    static byte[] strToByte(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2 + 1];
        for (int i = 0; i < len; i += 2) {
            int val = Integer.parseInt(s.substring(i, i + 2), 16);
            b[i / 2] = (byte) (val & 0xFF);
        }
        return b;
    }

    @Override
    public boolean isPlacing() {
        return Placing;
    }

    @Override
    public void setPlacing(boolean placing) {
        Placing = placing;
    }

    @Override
    public long getRunTime() {
        return RunTime;
    }

    @Override
    public int getSteps() {
        return Steps;
    }

    public void setSpeed(int V, boolean force) {
        if (force || V == Vmax || V == 0 || V != Vprev || Steps < 50) {
            Speed = V;
            System.out.println("set Speed=" + Speed);
            setSpeed(false);
        }
    }

    public void flipDir() {
        Dir = !Dir;
        Speed = 0;
        setSpeed(true);
    }

    @Override
    public String getDescription() {
        return Description;
    }


    @Override
    public String getConsist() {
        return Consist;
    }

    @Override
    public boolean isFunction(int fn) {
        return Function[fn];
    }


    @Override
    public int getSpeed() {
        return Speed;
    }

    public void setSpeed(boolean force) {
        if (force || Vprev != Speed) {
            Vprev = Speed;
            rocrailService.sendMessage("lc", String.format("<lc throttleid=\"%s\" id=\"%s\" V=\"%d\" dir=\"%s\" fn=\"%s\"/>", rocrailService.getDeviceName(), ID, Speed, (Dir ? "true" : "false"), (Lights ? "true" : "false")));
        }
    }

    @Override
    public boolean isDir() {
        return Dir;
    }

    @Override
    public boolean isLights() {
        return Lights;
    }

    @Override
    public int getAddr() {
        return Addr;
    }

    @Override
    public boolean isShow() {
        return Show;
    }

    @Override
    public String getID() {
        return ID;
    }

    public void addFunction(Attributes atts) {
        Function function = new Function();
        function.Nr = Item.getAttrValue(atts, "fn", 0);
        function.Text = Item.getAttrValue(atts, "text", "F" + function.Nr);
        function.Icon = Item.getAttrValue(atts, "icon", "");
        Functions.add(function);
    }

    public String getFunctionText(int nr) {
        if (Functions != null) {
            for (net.rocrail.androc.objects.Function function : Functions) {
                if (function.Nr == nr && !function.Text.isEmpty()) return function.Text;
            }
        }
        return "F" + nr;
    }

    public Bitmap getFunctionIcon(int nr) {
        return requestFunctionIcon(nr);
    }

    @Override
    public String getRoadname() {
        return Roadname;
    }

    @Override
    public LocoImage getImageView() {
        return imageView;
    }

    @Override
    public void flipFunction(int fn) {
        Function[fn] = !Function[fn];
        rocrailService.sendMessage("lc", String.format("<fn id=\"%s\" fnchanged=\"%d\" group=\"%d\" f%d=\"%s\"/>", ID, fn, (fn - 1) / 4 + 1, fn, (Function[fn] ? "true" : "false")));
    }

    @Override
    public void flipLights() {
        Lights = !Lights;
        rocrailService.sendMessage("lc", String.format("<fn id=\"%s\" fnchanged=\"%d\" group=\"%d\" f%d=\"%s\"/>", ID, 0, 1, 0, (Lights ? "true" : "false")));
    }

    public void updateFunctions(Attributes atts) {
        for (int i = 1; i <= 24; i++) {
            Function[i] = Item.getAttrValue(atts, "f" + i, Function[i]);
        }
    }

    public Bitmap requestFunctionIcon(int nr) {
        String IconName = "";
        if (Functions != null) {
            for (net.rocrail.androc.objects.Function function : Functions) {
                if (function.Nr == nr) {
                    if (!function.Icon.isEmpty()) {
                        IconName = function.Icon;
                        break;
                    }
                    return null;
                }
            }
        }
        if (IconName.isEmpty()) return null;

        Bitmap bmp = null;

        File dir = new File("/sdcard/androc/");
        if (!dir.exists()) dir.mkdirs();
        File file = new File("/sdcard/androc/" + IconName);
        if (file.exists()) {
            try {
                byte[] rawdata = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(rawdata);
                fis.close();
                bmp = BitmapFactory.decodeByteArray(rawdata, 0, rawdata.length);
                FunBmp.put("" + nr, bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (bmp == null && !FunBmp.containsKey("" + nr)) {
            FunBmp.put("" + nr, null);

            rocrailService.sendMessage("datareq", String.format("<datareq id=\"%s\" function=\"%d\" filename=\"%s\"/>", ID, nr, IconName));
        }
        return bmp;
    }

    public void setPicData(String filename, String data, int nr) {
        if (data != null && !data.isEmpty()) {
            // convert from HEXA to Bitmap
            byte[] rawdata = strToByte(data);

            File dir = new File("/sdcard/androc/");
            if (!dir.exists()) dir.mkdirs();

            File file = null;
            if (filename.equals(PicName)) file = new File("/sdcard/androc/" + PicName);
            else {
                if (Functions != null) {
                    for (net.rocrail.androc.objects.Function function : Functions) {
                        if (function.Nr == nr && !function.Icon.isEmpty())
                            file = new File("/sdcard/androc/" + function.Icon);
                    }
                }
            }

            if (file != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(rawdata);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bitmap bmp = BitmapFactory.decodeByteArray(rawdata, 0, rawdata.length);
                if (nr == 0) {
                    LocoBmp = bmp;
                    if (imageView != null) {
                        imageView.post(new UpdateMobileImage(this));
                    }
                } else {
                    FunBmp.put("" + nr, bmp);
                }
            }
        }
    }

    @Override
    public Bitmap getBmp(LocoImage image) {
        if (LocoBmp == null) {
            requestImg(image);
        }
        return LocoBmp;
    }

    public void requestImg(LocoImage image) {
        if (image != null) imageView = image;


        File dir = new File("/sdcard/androc/");
        if (!dir.exists()) dir.mkdirs();
        File file = new File("/sdcard/androc/" + PicName);
        if (file.exists()) {
            try {
                byte[] rawdata = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(rawdata);
                fis.close();
                LocoBmp = BitmapFactory.decodeByteArray(rawdata, 0, rawdata.length);
                if (imageView != null) {
                    imageView.post(new UpdateMobileImage(this));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (LocoBmp == null && !ImageRequested) {
            ImageRequested = true;
            if (PicName != null && !PicName.isEmpty()) {
                // type 1 is for small images
                rocrailService.sendMessage("datareq", String.format("<datareq id=\"%s\" type=\"1\" filename=\"%s\"/>", ID, PicName));
            }
        }
    }

    public String toString() {
        if (!Description.isEmpty()) {
            return ID + ", " + Description;
        }
        return ID;
    }
}
