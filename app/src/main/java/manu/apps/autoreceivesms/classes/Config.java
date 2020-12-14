package manu.apps.autoreceivesms.classes;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import manu.apps.autoreceivesms.R;

public class Config {

    public static void showDismissibleDialog(Context context, String tvMessage, String btnText, Drawable imvDrawable)  {

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_dismissible_dialog);
        dialog.show();
        dialog.setCancelable(false);

        // Setting dialog background to transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        // Setting size of the dialog
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        ImageView imvDismissibleDialog = dialog.findViewById(R.id.imv_dismissible_dialog);
        TextView tvDismissibleDialog = dialog.findViewById(R.id.tv_dismissible_dialog_message);
        MaterialButton btnDismissibleDialog = dialog.findViewById(R.id.btn_dismissible_dialog);


        imvDismissibleDialog.setImageDrawable(imvDrawable);
        tvDismissibleDialog.setText(tvMessage);
        btnDismissibleDialog.setText(btnText);

        btnDismissibleDialog.setOnClickListener(v -> dialog.dismiss());
    }
}
