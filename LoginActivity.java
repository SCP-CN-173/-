package com.app.map.demo;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


import com.app.map.demo.base.BaseActivity;
import com.app.map.demo.base.MyApplication;
import com.app.map.demo.bean.UserInfoBean;
import com.app.map.demo.utils.StrUtil;
import com.app.map.demo.utils.ToastUtil;

import org.litepal.crud.DataSupport;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.tv_regi)
    TextView tv_regi;
    @BindView(R.id.tv_login)
    TextView tv_login;
    @BindView(R.id.et_phone)
    EditText et_phone;
    @BindView(R.id.et_pwd)
    EditText et_pwd;

    private Fragment fragment0, fragment1;

    @Override
    protected void setContent() {
        super.setContent();
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        tv_regi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RegiDialog(myActivity).showDialog();
            }
        });

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempPhone = et_phone.getText().toString().trim();
                String tempPwd = et_pwd.getText().toString().trim();
                if (StrUtil.isEmpty(tempPhone) || StrUtil.isEmpty(tempPwd)) {
                    ToastUtil.showToast(myActivity, "手机号或密码不能为空");
                    return;
                }
                if (tempPhone.length()!=11) {
                    ToastUtil.showToast(myActivity, "手机号错误");
                    return;
                }
                List<UserInfoBean> list = DataSupport.where("name = " + tempPhone + " and " + "pwd = " + tempPwd).find(UserInfoBean.class);//查询表Comment
                if (list.size() == 0) {
                    ToastUtil.showToast(myActivity, "手机号或密码错误");
                    return;
                }
                MyApplication.curUser = tempPhone;
                startActivity(new Intent(myContext, MainActivity.class));
                finish();
            }
        });
    }
}
