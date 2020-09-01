const app = getApp()

Page({
    data: {

    },
    doRegist: function(e) {
      var formObject = e.detail.value;
      var username = formObject.username;
      var password = formObject.password;

      // 简单验证
      if (username.length == 0 || password.length == 0) {
        wx.showToast({
          title: '用户名和密码不能为空！',
          icon: 'none',
          duration: 2000
        })
      } else {
        // 展示等待图标
        wx.showLoading({
          title: '注册中...',
        });
        // 调用后端接口
        var serverUrl = app.serverUrl;
        wx.request({
          url: serverUrl + '/regist',
          method: 'POST',
          data: {
            username: username,
            password: password
          },
          header: {
            'content-type': 'application/json' //默认值
          },
          success: function(res) {
            // 关闭等待效果
            wx.hideLoading();
            // 解析后端回复
            var status = res.data.status;
            if (status == 200) {
              wx.showToast({
                title: '注册成功!',
                icon: 'success',
                duration: 2000,
                success: function () {
                  setTimeout(function () {
                    // 保存全局用户信息
                    app.setGlobalUserInfo(res.data.data);
                    // 跳转到 我的界面
                    wx.redirectTo({
                      url: '../index/index',
                    });
                  }, 2000);
                }
              });
            } else if (status == 500) {
              wx.showToast({
                title: res.data.msg,
                icon: 'fail',
                duration: 2000
              });
            }
          }
        })
      }
    },

    goLoginPage: function() {
      wx.navigateTo({
        url: '../userLogin/login',
      });
    }
})