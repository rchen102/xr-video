//app.js
App({
  serverUrl: "http://192.168.0.15:8081",
  // serverUrl: "https://ec7532e4dc4d.ngrok.io",
  
  setGlobalUserInfo: function(user) {
    wx.setStorageSync('userInfo', user);
  },

  getGlobalUserInfo: function () {
    return wx.getStorageSync('userInfo');
  },

  reportReasonArray: [
    "色情低俗",
    "政治敏感",
    "涉嫌诈骗",
    "辱骂谩骂",
    "广告垃圾",
    "诱导分享",
    "引人不适",
    "其它原因"
  ]
})