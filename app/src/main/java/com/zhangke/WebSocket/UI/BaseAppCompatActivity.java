package com.zhangke.WebSocket.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;


/**
 * AppCompatActivity 基类
 * Created by 张可 on 2017/5/22.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity implements IBaseActivity {

    protected final String TAG = this.getClass().getSimpleName();

    private RoundProgressDialog roundProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        roundProgressDialog = new RoundProgressDialog(this);
        initBind();
        initView();
        initView(savedInstanceState);
    }

    protected abstract int getLayoutResId();

    /**
     * 绑定服务注册 ButterKnife 等等
     */
    protected void initBind() {

    }

    protected abstract void initView();

    protected void initView(@Nullable Bundle savedInstanceState) {
    }

    /**
     * 全屏，隐藏状态栏
     */
    protected void fullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void initToolbar(Toolbar toolbar, String title, boolean showBackBtn) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(showBackBtn);
        if (showBackBtn) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void showToastMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseAppCompatActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示圆形加载对话框，默认消息（请稍等...）
     */
    @Override
    public void showRoundProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!roundProgressDialog.isShowing()) {
                    roundProgressDialog.showProgressDialog();
                }
            }
        });
    }

    /**
     * 显示圆形加载对话框
     *
     * @param msg 提示消息
     */
    @Override
    public void showRoundProgressDialog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!roundProgressDialog.isShowing()) {
                    roundProgressDialog.showProgressDialog(msg);
                }
            }
        });
    }

    /**
     * 关闭圆形加载对话框
     */
    @Override
    public void closeRoundProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (roundProgressDialog.isShowing()) {
                    roundProgressDialog.closeProgressDialog();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
