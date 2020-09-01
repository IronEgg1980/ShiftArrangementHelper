package enthusiast.yzw.shift_arrangement_helper.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import enthusiast.yzw.shift_arrangement_helper.R;
import enthusiast.yzw.shift_arrangement_helper.enums.DialogResult;

public class ConfirmDialog extends DialogFragment {
    public static ConfirmDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        ConfirmDialog fragment = new ConfirmDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private DialogDissmissListener dialogDissmissListener;
    private String mTitle = "title", mMessage = "message";
    private DialogResult mResult = DialogResult.CANCEL;

    public ConfirmDialog setDialogDissmissListener(DialogDissmissListener dialogDissmissListener) {
        this.dialogDissmissListener = dialogDissmissListener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString("title");
            mMessage = bundle.getString("message");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_message_confirm, container, false);
        TextView textView1 = view.findViewById(R.id.textview_dialog_title);
        textView1.setText(mTitle);
        TextView textView2 = view.findViewById(R.id.textview_dialog_message);
        textView2.setText(mMessage);
        view.findViewById(R.id.textview_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResult = DialogResult.CANCEL;
                dismiss();
            }
        });
        view.findViewById(R.id.textview_dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResult = DialogResult.CONFIRM;
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dialogDissmissListener != null) {
            dialogDissmissListener.onDissmiss(mResult);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) (dm.widthPixels * 0.8);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            if(window!=null){
                window.setLayout(width,height);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }
}
