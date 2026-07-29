/* Ported from smartisanos.widget.tabswitcher.TabSwitcher in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SmartisanTabSwitcher extends SmartisanBottomBar {
  public interface OnTabSelectedListener {
    void onTabSelected(int tabId);
  }

  public interface OnTabsChangedListener {
    void onTabsChanged(List<Tab> bottomTabs, List<Tab> overflowTabs);
  }

  public interface OnEditorVisibilityChangedListener {
    void onEditorVisibilityChanged(boolean visible);
  }

  public static final class Tab {
    private final int id;
    private final CharSequence title;
    private final int bottomDrawable;
    private final int overflowDrawable;
    private final ColorStateList textColors;
    private final boolean movable;

    public Tab(int id, CharSequence title, int bottomDrawable, int overflowDrawable,
        boolean movable) {
      this(id, title, bottomDrawable, overflowDrawable, null, movable);
    }

    public Tab(int id, CharSequence title, int bottomDrawable, int overflowDrawable,
        ColorStateList textColors, boolean movable) {
      this.id = id;
      this.title = title;
      this.bottomDrawable = bottomDrawable;
      this.overflowDrawable = overflowDrawable;
      this.textColors = textColors;
      this.movable = movable;
    }

    public int getId() { return id; }
    public CharSequence getTitle() { return title; }
    public int getBottomDrawable() { return bottomDrawable; }
    public int getOverflowDrawable() { return overflowDrawable; }
    public boolean isMovable() { return movable; }

    @Override public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Tab)) return false;
      Tab tab = (Tab) object;
      return id == tab.id
          && bottomDrawable == tab.bottomDrawable
          && overflowDrawable == tab.overflowDrawable
          && movable == tab.movable
          && Objects.equals(title == null ? null : title.toString(),
              tab.title == null ? null : tab.title.toString())
          && Objects.equals(textColors, tab.textColors);
    }

    @Override public int hashCode() {
      return Objects.hash(id, title == null ? null : title.toString(), bottomDrawable,
          overflowDrawable, textColors, movable);
    }
  }

  private final int bottomBarHeight;
  private List<Tab> bottomTabs = Collections.emptyList();
  private List<Tab> overflowTabs = Collections.emptyList();
  private int selectedTabId = NONE;
  private int startInset;
  private int endInset;
  private int bottomInset;
  private boolean editable = true;
  private OnTabSelectedListener tabSelectedListener;
  private OnTabsChangedListener tabsChangedListener;
  private OnEditorVisibilityChangedListener editorVisibilityListener;
  private EditorOverlay editorOverlay;

  public SmartisanTabSwitcher(Context context) { this(context, null); }
  public SmartisanTabSwitcher(Context context, AttributeSet attrs) { this(context, attrs, 0); }
  public SmartisanTabSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    bottomBarHeight = getResources().getDimensionPixelSize(
        R.dimen.smartisan_rom_smartisan_bottom_bar_height_with_text_icon);
    super.setOnClickListener(view -> {
      int id = view.getId();
      if (id != selectedTabId) {
        selectedTabId = id;
        if (tabSelectedListener != null) tabSelectedListener.onTabSelected(id);
      }
    });
    super.setOnLongClickListener(view -> {
      if (!editable) return false;
      showEditor();
      return true;
    });
  }

  public void setTabs(List<Tab> bottom, List<Tab> overflow) {
    List<Tab> nextBottom = immutableCopy(bottom);
    List<Tab> nextOverflow = immutableCopy(overflow);
    if (nextBottom.equals(bottomTabs) && nextOverflow.equals(overflowTabs)) return;
    bottomTabs = nextBottom;
    overflowTabs = nextOverflow;
    renderBottomBar();
  }

  public List<Tab> getBottomTabs() { return bottomTabs; }
  public List<Tab> getOverflowTabs() { return overflowTabs; }

  public void setSelectedTabId(int id) {
    selectedTabId = id;
    setDefaultSelectedItem(id);
  }

  public int getSelectedTabId() { return selectedTabId; }
  public void setEditable(boolean value) { editable = value; if (!value) dismissEditor(false); }
  public boolean isEditable() { return editable; }
  public void setOnTabSelectedListener(OnTabSelectedListener listener) { tabSelectedListener = listener; }
  public void setOnTabsChangedListener(OnTabsChangedListener listener) { tabsChangedListener = listener; }
  public void setOnEditorVisibilityChangedListener(OnEditorVisibilityChangedListener listener) {
    editorVisibilityListener = listener;
  }

  public void setNavigationBarInsets(int start, int end, int bottom) {
    startInset = start;
    endInset = end;
    bottomInset = bottom;
    setPadding(start, 0, end, bottom);
    if (editorOverlay != null) editorOverlay.setInsets(start, end, bottom);
  }

  public boolean isEditorShown() {
    return editorOverlay != null && editorOverlay.isShownInWindow();
  }

  public void showEditor() {
    if (!editable || bottomTabs.isEmpty() || isEditorShown()) return;
    View root = getRootView().findViewById(android.R.id.content);
    if (!(root instanceof ViewGroup)) root = getRootView();
    if (!(root instanceof ViewGroup)) return;
    if (editorOverlay == null) editorOverlay = new EditorOverlay(getContext());
    ViewGroup parent = (ViewGroup) root;
    if (editorOverlay.getParent() instanceof ViewGroup) {
      ((ViewGroup) editorOverlay.getParent()).removeView(editorOverlay);
    }
    parent.addView(editorOverlay, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    editorOverlay.show(bottomTabs, overflowTabs, selectedTabId);
    if (editorVisibilityListener != null) editorVisibilityListener.onEditorVisibilityChanged(true);
  }

  public void dismissEditor(boolean commit) {
    if (editorOverlay != null && editorOverlay.isShownInWindow()) editorOverlay.dismiss(commit);
  }

  private void renderBottomBar() {
    clearItems();
    for (Tab tab : bottomTabs) {
      ColorStateList colors = tab.textColors != null
          ? tab.textColors
          : getResources().getColorStateList(R.color.smartisan_rom_bottom_tab_text_color);
      addBarItem(tab.id, string(tab.title), tab.bottomDrawable, -1, colors);
    }
    if (selectedTabId != NONE) setDefaultSelectedItem(selectedTabId);
    setup(true);
  }

  private static List<Tab> immutableCopy(List<Tab> tabs) {
    if (tabs == null || tabs.isEmpty()) return Collections.emptyList();
    return Collections.unmodifiableList(new ArrayList<>(tabs));
  }

  private static String string(CharSequence value) { return value == null ? "" : value.toString(); }
  private int dp(float value) {
    return (int) (value * getResources().getDisplayMetrics().density + .5f);
  }

  @Override protected void onDetachedFromWindow() {
    if (editorOverlay != null) editorOverlay.removeImmediately();
    super.onDetachedFromWindow();
  }

  private final class EditorOverlay extends FrameLayout {
    private static final long SHOW_DURATION = 220L;
    private static final long HIDE_DURATION = 180L;
    private static final String DRAG_LABEL = "smartisan-tab";
    private final SmartisanDialogTitleBar titleBar;
    private final LinearLayout overflowZone;
    private final SmartisanBottomBar bottomZone;
    private final LinearLayout panel;
    private final TextView accessibilityAnnouncement;
    private List<Tab> draftBottom = new ArrayList<>();
    private List<Tab> draftOverflow = new ArrayList<>();
    private boolean visible;
    private boolean dismissing;
    private int draftSelectedId;

    EditorOverlay(Context context) {
      super(context);
      setBackgroundColor(0x99000000);
      setClickable(true);
      setFocusableInTouchMode(true);
      setOnClickListener(view -> dismiss(false));

      titleBar = new SmartisanDialogTitleBar(context);
      titleBar.setTitle(R.string.smartisan_tab_editor_title);
      titleBar.setLeftButtonVisibility(VISIBLE);
      titleBar.setRightButtonVisibility(VISIBLE);
      titleBar.setLeftImageViewResource(R.drawable.smartisan_rom_standard_icon_cancel_selector);
      titleBar.setRightImageViewResource(R.drawable.smartisan_rom_standard_icon_complete_selector);
      titleBar.getLeftImageView().setContentDescription(
          context.getString(R.string.smartisan_tab_editor_cancel));
      titleBar.getRightImageView().setContentDescription(
          context.getString(R.string.smartisan_tab_editor_done));
      titleBar.setOnLeftButtonClickListener(view -> dismiss(false));
      titleBar.setOnRightButtonClickListener(view -> dismiss(true));

      overflowZone = new LinearLayout(context);
      overflowZone.setOrientation(LinearLayout.HORIZONTAL);
      overflowZone.setGravity(Gravity.START);
      overflowZone.setWeightSum(5f);
      overflowZone.setBackgroundColor(0xfff5f5f5);

      bottomZone = new SmartisanBottomBar(context);
      bottomZone.setBackgroundColor(Color.WHITE);

      panel = new LinearLayout(context);
      panel.setOrientation(LinearLayout.VERTICAL);
      panel.setClickable(true);
      panel.addView(titleBar, new LinearLayout.LayoutParams(
          LayoutParams.MATCH_PARENT, dp(48)));
      panel.addView(overflowZone, new LinearLayout.LayoutParams(
          LayoutParams.MATCH_PARENT, dp(80)));
      panel.addView(bottomZone, new LinearLayout.LayoutParams(
          LayoutParams.MATCH_PARENT, bottomBarHeight));
      addView(panel, new FrameLayout.LayoutParams(
          LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

      accessibilityAnnouncement = new TextView(context);
      accessibilityAnnouncement.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
      accessibilityAnnouncement.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
      accessibilityAnnouncement.setTextColor(Color.TRANSPARENT);
      addView(accessibilityAnnouncement, new FrameLayout.LayoutParams(1, 1, Gravity.TOP | Gravity.START));
    }

    boolean isShownInWindow() { return visible || dismissing; }

    void setInsets(int start, int end, int bottom) {
      overflowZone.setPadding(start, 0, end, 0);
      bottomZone.setPadding(start, 0, end, bottom);
      ViewGroup.LayoutParams params = bottomZone.getLayoutParams();
      if (params != null) {
        params.height = bottomBarHeight + bottom;
        bottomZone.setLayoutParams(params);
      }
    }

    void show(List<Tab> bottom, List<Tab> overflow, int selectedId) {
      draftBottom = new ArrayList<>(bottom);
      draftOverflow = new ArrayList<>(overflow);
      draftSelectedId = selectedId;
      visible = true;
      dismissing = false;
      setInsets(startInset, endInset, bottomInset);
      render();
      setAlpha(0f);
      panel.post(() -> {
        panel.setTranslationY(panel.getHeight());
        animate().alpha(1f).setDuration(SHOW_DURATION).start();
        panel.animate().translationY(0f).setDuration(SHOW_DURATION).start();
      });
      requestFocus();
    }

    void dismiss(boolean commit) {
      if (!visible || dismissing) return;
      visible = false;
      dismissing = true;
      if (commit) {
        bottomTabs = immutableCopy(draftBottom);
        overflowTabs = immutableCopy(draftOverflow);
        if (!containsId(bottomTabs, draftSelectedId)) {
          Tab replacement = firstSelectable(bottomTabs);
          if (replacement != null) selectedTabId = replacement.id;
        }
        renderBottomBar();
        if (tabsChangedListener != null) tabsChangedListener.onTabsChanged(bottomTabs, overflowTabs);
      }
      panel.animate().translationY(panel.getHeight()).setDuration(HIDE_DURATION).start();
      animate().alpha(0f).setDuration(HIDE_DURATION).withEndAction(() -> {
        dismissing = false;
        removeImmediately();
        if (editorVisibilityListener != null) editorVisibilityListener.onEditorVisibilityChanged(false);
      }).start();
    }

    void removeImmediately() {
      visible = false;
      dismissing = false;
      animate().cancel();
      panel.animate().cancel();
      if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).removeView(this);
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
      if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
        dismiss(false);
        return true;
      }
      return super.dispatchKeyEvent(event);
    }

    private void render() {
      renderOverflow();
      renderBottom();
    }

    private void renderOverflow() {
      overflowZone.removeAllViews();
      for (Tab tab : draftOverflow) {
        SmartisanBottomBarItemView item = createItem(tab, true);
        overflowZone.addView(item, new LinearLayout.LayoutParams(
            0, LayoutParams.MATCH_PARENT, 1f));
      }
    }

    private void renderBottom() {
      bottomZone.clearItems();
      for (Tab tab : draftBottom) {
        ColorStateList colors = tab.textColors != null
            ? tab.textColors
            : getResources().getColorStateList(R.color.smartisan_rom_bottom_tab_text_color);
        bottomZone.addBarItem(tab.id, string(tab.title), tab.bottomDrawable, -1, colors);
      }
      bottomZone.setDefaultSelectedItem(draftSelectedId);
      bottomZone.setup(true);
      for (Tab tab : draftBottom) {
        SmartisanBottomBarItemView item = bottomZone.getItemView(tab.id);
        if (item != null) configureEditorItem(item, tab, false);
      }
    }

    private SmartisanBottomBarItemView createItem(Tab tab, boolean overflow) {
      SmartisanBottomBarItemView item = new SmartisanBottomBarItemView(getContext());
      item.setId(tab.id);
      item.setScaleable(false);
      item.setDrawableResource(overflow ? tab.overflowDrawable : tab.bottomDrawable);
      item.setText(string(tab.title));
      item.setTextColor(tab.textColors != null
          ? tab.textColors
          : getResources().getColorStateList(R.color.smartisan_rom_bottom_tab_text_color));
      item.setPadding(0, overflow ? dp(11) : 0, 0, 0);
      configureEditorItem(item, tab, overflow);
      return item;
    }

    private void configureEditorItem(View item, Tab tab, boolean overflow) {
      item.setContentDescription(getContext().getString(
          overflow ? R.string.smartisan_tab_overflow_description
              : R.string.smartisan_tab_bottom_description,
          tab.title));
      item.setOnLongClickListener(view -> startDrag(view, tab));
      item.setOnDragListener((view, event) -> handleDrag(tab, event));
      item.setAccessibilityDelegate(new TabAccessibilityDelegate(tab, overflow));
    }

    private boolean startDrag(View view, Tab tab) {
      if (!tab.movable) return false;
      ClipData data = ClipData.newPlainText(DRAG_LABEL, String.valueOf(tab.id));
      View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
      if (Build.VERSION.SDK_INT >= 24) return view.startDragAndDrop(data, shadow, tab, 0);
      return view.startDrag(data, shadow, tab, 0);
    }

    private boolean handleDrag(Tab target, DragEvent event) {
      Object state = event.getLocalState();
      if (!(state instanceof Tab)) return false;
      Tab source = (Tab) state;
      if (!source.movable || !target.movable) return false;
      if (event.getAction() == DragEvent.ACTION_DROP) {
        if (source.id != target.id) swap(source, target, true);
        return true;
      }
      return event.getAction() == DragEvent.ACTION_DRAG_STARTED
          || event.getAction() == DragEvent.ACTION_DRAG_ENTERED
          || event.getAction() == DragEvent.ACTION_DRAG_LOCATION
          || event.getAction() == DragEvent.ACTION_DRAG_EXITED
          || event.getAction() == DragEvent.ACTION_DRAG_ENDED;
    }

    private void swap(Tab first, Tab second, boolean announce) {
      List<Tab> firstList = listContaining(first.id);
      List<Tab> secondList = listContaining(second.id);
      if (firstList == null || secondList == null) return;
      int firstIndex = indexOf(firstList, first.id);
      int secondIndex = indexOf(secondList, second.id);
      firstList.set(firstIndex, second);
      secondList.set(secondIndex, first);
      render();
      if (announce) announce(getContext().getString(
          R.string.smartisan_tab_items_swapped, first.title, second.title));
    }

    private void move(Tab tab, int offset) {
      List<Tab> list = listContaining(tab.id);
      if (list == null) return;
      int from = indexOf(list, tab.id);
      int to = Math.max(0, Math.min(list.size() - 1, from + offset));
      if (from == to || !list.get(to).movable) return;
      list.remove(from);
      list.add(to, tab);
      render();
      announce(getContext().getString(R.string.smartisan_tab_item_moved, tab.title));
    }

    private void swapZone(Tab tab) {
      if (!tab.movable) return;
      if (containsId(draftBottom, tab.id)) {
        Tab target = firstSelectable(draftOverflow);
        if (target != null) swap(tab, target, true);
      } else {
        Tab target = lastSelectable(draftBottom);
        if (target != null) swap(tab, target, true);
      }
    }

    private List<Tab> listContaining(int id) {
      if (containsId(draftBottom, id)) return draftBottom;
      if (containsId(draftOverflow, id)) return draftOverflow;
      return null;
    }

    private void announce(CharSequence message) {
      accessibilityAnnouncement.setText("");
      accessibilityAnnouncement.post(() -> accessibilityAnnouncement.setText(message));
    }

    private final class TabAccessibilityDelegate extends View.AccessibilityDelegate {
      private final Tab tab;
      private final boolean overflow;

      TabAccessibilityDelegate(Tab tab, boolean overflow) {
        this.tab = tab;
        this.overflow = overflow;
      }

      @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        info.setLongClickable(false);
        info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK);
        List<Tab> list = overflow ? draftOverflow : draftBottom;
        int index = indexOf(list, tab.id);
        if (index > 0 && list.get(index - 1).movable) {
          info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
              R.id.smartisan_tab_action_move_earlier,
              getContext().getString(R.string.smartisan_tab_move_earlier)));
        }
        if (index >= 0 && index < list.size() - 1 && list.get(index + 1).movable) {
          info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
              R.id.smartisan_tab_action_move_later,
              getContext().getString(R.string.smartisan_tab_move_later)));
        }
        if (tab.movable && !(overflow ? draftBottom : draftOverflow).isEmpty()) {
          info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
              R.id.smartisan_tab_action_swap_zone,
              getContext().getString(overflow
                  ? R.string.smartisan_tab_pin_replacing
                  : R.string.smartisan_tab_unpin_replacing)));
        }
      }

      @Override public boolean performAccessibilityAction(View host, int action, Bundle args) {
        if (action == R.id.smartisan_tab_action_move_earlier) { move(tab, -1); return true; }
        if (action == R.id.smartisan_tab_action_move_later) { move(tab, 1); return true; }
        if (action == R.id.smartisan_tab_action_swap_zone) { swapZone(tab); return true; }
        return super.performAccessibilityAction(host, action, args);
      }
    }
  }

  private static boolean containsId(List<Tab> tabs, int id) { return indexOf(tabs, id) >= 0; }
  private static int indexOf(List<Tab> tabs, int id) {
    for (int i = 0; i < tabs.size(); i++) if (tabs.get(i).id == id) return i;
    return -1;
  }
  private static Tab firstSelectable(List<Tab> tabs) {
    for (Tab tab : tabs) if (tab.movable) return tab;
    return null;
  }
  private static Tab lastSelectable(List<Tab> tabs) {
    for (int i = tabs.size() - 1; i >= 0; i--) if (tabs.get(i).movable) return tabs.get(i);
    return null;
  }
}
