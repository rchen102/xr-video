// 上传短视频工具类（到此步并未真正上传到后端）
function uploadVideo() {
  // 选择视频
  wx.chooseVideo({
    sourceType: ['album'],
    success: function(res) {
      wx.showLoading({
        title: '...',
      });

      // 获取上传视频的详细参数（准备传入下一个页面）
      var duration = res.duration;
      var tmpHeight = res.height;
      var tmpWidth = res.width;
      var tmpVideoUrl = res.tempFilePath;    // 视频的临时路径
      var tmpCoverUrl = res.thumbTempFilePath; // 视频封面的临时路径

      wx.hideLoading();
      if (duration > 16) {
        wx.showToast({
          title: '视频长度不能超过 15 秒',
          icon: "none",
          duration: 2500
        });
      } else if (duration < 1) {
        wx.showToast({
          title: '视频长度太短，请上传超过 1 秒的视频',
          icon: "none",
          duration: 2500
        });
      } else {
        // 打开选择 bgm 的页面，并传入相关参数
        wx.showToast({
          title: '处理完成',
          icon: 'success',
          duration: 1000
        });

        setTimeout(() => {
          wx.navigateTo({
            url: '../chooseBgm/chooseBgm?duration=' + duration
              + "&tmpHeight=" + tmpHeight
              + "&tmpWidth=" + tmpWidth
              + "&tmpVideoUrl=" + tmpVideoUrl
              + "&tmpCoverUrl=" + tmpCoverUrl
          });
        }, 1000);        
      }
    },
  });
}

module.exports = {
  uploadVideo: uploadVideo
}

