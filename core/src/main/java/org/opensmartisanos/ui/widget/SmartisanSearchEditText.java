/* Ported from smartisanos.widget.SearchBarEditText in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.CompletionInfo;
import android.widget.EditText;

public class SmartisanSearchEditText extends EditText {
    public interface OnCommitCompletionListener {
        void onCommitCompletion(CompletionInfo completion);
    }
    private OnCommitCompletionListener listener;
    public SmartisanSearchEditText(Context context) { super(context); }
    public SmartisanSearchEditText(Context context, AttributeSet attrs) { super(context, attrs); }
    public SmartisanSearchEditText(Context context, AttributeSet attrs, int style) { super(context, attrs, style); }
    public void setOnCommitCompletionListener(OnCommitCompletionListener listener) { this.listener = listener; }
    @Override public void onCommitCompletion(CompletionInfo completion) {
        super.onCommitCompletion(completion);
        if (listener != null) listener.onCommitCompletion(completion);
    }
}
