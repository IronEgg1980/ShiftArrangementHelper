package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;

public class InputTextDialog extends DialogFragment {
    public static InputTextDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putString("title", title);
        InputTextDialog fragment = new InputTextDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static InputTextDialog newInstance(String message) {
        return newInstance("文本输入", message);
    }

    private DialogDissmissListener listener;
    private EditText editText;
    private String message = "", title = "";
    private DialogResult result = DialogResult.CANCEL;

    public InputTextDialog setListener(DialogDissmissListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null)
            listener.onDissmiss(result, message);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            message = bundle.getString("message");
            title = bundle.getString("title");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_input_text, container, false);
        TextView textView = view.findViewById(R.id.textview_dialog_title);
        textView.setText(title);
        editText = view.findViewById(R.id.edittext_dialog_input);
        editText.setText(message);
        view.findViewById(R.id.textview_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result = DialogResult.CANCEL;
                dismiss();
            }
        });
        view.findViewById(R.id.textview_dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = editText.getText() == null ? "" : editText.getText().toString();
                result = DialogResult.CONFIRM;
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) (dm.widthPixels * 0.7);
//        int height =(int) (dm.heightPixels * 0.5);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(width, height);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            editText.selectAll();
            editText.requestFocus();
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                }
            },100);
        }
    }
}
