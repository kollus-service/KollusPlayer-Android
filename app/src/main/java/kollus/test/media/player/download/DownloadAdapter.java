package kollus.test.media.player.download;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.kollus.sdk.media.content.KollusContent;
import com.kollus.sdk.media.util.Log;
import kollus.test.media.R;
import kollus.test.media.player.contents.DownloadContentsViewHolder;
import kollus.test.media.player.util.DiskUtil;

import java.util.ArrayList;
import java.util.List;



public class DownloadAdapter extends RecyclerView.Adapter<DownloadContentsViewHolder> {
	private static final String TAG = DownloadAdapter.class.getSimpleName();
	
	private Resources mResources;
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<KollusContent> mContentsList;
    private OnDownloadCancelListener mDownloadCancelListener;
//    private boolean mBindView;
    
    public interface OnDownloadCancelListener {
    	public void onDownloadCancel(KollusContent content);
    }
    
    public DownloadAdapter(Context context, ArrayList<KollusContent> contentsList, OnDownloadCancelListener listener){
        // Cache the LayoutInflate to avoid asking for a new one each time.
//    	super(context, R.layout.file_list, contentsList);
    	
    	mContext = context;
    	mResources = context.getResources();
        mInflater = LayoutInflater.from(context);
        mContentsList = contentsList;
        mDownloadCancelListener = listener;
    }

    @Override
    public DownloadContentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(mContext).inflate(R.layout.download_list, parent, false);
        DownloadContentsViewHolder viewHolder = new DownloadContentsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DownloadContentsViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(DownloadContentsViewHolder holder, int position, List<Object> payloads) {
        Log.d(TAG, "onBindViewHolder:"+position);
        KollusContent content = mContentsList.get(position);

//        if(!mBindView)
        {
            String thumbnail = content.getThumbnailPath();
            if (thumbnail != null && !thumbnail.startsWith("http://")) {
                Bitmap bm = BitmapFactory.decodeFile(thumbnail);
                if (bm != null)
                    holder.icon.setImageBitmap(bm);
            }
            holder.btnDelete.setTag(content);

            String cource = content.getCourse();
            String subcource = content.getSubCourse();
            String title;
            if (cource != null && cource.length() > 0) {
                if (subcource != null && subcource.length() > 0)
                    title = cource + "(" + subcource + ")";
                else
                    title = cource;
            } else
                title = subcource;

            holder.fileName.setTextColor(mResources.getColor(R.color.text_color_dark_gray));
            holder.fileName.setPaintFlags(holder.fileName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.fileName.setText(title);

            holder.btnDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // TODO Auto-generated method stub
//				view.setVisibility(View.GONE);
                    mDownloadCancelListener.onDownloadCancel((KollusContent)view.getTag());
                }

            });

//            mBindView = true;
        }

        if(content.getDownloadError()) {
            holder.fileName.setTextColor(Color.RED);
            holder.fileName.setPaintFlags(holder.fileName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        String strRecvFileSize = DiskUtil.getStringSize(content.getReceivedSize());
        String strFileSize = DiskUtil.getStringSize(content.getFileSize());
        holder.fileSize.setText(String.format("%s / %s", strRecvFileSize, strFileSize));
        holder.timeBar.setProgress(content.getDownloadPercent());


        if(content.isCompleted()) {
            holder.txtPercent.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
        else {
            holder.txtPercent.setVisibility(View.VISIBLE);
            holder.txtPercent.setText(content.getDownloadPercent()+"%");
            holder.btnDelete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if(mContentsList == null)
            return 0;

        return mContentsList.size();
    }
}