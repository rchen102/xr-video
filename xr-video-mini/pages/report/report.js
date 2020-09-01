const app = getApp();

Page({

  data: {
    reasonType: '请点击选择原因',
    reportReasonArray: app.reportReasonArray,
    publishUserId: '',
    videoId: ''
  },

  onLoad: function(params) {
    var me = this;

    var videoId = params.videoId;
    var publishUserId = params.publishUserId;

    me.setData({
      videoId: videoId,
      publishUserId: publishUserId
    });
  },

  changeMe: function(e) {
    var me = this;
    var index = e.detail.value;
    var reasonType = app.reportReasonArray[index];

    me.setData({
      reasonType: reasonType
    });
  },

  submitReport: function(e) {
    var me = this;
    var reasonIndex = e.detail.value.reasonIndex;
    var reasonContent = e.detail.value.reasonContent;

    var user = app.getGlobalUserInfo();
    var curUserId = user.id;

    if (reasonIndex == null || reasonIndex == undefined || reasonIndex == '') {
      wx.showToast({
        title: '请选择举报理由',
        icon: 'none',
        duration: 2000
      });
      return;
    }

    var serverUrl = app.serverUrl;
    wx.request({
      url: serverUrl + '/user/reportUser',
      method: 'POST',
      data: {
        dealUserId: me.data.publishUserId,
        dealVideoId: me.data.videoId,
        title: app.reportReasonArray[reasonIndex],
        content: reasonContent,
        userid: curUserId
      },
      header: {
        'content-type': 'application/json',
        'userId': user.id,
        'userToken': user.userToken
      },
      success: function(res) {
        var status = res.data.status;
        if (status == 200) {
          wx.showToast({
            title: res.data.data,
            icon: 'success',
            duration: 1500,
          });
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      }

    })
  }
 
})