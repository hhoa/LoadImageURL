package com.example.hhoa.loadimageurl;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class NoInternetDialog extends Dialog
                                implements View.OnClickListener {
    Context context;
    public NoInternetDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_no_internet);

        Button btnAgain = findViewById(R.id.btn_again);
        btnAgain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(checkInternetConnection())
        {
            dismiss();
        }
    }

    public boolean checkInternetConnection()
    {
        Connectivity cn = new Connectivity();
        if (cn.isConnected(context))
        {
            if (cn.isConnectedMobile(context))
            {
                Toast.makeText(context, context.getString(R.string.mobile), Toast.LENGTH_SHORT).show();
            }
            else if (cn.isConnectedWifi(context))
                Toast.makeText(context, context.getString(R.string.wifi), Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            Toast.makeText(context, context.getString(R.string.err_connectivity), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
