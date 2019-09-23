package kollus.test.media.player.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import kollus.test.media.R;


public class CaptionControlButton extends androidx.appcompat.widget.AppCompatRadioButton {
	private static String TAG = CaptionControlButton.class.getSimpleName();
	
	private String mLanguageCode;
	private int mLabelWidth;
	private int mLabelHeight;
	private int mPadding;
	private float mLabelRound;

    public CaptionControlButton(Context context) {
    	super(context);
    	init(context);
    }

    public CaptionControlButton(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	init(context);
    }

    public CaptionControlButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mLabelWidth = (int)(metrics.density*30);
        mLabelHeight = (int)(metrics.density*20);
        mLabelRound = metrics.density*3;
        mPadding = (int)(metrics.density*5);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onDraw(Canvas canvas) {
    	String text = this.getText().toString();
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        
        float currentWidth = textPaint.measureText(text);
        float currentHeight = textPaint.measureText("X");

        textPaint.setTextSize(this.getTextSize());
//        textPaint.setTextAlign(Paint.Align.CENTER);

        if (isChecked()) {
        	setBackgroundColor(getResources().getColor(R.color.progress_blue_color));
        } else {
        	setBackgroundColor(Color.TRANSPARENT);
        }
        
        float x = 0;
        float y = (this.getHeight()-currentHeight)/2+currentHeight;
        int l=0, t=0, r=0, b=0;
        
    	l = mPadding;
        t = (this.getHeight()-mLabelHeight)/2;
        r = l+mLabelWidth;
        b = t+mLabelHeight;
      
        x = r+mPadding;
        y = b-(mLabelHeight-currentHeight)/4;
      
        if(this.getId() > 0) {
	        Paint labelPaint = new Paint();
	        labelPaint.setColor(Color.GRAY);
	        labelPaint.setStrokeWidth(1);
	        labelPaint.setStyle(Paint.Style.FILL);	        
	        
	        RectF labelRect = new RectF((float)l, (float)t, (float)r, (float)b);
//	        label.setBounds(labelRect);
//	        label.draw(canvas);
	        canvas.drawRoundRect(labelRect, mLabelRound, mLabelRound, labelPaint);
	        
	        if(mLanguageCode != null) {
	        	int labelW = (int)textPaint.measureText(mLanguageCode);
	        	canvas.drawText(mLanguageCode, l+(labelRect.width()-labelW)/2, y, textPaint);
	        }
        }
        else {
        	x = mPadding;
        }
        this.setWidth(this.getWidth()+mLabelWidth+mPadding);
        this.setHeight(mLabelHeight+mPadding*2);
        
        canvas.drawText(text, x, y, textPaint);
    }
    
    public void setLanguageCode(String code) {
    	mLanguageCode = code.toUpperCase();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
    }

}
