package com.editor.text;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.example.xeditor.R;

public class CodeEditor extends ViewGroup {

	private Paint backPaint;		 	//绘制当前行的背景色画笔
	private GestureDetector detector;	//手势对象
	private int rectStartX,rectStartY;	//绘制当前行的背景色的开始X Y坐标
	private int rectEndX,rectEndY;	  	//绘制当前行的背景色的结束X Y坐标
	private int rowHeight,currRow;		//行高 当前行
	private int totalRows;				//总共行数

	private int scrollX,scrollY;		//滚动的X Y距离
	private InputMethodManager imm;		//输入法对象
	private boolean isKeyEnter = false;	//是否按下确定键
	private boolean isKeyDelete = false;//是否按下删除键
	private int viewWidth,viewHeight;	//View的宽度和高度
	private int mChildHeight,srcRow;	//n-1个子View的高度
	private TextEditorView textEditor;	//自定义的EditText对象

	private int screenWidth,screenHeight;//屏幕的宽度和高度

	private final int MARGIN_LEFT = 100;//文本左侧的间距


	public CodeEditor(Context context) {
		super(context, null);
	}


	public CodeEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	public CodeEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setTextEditor(TextEditorView textEditor) {
		this.textEditor = textEditor;
	}

	public TextEditorView getTextEditor() {
		return textEditor;

	}




	/* 初始化一些参数 */
	public void init(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = windowManager.getDefaultDisplay().getWidth();
		screenHeight = windowManager.getDefaultDisplay().getHeight();

		imm = (InputMethodManager)getContext()
			.getSystemService(Context.INPUT_METHOD_SERVICE);
		detector = new GestureDetector(new GestureListener(this));
		setBackgroundColor(context.getResources()
						   .getColor(R.color.editor_backgroud));
		backPaint = new Paint();
		backPaint.setColor(context.getResources()
						   .getColor(R.color.current_row_backgroud));
		rectStartX = MARGIN_LEFT;
	}




	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		//获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMode);
		int heightSize = MeasureSpec.getSize(heightMode);

		calcuDimension(widthMode, heightMode, widthSize, heightSize);

		// 计算出所有的childView的宽和高
		measureChildren(widthMeasureSpec, heightMeasureSpec);

	}



	/* 计算子View的大小 */
	public void calcuDimension(int widthMode, int heightMode
							   , int widthSize, int heightSize) {

		//计算子View的尺寸
		int width=0,height=0;
		int childWidth=0,childHeight=0;
		MarginLayoutParams mLayoutParams = null;

		for (int i=0;i < getChildCount();i++) {
			View childView = getChildAt(i);
			childWidth = getChildAt(i).getMeasuredWidth();
			childHeight = getChildAt(i).getMeasuredHeight();
			mLayoutParams = (MarginLayoutParams)childView.getLayoutParams();

			width = childWidth + mLayoutParams.leftMargin + mLayoutParams.rightMargin ;
			height = childHeight + mLayoutParams.topMargin + mLayoutParams.bottomMargin;

		}

		//设置View的尺寸
		if (width < screenWidth) {
			width = screenWidth + screenWidth / 2 ;
		} else {
			width = width + screenWidth / 2;
		}

		if (height < screenHeight) {
			height = screenHeight + screenHeight / 2;
		} else {
			height = height + screenHeight / 2;
		}
		rectEndX = width;

		//如果是wrap_content设置为我们计算的值，否则：直接设置为父容器计算的值
		setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? widthSize
							 : width, (heightMode == MeasureSpec.EXACTLY) ? heightSize: height);
	}




	/* 
	 * 父View对子View进行布局，按垂直线性进行布局
	 * 设置子View的宽度在为 childWidth +rectEnd
	 * 否则在子View外单击，不会改变当前行的背景色
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {



		int childWidth=0,childHeight=0;
		MarginLayoutParams mLayoutParams = null;
		for (int i=0;i < getChildCount();i++) {
			View childView = getChildAt(i);
			childWidth = getChildAt(i).getMeasuredWidth();
			childHeight = getChildAt(i).getMeasuredHeight();
			mLayoutParams = (MarginLayoutParams) childView.getLayoutParams();


			l = mLayoutParams.leftMargin;
			if (i == 0) {
				t = mLayoutParams.topMargin;
			} else {
				t = b + mLayoutParams.topMargin;
			}

			r = l + childWidth + rectEndX ;
			b = t + childHeight ;

			childView.layout(l, t, r, b);

		}


	}




	/* 这三个方法必须覆写，否则无法获得MarginLayoutParams的对象和属性 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new MarginLayoutParams(LayoutParams.FILL_PARENT,
									  LayoutParams.FILL_PARENT);
	}

	@Override
	protected LayoutParams generateLayoutParams(
		LayoutParams p) {
		return new MarginLayoutParams(p);
	}



	@Override
	protected void onDraw(Canvas canvas) {
		// TODO: Implement this method
		super.onDraw(canvas);
		//此处必须强制刷新，否则子View不会时时刷新
		invalidate();
	}


	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO: Implement this method
		super.dispatchDraw(canvas);
		canvas.save();
		textEditor = (TextEditorView) getChildAt(2);
		rowHeight = textEditor.getRowHeight();
		currRow = textEditor.getCurrRow();
		totalRows = textEditor.getTotalRows();
		//如果当前选择的行，与之前选择的行不同，才进行计算，提高效率
		if (srcRow != currRow) {
			int totalHeight = 0;
			rectEndY = currRow * rowHeight + rowHeight / 5;
			rectStartY = rectEndY - rowHeight - rowHeight / 10;
			for (int i=0;i < getChildCount() - 1;i++) {
				totalHeight += getChildAt(i).getMeasuredHeight();
			}
			mChildHeight = totalHeight;
			srcRow = currRow;

		}
	}


	//必须在此方法中绘制当前行的背景色，否则背景色会覆盖文本
	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		// TODO: Implement this method
		textEditor = (TextEditorView) getChildAt(2);
		if (!textEditor.hasSelection()) {
			//调整Y的坐标
			if ((currRow + 1) * rowHeight + rowHeight / 4 - scrollY >= viewHeight - mChildHeight
				&& isKeyEnter == true) {
				rectEndY = scrollY + viewHeight  - mChildHeight;
				rectStartY = rectEndY - rowHeight;
			}

			if ((currRow + 1) * rowHeight - scrollY <= rowHeight / 2 
				&& isKeyDelete == true) {
				rectEndY =  scrollY - rowHeight - rowHeight / 4;
				rectStartY = rectEndY - rowHeight;
			} 

			canvas.drawRect(rectStartX, rectStartY + mChildHeight, rectEndX, rectEndY + mChildHeight, backPaint);
		}

		canvas.restore();
		return super.drawChild(canvas, child, drawingTime);
	}



	/* 此处交给手势去处理，就能在子View以外单击也能弹出输入法 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO: Implement this method
		isKeyEnter = false;
		isKeyDelete = false;
		return detector.onTouchEvent(event);
	}


	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO: Implement this method
		isKeyEnter = false;
		isKeyDelete = false;
		return super.dispatchTouchEvent(ev);
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO: Implement this method
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_ENTER:
				isKeyEnter = true;
				isKeyDelete = false;
				break;
			case KeyEvent.KEYCODE_DEL:
				isKeyEnter = false;
				isKeyDelete = true;
				break;
			default:
				isKeyEnter = false;
				isKeyDelete = false;
				break;
		}
		return super.dispatchKeyEvent(event);
	}



	/* 显示隐藏输入法 */
	public void showIME(boolean show) {
		if (show) {
			imm.showSoftInput(textEditor, 0);

		} else {
			imm.hideSoftInputFromWindow(textEditor.getWindowToken(), 0); 
		}
	}


	/* 获得View的宽度和高度 */
	public void getViewWidth(int viewWidth) {
		this.viewWidth = viewWidth;
	}

	public void getViewHeight(int viewHeight) {
		this.viewHeight = viewHeight;
	}


	/* 获得View滚动的X Y距离 */
	public void scrollX(int scrollX) {
		this.scrollX = scrollX;
	}   

	public void scrollY(int scrollY) {
		this.scrollY = scrollY;
	}


	/* 获得EditText的行 行高 当前行 */
	public int getRowHeight() {
		return this.rowHeight;
	}

	public int getTotalRows() {
		return this.totalRows;
	}

	public int getCurrRow() {
		return this.currRow;
	}

}
