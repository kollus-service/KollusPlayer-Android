package kollus.test.media.player.contents;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import kollus.test.media.R;


public class ContentsViewHolder extends RecyclerView.ViewHolder{
	public View listItem;
	public CheckBox check;

	public View folderLayer;
	public TextView folderName;

	public View fileLayer;
	public ImageView icon;
	public ImageView icDrm;
	public ImageView icHang;
	public TextView txtPercent;
	public TextView fileName;
	public TextView playTime;
	public TextView duration;
	public ProgressBar  timeBar;
	public ImageView btnDetail;

	public ContentsViewHolder(View itemView) {
		super(itemView);

		listItem = itemView.findViewById(R.id.list_item);
		check = (CheckBox) itemView.findViewById(R.id.list_check);

		folderLayer = itemView.findViewById(R.id.folder_field);
		folderName = (TextView) itemView.findViewById(R.id.folder_name);
//		folderName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//		folderName.setSelected(true);
//		folderName.setSingleLine(true);
//		folderName.setMarqueeRepeatLimit(-1);

		fileLayer = itemView.findViewById(R.id.file_field);

		icon = (ImageView) itemView.findViewById(R.id.icon);
		icDrm = (ImageView) itemView.findViewById(R.id.list_drm);
		txtPercent = (TextView) itemView.findViewById(R.id.list_percent);
		icHang = (ImageView) itemView.findViewById(R.id.list_hang);
		fileName = (TextView) itemView.findViewById(R.id.file_name);
//		fileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//		fileName.setSelected(true);
//		fileName.setSingleLine(true);
//		fileName.setMarqueeRepeatLimit(-1);
		playTime = (TextView) itemView.findViewById(R.id.play_time);
		duration = (TextView) itemView.findViewById(R.id.duration);
		timeBar = (ProgressBar)itemView.findViewById(R.id.time_progress);
		btnDetail = (ImageView) itemView.findViewById(R.id.btn_detail);
	}
}
