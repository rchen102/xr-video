const app = getApp()

Page({
  data: {
    // 存储从后端传递来的 bgm 列表
    bgmList: [],
    // 后端地址
    serverUrl: '',
    // 存储从上一个页面传递过来的视频参数
    videoParams: {}
  },

  // 生命周期，加载，整个页面只执行一次
  onLoad: function (params) {
    var me = this;
    // 设置视频参数
    me.setData({
      videoParams: params
    });

    // 展示等待图标
    wx.showLoading({
      title: '...',
    });
    var serverUrl = app.serverUrl;
    var user = app.getGlobalUserInfo();
    // 调用后端接口，拉取bgm列表
    wx.request({
      url: serverUrl + '/bgm/list',
      method: 'POST',
      header: {
        'content-type': 'application/json',
        'userId': user.id,
        'userToken': user.userToken
      },
      success: function (res) {
        wx.hideLoading();
        var status = res.data.status;
        if (status == 200) {
          var bgmList = res.data.data;
          me.setData({
            bgmList: bgmList,
            serverUrl: serverUrl
          });
        } else if (status == 502) {
          wx.showToast({
            title: res.data.msg + ", 请重新登录",
            icon: 'none',
            duration: 1500
          });
          setTimeout(() => {
            wx.redirectTo({
              url: '../userLogin/login',
            })
          }, 1500);
        }
      }
    });
  },

  // 上传视频及相关参数至后端
  upload: function(e) {
    // e 用于捕获点击事件，从而获取 form 里的数据
    var me = this;

    console.log(e);

    // 获取 form 数据（bgm 和 描述）
    var bgmId = e.detail.value.bgmId;
    var desc = e.detail.value.desc;
    // 获取上个页面传递的视频参数
    var duration = me.data.videoParams.duration;
    var tmpHeight = me.data.videoParams.tmpHeight;
    var tmpWidth = me.data.videoParams.tmpWidth;
    var tmpVideoUrl = me.data.videoParams.tmpVideoUrl;
    var tmpCoverUrl = me.data.videoParams.tmpCoverUrl;

    // 上传视频至服务器
    wx.showLoading({
      title: '上传中...',
      mask: true
    });
    // 上传视频文件
    var serverUrl = app.serverUrl; // 获取后端地址
    var userInfo = app.getGlobalUserInfo();  // 获取用户对象
    wx.uploadFile({
      url: serverUrl + '/video/upload',
      formData: {
        userId: userInfo.id,    
        bgmId: bgmId,
        desc: desc,
        videoSeconds: duration,
        videoHeight: tmpHeight,
        videoWidth: tmpWidth
      },
      filePath: tmpVideoUrl,
      name: 'file',
      header: {
        'content-type': 'application/json',
        'userId': userInfo.id,
        'userToken': userInfo.userToken
      },
      success: function (res) {
        wx.hideLoading();
        // 注意返回的 res.data 是 string，不是 json 类型
        var data = JSON.parse(res.data); 
        var status = data.status;
        if (status == 200) {
          wx.showToast({
            title: '上传成功',
            icon: "success",
            duration: 1500
          });
          // 1.5s 后跳转回个人信息页面
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        } else if (status == 502) {
          wx.showToast({
            title: data.msg + ", 请重新登录",
            icon: 'none',
            duration: 1500
          });
          setTimeout(() => {
            wx.redirectTo({
              url: '../userLogin/login',
            })
          }, 1500);
        } else {
          // 上传视频失败
          wx.showToast({
            title: '上传失败',
            duration: 1500
          });
        }
      }
    });
  }
})