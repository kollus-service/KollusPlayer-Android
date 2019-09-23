package kollus.test.media.player.download;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kollus.sdk.media.KollusPlayerDRMListener;
import com.kollus.sdk.media.KollusStorage;
import com.kollus.sdk.media.KollusStorage.OnKollusStorageListener;
import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.ErrorCodes;
import com.kollus.sdk.media.util.Log;
import com.kollus.sdk.media.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service{
	private static final String TAG = DownloadService.class.getSimpleName();

	private final int MIN_FREE_SIZE = 150*1024*1024;
    public static final int ADD_HANDLER	 		= 0;
    public static final int DOWNLOAD_START 		= 10;
    public static final int DOWNLOAD_LOADED		= 11;
    public static final int DOWNLOAD_ALREDY_LOADED = 12;
    public static final int DOWNLOAD_STARTED	= 13;
    public static final int DOWNLOAD_LOAD_ERROR	= 14;
    public static final int DOWNLOAD_CANCEL 	= 20;
    public static final int DOWNLOAD_CANCELED 	= 21;
    public static final int DOWNLOAD_ERROR 		= 30;
    public static final int DOWNLOAD_PROCESS 	= 40;
    public static final int DOWNLOAD_COMPLETE 	= 50;
    public static final int DOWNLOAD_DRM		= 60;
    public static final int DOWNLOAD_DRM_INFO	= 61;

    private KollusStorage mStorage;
	private String mStoragePath;
    Messenger mMessenger = new Messenger(new LocalHandler());
    Messenger mClientMessenger;
    List<Handler> mHandlers;
    private ExecutorService mExecutor;
    List<DownloadInfo> mDownloadList;
	private static boolean mDownloading = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlers = new ArrayList<Handler>();
        mDownloadList = new ArrayList<DownloadInfo>();
        mStorage = KollusStorage.getInstance(getApplicationContext());
		mStorage.setOnKollusStorageListener(mKollusStorageListener);
		mStorage.setKollusPlayerDRMListener(mKollusPlayerDRMListener);
		mStoragePath = Utils.getStoragePath(this);
		mExecutor = Executors.newFixedThreadPool(1);
		Log.d(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
//    	mStorage.releaseInstance();
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		return super.onStartCommand(intent, flags, startId);
    	return START_REDELIVER_INTENT;
	}

	private class LocalHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ADD_HANDLER:
            {
            	mClientMessenger = new Messenger((Handler) msg.obj);
                try {
                	mClientMessenger.send(Message.obtain(null, ADD_HANDLER, "Registed messanger"));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
                break;
                
            case DOWNLOAD_START:
            {
            	DownloadInfo info = (DownloadInfo)msg.obj;
            	LoadTask task = new LoadTask(info);
            	mExecutor.execute(task);
            }
                break;

            case DOWNLOAD_CANCEL:
            {
            	String mediaContentKey = (String)msg.obj;
            	Log.d(TAG, "DOWNLOAD_CANCEL:"+mediaContentKey);
            	try {
        			mStorage.unload(mediaContentKey);
            		
        			synchronized (mDownloadList){
	            		if(mDownloadList.get(0).getKollusContent().getMediaContentKey().equals(mediaContentKey)) {
							nextDownload();
						}
	            		else {
							removeDownloadList(msg.arg1);
						}
        			}
				
        			mClientMessenger.send(Message.obtain(null, DOWNLOAD_CANCELED, 0, 0, mediaContentKey));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
                break;
            
            default:
                break;
            }
            super.handleMessage(msg);
        }
    }

	public static boolean isDownloading() {
		return mDownloading;
	}
    
    private class LoadTask implements Runnable {
		private DownloadInfo mInfo;

		LoadTask(DownloadInfo info) {
			mInfo = info;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
    			synchronized (mDownloadList) {
    				for(DownloadInfo info : mDownloadList) {
    					if(info.getUrl().equals(mInfo.getUrl())) {
    						Log.w(TAG, "Already exists in DownloadList");
    						mClientMessenger.send(Message.obtain(null, DOWNLOAD_ALREDY_LOADED));
    						return;
    					}
    				}
    			}

				long freeSize = Utils.getAvailableMemorySize(mStoragePath)-mInfo.getKollusContent().getFileSize();
				if(freeSize < MIN_FREE_SIZE) {
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_LOAD_ERROR, 0, ErrorCodes.ERROR_STORAGE_FULL));
					return;
				}

        		int nErrorCode = mStorage.load(mInfo.getUrl(), mInfo.getKollusContent());
        		if(nErrorCode != ErrorCodes.ERROR_OK) {
        			mClientMessenger.send(Message.obtain(null, DOWNLOAD_LOAD_ERROR, 0, nErrorCode));
        		}
        		else {
        			mClientMessenger.send(Message.obtain(null, DOWNLOAD_LOADED, 0, 0, mInfo));
        			synchronized (mDownloadList) {
	    				if(mDownloadList.isEmpty()) {
							long fileSize = mInfo.getKollusContent().getFileSize();
							freeSize = Utils.getAvailableMemorySize(mStoragePath);
							if(fileSize > freeSize || (freeSize-fileSize) < MIN_FREE_SIZE) {
								mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, ErrorCodes.ERROR_STORAGE_FULL, mInfo.getKollusContent()));
								return;
							}

	    					int nRet = mStorage.download(mInfo.getKollusContent().getMediaContentKey());
	    					Log.d(TAG, "Send Message Start index "+mInfo.getKollusContent().getUriIndex()+" return "+nRet);
	    	    			if(nRet >= 0) {
								mDownloading = true;
	    	    				mClientMessenger.send(Message.obtain(null, DOWNLOAD_STARTED, 0, 0, mInfo.getKollusContent()));
	    	    			}
	    	    			else {
	    	    				mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, nRet, mInfo.getKollusContent()));
	    	    			}
	    	    			
	    				}
	    				
	    				mDownloadList.add(mInfo);
        			}
    			}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
    
    OnKollusStorageListener mKollusStorageListener = new OnKollusStorageListener() {
		@Override
		public void onComplete(KollusContent content) {
			// TODO Auto-generated method stub
			if(mClientMessenger != null) {
				try {
					synchronized (mDownloadList) {
						nextDownload();
					}
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_COMPLETE, 0, 0, content));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onProgress(KollusContent content) {
			// TODO Auto-generated method stub
			if(mClientMessenger != null) {
				try {
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_PROCESS, 0, 0, content));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onError(KollusContent content, int errorCode) {
			// TODO Auto-generated method stub
			if(mClientMessenger != null) {
				try {
					Log.d(TAG, "onError "+content.getMediaContentKey());
					synchronized (mDownloadList) {
						nextDownload();
					}
					content.setDownloadError();
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, errorCode, content));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	private void nextDownload() throws RemoteException {

		while(!mDownloadList.isEmpty()) {
			mDownloadList.remove(0);
			
			if(mDownloadList.isEmpty()) {
				Log.d(TAG, "nextDownload is null");
				mDownloading = false;
				break;
			}
			else {
				long fileSize = mDownloadList.get(0).getKollusContent().getFileSize();
				long freeSize = Utils.getAvailableMemorySize(mStoragePath);
				if(fileSize > freeSize || (freeSize-fileSize) < MIN_FREE_SIZE) {
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, ErrorCodes.ERROR_STORAGE_FULL, mDownloadList.get(0).getKollusContent()));
				}
				else {
					int nRet = mStorage.download(mDownloadList.get(0).getKollusContent().getMediaContentKey());
					if (nRet >= 0) {
						Log.d(TAG, "nextDownload started " + mDownloadList.get(0).getKollusContent());
						mDownloading = true;
						mClientMessenger.send(Message.obtain(null, DOWNLOAD_STARTED, 0, 0, mDownloadList.get(0).getKollusContent()));
						break;
					} else {
						Log.d(TAG, "nextDownload error " + mDownloadList.get(0).getKollusContent());
						mClientMessenger.send(Message.obtain(null, DOWNLOAD_ERROR, 0, nRet, mDownloadList.get(0).getKollusContent()));
					}
				}
			}
		}
	}
	
	private void removeDownloadList(int index) {
		for(DownloadInfo info : mDownloadList) {
			if(info.getKollusContent().getUriIndex() == index) {
				mDownloadList.remove(info);
				break;
			}
		}
	}
	
	KollusPlayerDRMListener mKollusPlayerDRMListener = new KollusPlayerDRMListener() {
		@Override
		public void onDRM(String request, String response) {
			// TODO Auto-generated method stub
			if(mClientMessenger != null) {
				try {
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_DRM, new DownloadDRM(request, response)));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onDRMInfo(KollusContent content, int nInfoCode) {
			if(mClientMessenger != null) {
				try {
					if(nInfoCode == KollusPlayerDRMListener.DCB_INFO_DELETE) {
						content.setDownloadError();
						synchronized (mDownloadList) {
							nextDownload();
						}
					}
					mClientMessenger.send(Message.obtain(null, DOWNLOAD_DRM_INFO, 0, nInfoCode, content));
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
}

