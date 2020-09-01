const app = getApp();

// 上传视频工具类
var videoUtil = require('../../utils/videoUtil.js');

Page({
  // 页面初始数据
  data: {
    faceUrl: "../resource/images/noneface.png",
    // 判断是否是本人
    isMe: true, 
    // 判断是否关注
    isFollow: false,
    // 视频创建者 id，用于关注/取消关注时使用
    publisherId: '',

    // 如果是个人主页，就是自己的 id
    // 如果是访问他人主页，就是他人的 id
    userId: '',

    // 作品，收藏，关注，样式控制
    videoSelClass: "video-info",
    // 默认一开始，作品被选中
    isSelectedWork: "video-info-selected",
    isSelectedLike: "",
    isSelectedFollow: "",

    // 配合实现选中效果，false 代表当前正在下方展示
    myWorkFalg: false,
    myLikesFalg: true,
    myFollowFalg: true,

    // 分页查询相关参数
    myVideoList: [],
    myVideoPage: 1,
    myVideoTotal: 1,

    likeVideoList: [],
    likeVideoPage: 1,
    likeVideoTotal: 1,

    followVideoList: [],
    followVideoPage: 1,
    followVideoTotal: 1
  },

  // 生命周期，页面初次加载，只会调用一次
  onLoad: function(params) {
    var me = this;
    // 当前 mini app 缓存的用户信息
    var user = app.getGlobalUserInfo();
    
    // 判断是否访问的是别人的主页
    var userId = user.id;
    var publisherId = params.publisherId;
    if (publisherId != null && publisherId != undefined && publisherId != '') {
      if (userId != publisherId) {
        me.setData({
          isMe: false,
          publisherId: publisherId,
        });
        userId = publisherId;
      }
    }
    me.setData({
      userId:userId
    });

    // 展示等待图标
    wx.showLoading({
      title: '...',
      mask: true
    });
    // 调用后端接口获取用户信息,如果非主页，则还要获取关注信息
    // 注意 token 相关发送的是当前登录用户的信息
    var authId = '';
    var authToken = '';
    if (user.id != null && user.id != undefined && user.id != '') {
      authId = user.id;
    }
    if (user.userToken != null && user.userToken != undefined && user.userToken != '') {
      authToken = user.userToken;
    }
    // 访问后端，拉取用户信息
    var serverUrl = app.serverUrl;
    wx.request({
      url: serverUrl + '/user/query?userId=' + userId + '&fanId=' + user.id,
      method: 'POST',
      header: {
        'content-type': 'application/json',
        'userId': authId,
        'userToken': authToken
      },
      success: function (res) {
        wx.hideLoading();
        var status = res.data.status;
        if (status == 200) {
          var userInfo = res.data.data;
          // 初始化用户数据
          // 用户头像
          var faceUrl = "../resource/images/noneface.png";
          if (userInfo.faceImage != null && userInfo.faceImage != "" && userInfo.faceImage != undefined) {
            faceUrl = serverUrl + userInfo.faceImage;
          }
          me.setData({
            faceUrl: faceUrl,
            fansCounts: userInfo.fansCounts,
            followCounts: userInfo.followCounts,
            receiveLikeCounts: userInfo.receiveLikeCounts,
            nickname: userInfo.nickname,
            isFollow: userInfo.follow
          });
          me.flush();
        } else if (status == 502) {
          wx.showToast({
            title: res.data.msg + ", 请登录",
            icon: 'none',
            duration: 2000
          });
          setTimeout(() => {
            wx.redirectTo({
              url: '../userLogin/login',
            });
          }, 3000);
        }
      }
    });
  },

  // onShow: function() {
  //   this.flush();
  // },

  // 注销
  logout: function() {
    // var user = app.userInfo;
    var user = app.getGlobalUserInfo();
    // 展示等待图标
    wx.showLoading({
      title: '...',
    });
    // 调用后端接口
    var serverUrl = app.serverUrl;
    wx.request({
      url: serverUrl + '/logout?userId=' + user.id,
      method: 'POST',
      header: {
        'content-type': 'application/json' //默认值
      },
      success: function (res) {
        console.log(res.data);
        // 关闭等待效果
        wx.hideLoading();
        // 解析后端回复
        var status = res.data.status;
        if (status == 200) {
          wx.showToast({
            title: '退出成功',
            icon: 'success',
            duration: 2000
          });
          // 注销后，删除用户信息缓存
          wx.removeStorageSync('userInfo');
          // 跳转到用户登录界面
          wx.redirectTo({
            url: '../userLogin/login',
          });
        }
      }
    })

  },

  // 更改头像
  changeFace: function() {
    // 后续this作用域会发生改变
    var me = this;
    // 选择图片
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album'],
      success(res) {
        var tempFilePaths = res.tempFilePaths;

        wx.showLoading({
          title: '正在上传...',
        });

        // 上传至服务器
        var serverUrl = app.serverUrl;
        var userInfo = app.getGlobalUserInfo();
        wx.uploadFile({
          url: serverUrl + '/user/uploadFace?userId=' + userInfo.id,
          filePath: tempFilePaths[0],
          name: 'file',
          header: {
            'content-type': 'application/json',
            'userId': userInfo.id,
            'userToken': userInfo.userToken
          },
          success: function(res) {
            // 注意返回的 res.data 是 string，不是 json 类型
            var data = JSON.parse(res.data);
            wx.hideLoading();
            if (data.status == 200) {
              wx.showToast({
                title: '上传成功',
                icon: "success",
                duration: 1500
              });

              var imageUrl = data.data;
              // 注意，这里不能直接用this
              me.setData({
                faceUrl: serverUrl + imageUrl
              });
            } else if (data.status == 500) {
              wx.showToast({
                title: res.data.msg
              });
            }
          }
        });
      }
    });
  },

  // 上传短视频（到此步并未真正上传到后端）
  uploadVideo:function() {
    videoUtil.uploadVideo();
  },

  // 刷新
  flush: function() {
    var user = app.getGlobalUserInfo();
    if (user == null || user == undefined || user == '') return;
    var myWorkFalg = this.data.myWorkFalg;
    var myLikesFalg = this.data.myLikesFalg;
    var myFollowFalg = this.data.myFollowFalg;
    if (!myWorkFalg) {
      this.doSelectWork();
    } else if (!myLikesFalg) {
      this.doSelectLike();
    } else {
      this.doSelectFollow();
    }
  },

  followMe: function(e) {
    var me = this;
    var publisherId = me.data.publisherId;
    var user = app.getGlobalUserInfo();
    var userId = user.id;

    var followType = e.currentTarget.dataset.followtype;
    // 1: 关注，0：取消关注
    var url = '';
    if (followType == '1') {
      url = '/user/beyourfans?userId=' + publisherId + "&fanId=" + userId;
    } else {
      url = '/user/dontbeyourfans?userId=' + publisherId + "&fanId=" + userId;
    }

    // 请求后端
    wx.showLoading();
    wx.request({
      url: app.serverUrl + url,
      method: 'POST',
      header: {
        'content-type': 'application/json',
        'userId': user.id,
        'userToken': user.userToken
      },
      success: function(res) {
        wx.hideLoading();
        var status = res.data.status;
        if (status == 200) {
          if (followType == '1') {
            me.setData({
              isFollow: true,
              fansCounts: ++me.data.fansCounts
            });
          } else {
            me.setData({
              isFollow: false,
              fansCounts: --me.data.fansCounts
            });
          }
        }
      }
    });

  },

  // 选中作品
  doSelectWork: function () {
    this.setData({
      // 设置被选中效果
      isSelectedWork: "video-info-selected",
      isSelectedLike: "",
      isSelectedFollow: "",

      myWorkFalg: false,
      myLikesFalg: true,
      myFollowFalg: true,

      // 初始化分页参数
      myVideoList: [],
      myVideoPage: 1,
      myVideoTotal: 1,
    });

    this.getMyVideoList(1);
  },

  // 选中收藏
  doSelectLike: function () {
    this.setData({
      // 设置被选中效果
      isSelectedWork: "",
      isSelectedLike: "video-info-selected",
      isSelectedFollow: "",

      myWorkFalg: true,
      myLikesFalg: false,
      myFollowFalg: true,

      // 初始化分页参数
      likeVideoList: [],
      likeVideoPage: 1,
      likeVideoTotal: 1,
    });

    this.getMyLikesList(1);
  },

  doSelectFollow: function () {
    this.setData({
      // 设置被选中效果
      isSelectedWork: "",
      isSelectedLike: "",
      isSelectedFollow: "video-info-selected",

      myWorkFalg: true,
      myLikesFalg: true,
      myFollowFalg: false,

      // 初始化分页参数
      followVideoList: [],
      followVideoPage: 1,
      followVideoTotal: 1
    });

    this.getMyFollowList(1)
  },

  getMyVideoList: function (page) {
    var me = this;
    // 查询视频信息
    wx.showLoading();
    // 调用后端
    var serverUrl = app.serverUrl;
    var userId = me.data.userId;
    wx.request({
      url: serverUrl + '/video/showAll/?page=' + page + '&pageSize=6',
      method: "POST",
      data: {
        userId: userId
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        var myVideoList = res.data.data.rows;
        wx.hideLoading();

        var newVideoList = me.data.myVideoList;
        me.setData({
          myVideoPage: page,
          myVideoList: newVideoList.concat(myVideoList),
          myVideoTotal: res.data.data.total,
          serverUrl: app.serverUrl
        });
      }
    })
  },

  getMyLikesList: function (page) {
    var me = this;
    var userId = me.data.userId;

    // 查询视频信息
    wx.showLoading();
    // 调用后端
    var serverUrl = app.serverUrl;
    wx.request({
      url: serverUrl + '/video/showMyLike/?userId=' + userId + '&page=' + page + '&pageSize=6',
      method: "POST",
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        var likeVideoList = res.data.data.rows;
        wx.hideLoading();

        var newVideoList = me.data.likeVideoList;
        me.setData({
          likeVideoPage: page,
          likeVideoList: newVideoList.concat(likeVideoList),
          likeVideoTotal: res.data.data.total,
          serverUrl: app.serverUrl
        });
      }
    })
  },

  getMyFollowList: function (page) {
    var me = this;
    var userId = me.data.userId;

    // 查询视频信息
    wx.showLoading();
    // 调用后端
    var serverUrl = app.serverUrl;
    wx.request({
      url: serverUrl + '/video/showMyFollow/?userId=' + userId + '&page=' + page + '&pageSize=6',
      method: "POST",
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        var followVideoList = res.data.data.rows;
        wx.hideLoading();

        var newVideoList = me.data.followVideoList;
        me.setData({
          followVideoPage: page,
          followVideoList: newVideoList.concat(followVideoList),
          followVideoTotal: res.data.data.total,
          serverUrl: app.serverUrl
        });
      }
    })
  },

  // 到底部后触发加载
  onReachBottom: function () {
    var myWorkFalg = this.data.myWorkFalg;
    var myLikesFalg = this.data.myLikesFalg;
    var myFollowFalg = this.data.myFollowFalg;

    if (!myWorkFalg) {
      var currentPage = this.data.myVideoPage;
      var totalPage = this.data.myVideoTotal;
      // 获取总页数进行判断，如果当前页数和总页数相等，则不分页
      if (currentPage === totalPage) {
        wx.showToast({
          title: '已经没有视频啦...',
          icon: "none"
        });
        return;
      }
      var page = currentPage + 1;
      this.getMyVideoList(page);
    } else if (!myLikesFalg) {
      var currentPage = this.data.likeVideoPage;
      var totalPage = this.data.myLikesTotal;
      // 获取总页数进行判断，如果当前页数和总页数相等，则不分页
      if (currentPage === totalPage) {
        wx.showToast({
          title: '已经没有视频啦...',
          icon: "none"
        });
        return;
      }
      var page = currentPage + 1;
      this.getMyLikesList(page);
    } else if (!myFollowFalg) {
      var currentPage = this.data.followVideoPage;
      var totalPage = this.data.followVideoTotal;
      // 获取总页数进行判断，如果当前页数和总页数相等，则不分页
      if (currentPage === totalPage) {
        wx.showToast({
          title: '已经没有视频啦...',
          icon: "none"
        });
        return;
      }
      var page = currentPage + 1;
      this.getMyFollowList(page);
    }
  },

  // 点击跳转到视频详情页面
  showVideo: function (e) {
    var myWorkFalg = this.data.myWorkFalg;
    var myLikesFalg = this.data.myLikesFalg;
    var myFollowFalg = this.data.myFollowFalg;

    if (!myWorkFalg) {
      var videoList = this.data.myVideoList;
    } else if (!myLikesFalg) {
      var videoList = this.data.likeVideoList;
    } else if (!myFollowFalg) {
      var videoList = this.data.followVideoList;
    }

    var arrindex = e.target.dataset.arrindex;
    var videoInfo = JSON.stringify(videoList[arrindex]);

    wx.navigateTo({
      url: '../videoinfo/videoinfo?videoInfo=' + videoInfo
    })

  },
})