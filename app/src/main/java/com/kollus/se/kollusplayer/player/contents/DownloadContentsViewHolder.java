package com.kollus.se.kollusplayer.player.contents;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kollus.se.kollusplayer.R;



public class DownloadContentsViewHolder extends RecyclerView.ViewHolder{
	public View listItem;
	public ImageView icon;
	public TextView txtPercent;
	public TextView fileName;
	public ProgressBar timeBar;
	public TextView fileSize;
	public ImageView btnDelete;

	public DownloadContentsViewHolder(View itemView) {
		super(itemView);

		listItem = itemView.findViewById(R.id.list_item);

		icon = (ImageView) itemView.findViewById(R.id.icon);
		txtPercent = (TextView) itemView.findViewById(R.id.list_percent);

		fileName = (TextView) itemView.findViewById(R.id.file_name);
//		fileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//		fileName.setSelected(true);
//		fileName.setSingleLine(true);
//		fileName.setMarqueeRepeatLimit(-1);
		timeBar = (ProgressBar)itemView.findViewById(R.id.download_progress);
		fileSize = (TextView) itemView.findViewById(R.id.file_size);

		btnDelete = (ImageView) itemView.findViewById(R.id.download_cancel);
	}
}
