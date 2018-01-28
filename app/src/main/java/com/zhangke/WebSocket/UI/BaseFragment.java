package com.zhangke.WebSocket.UI;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Fragment基类
 * <p>
 * Created by 张可 on 2017/5/23.
 */

public abstract class BaseFragment extends Fragment implements IBaseActivity {

    protected final String TAG = this.getClass().getSimpleName();

    /**
     * rootView是否初始化标志，防止回调函数在rootView为空的时候触发
     */
    private boolean hasCreateView;

    /**
     * 当前Fragment是否处于可见状态标志，防止因ViewPager的缓存机制而导致回调函数的触发
     */
    private boolean isFragmentVisible;

    private boolean fragmentIsFirstVisible = true;

    private RoundProgressDialog roundProgressDialog;
    protected Activity mActivity;
    protected View rootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!hasCreateView && getUserVisibleHint()) {
            onFragmentVisible();
            isFragmentVisible = true;
            if(fragmentIsFirstVisible){
                onFragmentFirstVisible();
                fragmentIsFirstVisible = false;
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        roundProgressDialog = new RoundProgressDialog(mActivity);
        rootView = inflater.inflate(getFragmentLayoutId(), container, false);
        initBind();
        initView();
        return rootView;
    }

    /**
     * 绑定服务注册 ButterKnife 等等
     */
    protected void initBind() {}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentIsFirstVisible = true;
    }

    /**
     * 实现Fragment数据的缓加载
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (rootView == null) {
            return;
        }
        hasCreateView = true;
        if (isVisibleToUser) {
            onFragmentVisible();
            isFragmentVisible = true;
            if(fragmentIsFirstVisible){
                onFragmentFirstVisible();
                fragmentIsFirstVisible = false;
            }
            return;
        }
        if (isFragmentVisible) {
            onFragmentGone();
            isFragmentVisible = false;
        }
    }

    private void initVariable() {
        hasCreateView = false;
        isFragmentVisible = false;
    }

    /**
     * 如果需要用到懒加载，可以使用此方法加载数据
     */
    protected void onFragmentVisible() {}
    /**
     * Fragment第一次显示
     */
    protected void onFragmentFirstVisible(){}
    /**
     * Fragment不可见时调用此方法
     */
    protected void onFragmentGone(){}
    /**
     * 返回Fragment layout资源ID
     *
     * @return
     */
    protected abstract int getFragmentLayoutId();

    protected abstract void initView();

    @Override
    public void showToastMessage(final String msg) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示圆形加载对话框，默认消息（请稍等...）
     */
    @Override
    public void showRoundProgressDialog() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(roundProgressDialog != null && !roundProgressDialog.isShowing()) {
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(roundProgressDialog != null && !roundProgressDialog.isShowing()) {
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(roundProgressDialog != null && roundProgressDialog.isShowing())
                    roundProgressDialog.closeProgressDialog();
            }
        });
    }
}
