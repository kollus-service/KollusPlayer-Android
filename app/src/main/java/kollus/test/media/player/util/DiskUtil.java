package kollus.test.media.player.util;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Created by Song on 2017-09-07.
 */

public class DiskUtil {
    private static final String TAG = DiskUtil.class.getSimpleName();
    private static final double ONE_KILOBYTE = 1024;
    private static final double ONE_MEGABYTE = ONE_KILOBYTE*1024;
    private static final double ONE_GIGABYTE = ONE_MEGABYTE*1024;

    /**
     * 주어진 사이즈 String으로 가져오는 함수
     * 예 : 1.00GB, 1.00MB, 1.00KB, 1B
     * @param size
     * @return
     */
    public static String getStringSize(long size) {
        if(size >= ONE_GIGABYTE)
            return String.format("%1.2fGB", size/ONE_GIGABYTE);
        else if(size > ONE_MEGABYTE)
            return String.format("%1.2fMB", size/ONE_MEGABYTE);
        else if(size > ONE_KILOBYTE)
            return String.format("%1.2fKB", size/ONE_KILOBYTE);
        else if(size > 0)
            return String.format("%1.2dB", size);
        else
            return "0B";
    }

    private static String getExternalStoragePath(Context context) {
        String storagePath = null;
        int storageCount = 0;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (storageManager == null) {
                return null;
            }

            Method method;
            Object obj;

            method = storageManager.getClass().getMethod("getVolumePaths", (Class[]) null);
            obj = method.invoke(storageManager, (Object[]) null);
            String[] paths = (String[]) obj;
            if (paths == null) {
                return null;
            }

            method = storageManager.getClass().getMethod("getVolumeState", new Class[] { String.class });
            for (String path : paths) {
                obj = method.invoke(storageManager, new Object[] { path });
                if (Environment.MEDIA_MOUNTED.equals(obj)) {
                    storageCount++;
                    if (2 == storageCount) {
                        storagePath = path;
                        return storagePath;
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                storageCount = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storagePath;
    }

    /**
     * 외부 SD 카드 경로를 배열로 가져오는 함수
     * @param context
     * @return SD카드 경로
     */
    public static Vector<String> getExternalMounts(Context context) {
        final Vector<String> out = new Vector<String>();
        File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
        for(File iter : dirs) {
            if(iter != null)
                    out.add(iter.getParent());
        }

        String old = getExternalStoragePath(context);
        if(old != null) {
            old += "/Android/data/" + context.getPackageName();
            if (!out.contains(old))
                out.add(old);
        }

        try {
            for(String iter : out) {
                File dir = new File(iter);
                if(!dir.exists()) {
                    dir.mkdirs();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }
}
