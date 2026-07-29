/* Ported from smartisanos.widget.BottomBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opensmartisanos.ui.R;
import java.util.ArrayList;
import java.util.List;

public class SmartisanBottomBar extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    public static final int NONE=-1, STYLE_DEFAULT=0, STYLE_ALIGN_EDGE=1, STYLE_BOTTOM_TAB=2;
    public interface OnCheckedChangeListener { void onCheckedChanged(ViewGroup group, int id); }
    private final List<BarItem> items = new ArrayList<>(); private LinearLayout container; private int checkedId=-1, styleFlag; private boolean scalable, hasTextAndIcon; private OnCheckedChangeListener checkedListener; private View.OnClickListener clickListener; private View.OnLongClickListener longClickListener; private final ImageView shadow;
    public SmartisanBottomBar(Context c) { this(c,null); } public SmartisanBottomBar(Context c, AttributeSet a) { this(c,a,0); }
    public SmartisanBottomBar(Context c, AttributeSet a, int s) { super(c,a,s); setBackgroundColor(0xffffffff); shadow = new ImageView(c); shadow.setBackgroundResource(R.drawable.smartisan_rom_bottom_bar_shadow); addView(shadow, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.smartisan_rom_bottom_bar_shadow_height), Gravity.TOP)); }
    public void setDefaultSelectedItem(int id) { if (id != checkedId) { setChecked(id,true); checkedId=id; } }
    private void setChecked(int id, boolean value) { if (container != null && container.findViewById(id) instanceof SmartisanBottomBarItemView) ((SmartisanBottomBarItemView)container.findViewById(id)).setChecked(value); }
    public void setStyleFlag(int flag) { styleFlag=flag; }
    public void setup() { setup(scalable); }
    public void setup(boolean scale) { if (container != null) removeView(container); scalable=scale; if (items.isEmpty()) return; container=new LinearLayout(getContext()); container.setGravity(Gravity.CENTER); int width=getResources().getDisplayMetrics().widthPixels/5; for (BarItem item:items) { SmartisanBottomBarItemView child=create(item); container.addView(child,new LinearLayout.LayoutParams(width,hasTextAndIcon?dp(54):dp(48))); } fillStyle(); addView(container); SmartisanBottomBarItemView selected=container.findViewById(checkedId); if(selected==null) selected=(SmartisanBottomBarItemView)container.getChildAt(0); selected.setChecked(true); }
    private SmartisanBottomBarItemView create(BarItem item) { SmartisanBottomBarItemView child=new SmartisanBottomBarItemView(getContext()); child.setScaleable(scalable); child.setId(item.id); child.setOnCheckedChangeListener((view,value)->{ if(value){ if(checkedId!=-1&&checkedId!=view.getId())setChecked(checkedId,false); checkedId=view.getId(); if(checkedListener!=null)checkedListener.onCheckedChanged(container,checkedId); }}); child.setOnClickListener(this); child.setOnLongClickListener(this); if(item.drawable!=-1) child.setDrawableResource(item.drawable); hasTextAndIcon=!TextUtils.isEmpty(item.name); if(hasTextAndIcon){child.setText(item.name); child.setTextColor(item.textColors);} if(item.tint!=-1)child.setDrawableColorList(item.tint); return child; }
    private void fillStyle(){ int count=container.getChildCount(); if((styleFlag&STYLE_ALIGN_EDGE)!=0&&count>1){ for(int i=1;i<count;i+=2){ View spacer=new View(getContext()); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,1,1); container.addView(spacer,i); count++; } } else if((styleFlag&STYLE_BOTTOM_TAB)!=0&&count>=3){ int item=dp(48); int gap=(getResources().getDisplayMetrics().widthPixels-getPaddingLeft()-getPaddingRight()-item*count)/(count-1); for(int i=0;i<count;i++){LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)container.getChildAt(i).getLayoutParams();p.width=item;p.leftMargin=i==0?0:gap;}} }
    public View getShadowView(){return shadow;} public void setShadowViewVisible(boolean v){shadow.setVisibility(v?VISIBLE:GONE);} public void setOnCheckedChangeListener(OnCheckedChangeListener l){checkedListener=l;} @Override public void setOnClickListener(View.OnClickListener l){clickListener=l;} @Override public void setOnLongClickListener(View.OnLongClickListener l){longClickListener=l;} @Override public void onClick(View v){if(clickListener!=null)clickListener.onClick(v);} @Override public boolean onLongClick(View v){return longClickListener!=null&&longClickListener.onLongClick(v);}
    public BarItem addBarItem(int id,String name,int drawable,int tint,ColorStateList text){BarItem item=new BarItem(id,name,drawable,tint,text);items.add(item);return item;} public BarItem addBarItem(int id,String name,int drawable,int tint){return addBarItem(id,name,drawable,tint,getResources().getColorStateList(R.color.smartisan_rom_bottom_tab_text_color));} public BarItem addBarItem(int id,String name,int drawable){return addBarItem(id,name,drawable,-1);} public void clearItems(){items.clear();}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);} public final class BarItem { final int id,drawable,tint; final String name; final ColorStateList textColors; private int contentDescription=-1,textSize=-1; BarItem(int i,String n,int d,int t,ColorStateList c){id=i;name=n;drawable=d;tint=t;textColors=c;} public void setContentDescription(int value){contentDescription=value;} public void setTextSize(int value){textSize=value;} }
}
