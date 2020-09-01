const app = getApp()

Page({
  data: {
    redirectUrl: '',
  },

  onLoad: function(params) {
    // 获取重定向地址
    var me = this;
    var redirectUrl = params.redirectUrl;

    if (redirectUrl != null && redirectUrl != undefined && redirectUrl != '') {
      redirectUrl = redirectUrl.replace(/#/g, "?");
      redirectUrl = redirectUrl.replace(/@/g, "=");
      me.redirectUrl = redirectUrl;
    }
  },
  
  doLogin: function(e) {
    var me = this;
    var formObject = e.detail.value;
    var username = formObject.username;
    var password = formObject.password;

    // 简单验证
    if (username.length == 0 || password.length == 0) {
      wx.showToast({
        title: '用户名和密码不能为空！',
        icon: 'none',
        duration: 2000
      });
    } else {
      // 展示等待图标，拉取用户信息
      wx.showLoading({
        title: '登录中...',
      });
      // 调用后端接口
      var serverUrl = app.serverUrl;
      wx.request({
        url: serverUrl + '/login',
        method: 'POST',
        data: {
          username: username,
          password: password
        },
        header: {
          'content-type': 'application/json' //默认值
        },
        success: function (res) {
          wx.hideLoading();
          var status = res.data.status;
          if (status == 200) {
            // 登录成功，保存用户信息到缓存
            app.setGlobalUserInfo(res.data.data);

            // 判断是否需要跳转其他页面
            var redirectUrl = me.redirectUrl;
            if (redirectUrl != null && redirectUrl != undefined && redirectUrl != '') {
              // 跳转回先前页面
              wx.showToast({
                title: '登录成功，即将跳转回原界面',
                icon: 'none',
                duration: 1000
              });
              setTimeout(() => {
                wx.redirectTo({
                  url: redirectUrl
                });
              }, 1000);   
            } else {
              // 跳转到，个人主页
              wx.showToast({
                title: '登录成功',
                icon: 'success',
                duration: 1000
              });
              setTimeout(() => {
                wx.redirectTo({
                  url: '../index/index',
                });
              }, 1000);   
            }
          } else if (status == 500) {
            wx.showToast({
              title: res.data.msg,
              icon: 'none',
              duration: 2000
            });
          }
        }
      })
    }
  },

  goRegistPage: function() {
    // navigateTo 是隐藏页面，并不会卸载页面，且有返回键
    wx.navigateTo({
      url: '../userRegist/regist',
    });
  }
})